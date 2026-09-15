// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.gatoryap.server.config.AuthConfig
import com.gatoryap.server.config.RateLimitConfig
import com.gatoryap.server.db.UserRecord
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class TokenIssuerTest {

    private val issuer = TokenIssuer(configWith(SECRET))

    @Test
    fun `issues a token that verifies`() {
        val token = issuer.issue(user)
        val decoded = issuer.verifier.verify(token.value)
        assertEquals(user.id.toString(), decoded.subject)
    }

    @Test
    fun `carries the email as a claim`() {
        val decoded = issuer.verifier.verify(issuer.issue(user).value)
        assertEquals(user.email, decoded.getClaim(TokenIssuer.CLAIM_EMAIL).asString())
    }

    @Test
    fun `sets issuer and audience`() {
        val decoded = issuer.verifier.verify(issuer.issue(user).value)
        assertEquals("gatoryap", decoded.issuer)
        assertTrue(decoded.audience.contains("gatoryap-app"))
    }

    @Test
    fun `expires within the configured lifetime`() {
        val token = issuer.issue(user)
        val upperBound = Clock.System.now() + 15.minutes
        assertTrue(token.expiresAt <= upperBound)
    }

    @Test
    fun `rejects a token signed with another secret`() {
        val foreignIssuer = TokenIssuer(configWith("a-different-secret-of-sufficient-length"))
        val foreignToken = foreignIssuer.issue(user)

        assertFailsWith<JWTVerificationException> { issuer.verifier.verify(foreignToken.value) }
    }

    @Test
    fun `rejects a tampered token`() {
        val token = issuer.issue(user).value
        assertFailsWith<JWTVerificationException> { issuer.verifier.verify(token.dropLast(4) + "aaaa") }
    }

    @Test
    fun `rejects an already expired token`() {
        val pastIssuer = TokenIssuer(configWith(SECRET, accessTokenTtl = (-1).minutes))
        val expired = pastIssuer.issue(user)

        assertFailsWith<JWTVerificationException> { issuer.verifier.verify(expired.value) }
    }

    @Test
    fun `rejects a token meant for another audience`() {
        val otherAudience = TokenIssuer(configWith(SECRET).copy(audience = "someone-else"))
        val foreign = otherAudience.issue(user)

        assertFailsWith<JWTVerificationException> { issuer.verifier.verify(foreign.value) }
    }

    private val user = UserRecord(
        id = UUID.fromString("0192f7a0-0000-7000-8000-000000000001"),
        email = "person@example.com",
        passwordHash = "unused",
        createdAt = Clock.System.now(),
    )

    private fun configWith(secret: String, accessTokenTtl: kotlin.time.Duration = 15.minutes) = AuthConfig(
        jwtSecret = secret,
        issuer = "gatoryap",
        audience = "gatoryap-app",
        accessTokenTtl = accessTokenTtl,
        refreshTokenTtl = 30.days,
        rateLimit = RateLimitConfig(attempts = 10, window = 1.minutes),
    )

    private companion object {
        const val SECRET = "a-test-secret-long-enough-to-sign-with"
    }
}
