// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.gatoryap.server.config.AuthConfig
import com.gatoryap.server.db.UserRecord
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Instant

data class AccessToken(
    val value: String,
    val expiresAt: Instant,
)

/**
 * Issues and verifies access tokens.
 *
 * Tokens are self-contained, so an ordinary authenticated request touches no
 * database. The cost is that revocation is not immediate: a token stays valid
 * until it expires, which is why the lifetime is short and why logout works
 * through the refresh token instead.
 *
 * HS256 suits a single service holding its own secret. RS256 only earns its
 * extra machinery once something else must verify without being able to sign.
 */
class TokenIssuer(private val config: AuthConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.jwtSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    fun issue(user: UserRecord): AccessToken {
        val issuedAt = Clock.System.now()
        val expiresAt = issuedAt + config.accessTokenTtl

        val token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString())
            .withClaim(CLAIM_EMAIL, user.email)
            .withIssuedAt(Date(issuedAt.toEpochMilliseconds()))
            .withExpiresAt(Date(expiresAt.toEpochMilliseconds()))
            .sign(algorithm)

        return AccessToken(value = token, expiresAt = expiresAt)
    }

    companion object {
        const val CLAIM_EMAIL: String = "email"
    }
}
