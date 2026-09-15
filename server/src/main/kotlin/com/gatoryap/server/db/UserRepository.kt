// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.db

import java.util.UUID
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll

data class UserRecord(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,
)

/** A user about to be created. Bundled so the hash is never a bare parameter. */
data class NewUser(
    val email: String,
    val passwordHash: String,
)

class UserRepository {

    suspend fun findByEmail(email: String): UserRecord? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .limit(1)
            .firstOrNull()
            ?.toUserRecord()
    }

    suspend fun findById(id: UUID): UserRecord? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toUserRecord()
    }

    /**
     * Lets the unique constraint decide whether the address is taken. Checking
     * first and inserting after would race with a concurrent signup for the
     * same address. Throws on a duplicate; callers map SQLState 23505.
     */
    suspend fun insert(user: NewUser): UserRecord = dbQuery {
        UsersTable.insertReturning {
            it[email] = user.email
            it[passwordHash] = user.passwordHash
        }.single().toUserRecord()
    }
}

private fun ResultRow.toUserRecord() = UserRecord(
    id = this[UsersTable.id].value,
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.passwordHash],
    createdAt = this[UsersTable.createdAt],
)
