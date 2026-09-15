// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.db

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

data class RefreshTokenRecord(
    val id: UUID,
    val userId: UUID,
    val expiresAt: Instant,
    val revokedAt: Instant?,
) {
    val isRevoked: Boolean get() = revokedAt != null

    fun hasExpiredAt(moment: Instant): Boolean = expiresAt <= moment
}

/** A refresh token about to be stored. Only the hash is ever persisted. */
data class NewRefreshToken(
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
)

class RefreshTokenRepository {

    suspend fun insert(token: NewRefreshToken): UUID = dbQuery { insertToken(token) }

    suspend fun findByHash(tokenHash: String): RefreshTokenRecord? = dbQuery {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.tokenHash eq tokenHash }
            .limit(1)
            .firstOrNull()
            ?.toRefreshTokenRecord()
    }

    /**
     * Stores the replacement and retires its predecessor together, so a failure
     * midway cannot leave a user holding two live tokens or none at all.
     */
    suspend fun rotate(previousId: UUID, replacement: NewRefreshToken): UUID = dbQuery {
        val replacementId = insertToken(replacement)
        RefreshTokensTable.update({ RefreshTokensTable.id eq previousId }) {
            it[revokedAt] = Clock.System.now()
            it[replacedBy] = replacementId
        }
        replacementId
    }

    /** Returns the number of tokens actually revoked. */
    suspend fun revoke(id: UUID): Int = dbQuery {
        RefreshTokensTable.update({ liveToken(id) }) {
            it[revokedAt] = Clock.System.now()
        }
    }

    /**
     * Used when a retired token is presented again, which means the token has
     * leaked: every live session for the user is ended.
     */
    suspend fun revokeAllForUser(userId: UUID): Int = dbQuery {
        RefreshTokensTable.update({ liveTokensOf(userId) }) {
            it[revokedAt] = Clock.System.now()
        }
    }
}

private fun JdbcTransaction.insertToken(token: NewRefreshToken): UUID =
    RefreshTokensTable.insertReturning {
        it[userId] = token.userId
        it[tokenHash] = token.tokenHash
        it[expiresAt] = token.expiresAt
    }.single()[RefreshTokensTable.id].value

private fun liveToken(id: UUID): Op<Boolean> =
    (RefreshTokensTable.id eq id) and RefreshTokensTable.revokedAt.isNull()

private fun liveTokensOf(userId: UUID): Op<Boolean> =
    (RefreshTokensTable.userId eq userId) and RefreshTokensTable.revokedAt.isNull()

private fun ResultRow.toRefreshTokenRecord() = RefreshTokenRecord(
    id = this[RefreshTokensTable.id].value,
    userId = this[RefreshTokensTable.userId].value,
    expiresAt = this[RefreshTokensTable.expiresAt],
    revokedAt = this[RefreshTokensTable.revokedAt],
)
