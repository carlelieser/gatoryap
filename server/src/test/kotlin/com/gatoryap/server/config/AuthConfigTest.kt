// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class AuthConfigTest {

    @Test
    fun `uses a configured secret`() {
        val config = AppConfig.fromEnv(envOf("JWT_SECRET" to VALID_SECRET))
        assertEquals(VALID_SECRET, config.auth.jwtSecret)
    }

    @Test
    fun `generates a development secret when none is set`() {
        val config = AppConfig.fromEnv(envOf())
        assertTrue(config.auth.jwtSecret.length >= 32)
    }

    @Test
    fun `generates a different development secret each boot`() {
        val first = AppConfig.fromEnv(envOf()).auth.jwtSecret
        val second = AppConfig.fromEnv(envOf()).auth.jwtSecret
        assertNotEquals(first, second)
    }

    @Test
    fun `refuses to start in production without a secret`() {
        val failure = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnv(envOf("APP_ENV" to "production"))
        }
        assertTrue(failure.message!!.contains("JWT_SECRET"))
    }

    @Test
    fun `treats a blank secret as absent in production`() {
        assertFailsWith<IllegalStateException> {
            AppConfig.fromEnv(envOf("APP_ENV" to "production", "JWT_SECRET" to "   "))
        }
    }

    @Test
    fun `recognises production regardless of case`() {
        assertFailsWith<IllegalStateException> {
            AppConfig.fromEnv(envOf("APP_ENV" to "PRODUCTION"))
        }
    }

    @Test
    fun `rejects a secret that is too short to sign with`() {
        val failure = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnv(envOf("JWT_SECRET" to "too-short"))
        }
        assertTrue(failure.message!!.contains("at least 32"))
    }

    @Test
    fun `accepts a configured secret in production`() {
        val config = AppConfig.fromEnv(
            envOf("APP_ENV" to "production", "JWT_SECRET" to VALID_SECRET)
        )
        assertEquals(VALID_SECRET, config.auth.jwtSecret)
    }

    @Test
    fun `falls back to default token lifetimes`() {
        val auth = AppConfig.fromEnv(envOf()).auth
        assertEquals(15.minutes, auth.accessTokenTtl)
        assertEquals(30.days, auth.refreshTokenTtl)
    }

    @Test
    fun `reads token lifetimes from the environment`() {
        val auth = AppConfig.fromEnv(
            envOf("ACCESS_TOKEN_TTL_MINUTES" to "5", "REFRESH_TOKEN_TTL_DAYS" to "7")
        ).auth
        assertEquals(5.minutes, auth.accessTokenTtl)
        assertEquals(7.days, auth.refreshTokenTtl)
    }

    @Test
    fun `falls back to defaults for unparseable lifetimes`() {
        val auth = AppConfig.fromEnv(envOf("ACCESS_TOKEN_TTL_MINUTES" to "soon")).auth
        assertEquals(15.minutes, auth.accessTokenTtl)
    }

    @Test
    fun `defaults issuer and audience`() {
        val auth = AppConfig.fromEnv(envOf()).auth
        assertEquals("gatoryap", auth.issuer)
        assertEquals("gatoryap-app", auth.audience)
    }

    @Test
    fun `defaults the rate limit`() {
        val rateLimit = AppConfig.fromEnv(envOf()).auth.rateLimit
        assertEquals(10, rateLimit.attempts)
        assertEquals(1.minutes, rateLimit.window)
    }

    @Test
    fun `reads the rate limit from the environment`() {
        val rateLimit = AppConfig.fromEnv(
            envOf("AUTH_RATE_LIMIT_ATTEMPTS" to "3", "AUTH_RATE_LIMIT_WINDOW_MINUTES" to "5")
        ).auth.rateLimit
        assertEquals(3, rateLimit.attempts)
        assertEquals(5.minutes, rateLimit.window)
    }

    private fun envOf(vararg entries: Pair<String, String>): (String) -> String? {
        val values = entries.toMap()
        return { name -> values[name] }
    }

    private companion object {
        const val VALID_SECRET = "a-secret-long-enough-to-sign-tokens-with"
    }
}
