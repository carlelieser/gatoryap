// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.db

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Column defaults — ids and timestamps — are declared in V1__auth.sql. Flyway
 * owns the schema; nothing here may create it.
 */
object UsersTable : UUIDTable("users") {
    /** citext in Postgres. Exposed has no citext type, and since this column is
     *  only ever read and written as text, the case handling stays in the database. */
    val email = text("email")
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable)
    val tokenHash = varchar("token_hash", TOKEN_HASH_LENGTH)
    val issuedAt = timestamp("issued_at")
    val expiresAt = timestamp("expires_at")
    val revokedAt = timestamp("revoked_at").nullable()
    val replacedBy = reference("replaced_by", RefreshTokensTable).nullable()
}

/** SHA-256 rendered as hex. */
private const val TOKEN_HASH_LENGTH = 64
