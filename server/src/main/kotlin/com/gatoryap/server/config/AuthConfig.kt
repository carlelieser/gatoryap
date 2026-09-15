// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.config

import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import org.slf4j.LoggerFactory

data class AuthConfig(
    val jwtSecret: String,
    val issuer: String,
    val audience: String,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
)

/**
 * Reads [AuthConfig] from the environment.
 *
 * Kept apart from [AppConfig.fromEnv] because resolving the signing secret is a
 * policy decision, not a lookup: there is deliberately no default value.
 */
internal object AuthConfigReader {

    private const val MIN_SECRET_LENGTH = 32
    private const val SECRET_BYTES = 32

    fun read(env: (String) -> String?): AuthConfig = AuthConfig(
        jwtSecret = resolveSecret(env),
        issuer = env("JWT_ISSUER") ?: "gatoryap",
        audience = env("JWT_AUDIENCE") ?: "gatoryap-app",
        accessTokenTtl = env("ACCESS_TOKEN_TTL_MINUTES")?.toLongOrNull()?.minutes
            ?: DEFAULT_ACCESS_TTL,
        refreshTokenTtl = env("REFRESH_TOKEN_TTL_DAYS")?.toLongOrNull()?.days
            ?: DEFAULT_REFRESH_TTL,
    )

    /**
     * A hardcoded fallback secret would let a known signing key reach
     * production unnoticed, so development gets a fresh random one each boot
     * and production refuses to start without an explicit value.
     */
    private fun resolveSecret(env: (String) -> String?): String {
        val configured = env("JWT_SECRET")?.takeIf { it.isNotBlank() }
        if (configured != null) return validated(configured)

        if (isProduction(env)) {
            throw IllegalStateException(
                "JWT_SECRET is required when APP_ENV=production, but it was not set"
            )
        }
        return generateDevelopmentSecret()
    }

    private fun validated(secret: String): String {
        if (secret.length >= MIN_SECRET_LENGTH) return secret
        throw IllegalStateException(
            "JWT_SECRET must be at least $MIN_SECRET_LENGTH characters, but was ${secret.length}"
        )
    }

    private fun isProduction(env: (String) -> String?): Boolean =
        env("APP_ENV")?.lowercase() == "production"

    private fun generateDevelopmentSecret(): String {
        val bytes = ByteArray(SECRET_BYTES).also(SecureRandom()::nextBytes)
        LoggerFactory.getLogger(AuthConfigReader::class.java).warn(
            "JWT_SECRET is not set — generated a temporary development secret. " +
                "Every token becomes invalid when the server restarts. " +
                "Set JWT_SECRET to keep sessions across restarts."
        )
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private val DEFAULT_ACCESS_TTL = 15.minutes
    private val DEFAULT_REFRESH_TTL = 30.days
}
