// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server

import com.gatoryap.server.auth.Argon2PasswordHasher
import com.gatoryap.server.auth.AuthDependencies
import com.gatoryap.server.auth.AuthService
import com.gatoryap.server.auth.PasswordHasher
import com.gatoryap.server.auth.RefreshTokenService
import com.gatoryap.server.auth.TokenIssuer
import com.gatoryap.server.config.AppConfig
import com.gatoryap.server.config.AuthConfig
import com.gatoryap.server.db.RefreshTokenRepository
import com.gatoryap.server.db.UserRepository
import javax.sql.DataSource

/**
 * Everything [module] needs, assembled once at startup.
 *
 * The project uses no dependency-injection framework: construction is explicit
 * and readable in one place, and tests get a single seam to build against a
 * throwaway database.
 */
class ServerDependencies(
    val dataSource: DataSource,
    val authService: AuthService,
    val users: UserRepository,
    val tokenIssuer: TokenIssuer,
    val authConfig: AuthConfig,
) {
    companion object {
        /**
         * [hasher] is a parameter so tests can substitute cheaper cost
         * parameters; production always uses the OWASP defaults.
         */
        fun assemble(
            config: AppConfig,
            dataSource: DataSource,
            hasher: PasswordHasher = Argon2PasswordHasher(),
        ): ServerDependencies {
            val users = UserRepository()
            val refreshTokens = RefreshTokenRepository()
            val tokenIssuer = TokenIssuer(config.auth)

            val authService = AuthService(
                AuthDependencies(
                    users = users,
                    refreshTokens = refreshTokens,
                    hasher = hasher,
                    tokenIssuer = tokenIssuer,
                    refreshTokenService = RefreshTokenService(),
                    config = config.auth,
                )
            )

            return ServerDependencies(
                dataSource = dataSource,
                authService = authService,
                users = users,
                tokenIssuer = tokenIssuer,
                authConfig = config.auth,
            )
        }
    }
}
