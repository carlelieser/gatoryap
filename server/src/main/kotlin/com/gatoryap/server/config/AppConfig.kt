// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.config

/**
 * Configuration is read from the environment only. The server is agnostic about
 * which managed Postgres provider backs it — anything reachable over JDBC works.
 */
data class AppConfig(
    val http: HttpConfig,
    val database: DatabaseConfig,
) {
    companion object {
        fun fromEnv(env: (String) -> String? = System::getenv): AppConfig = AppConfig(
            http = HttpConfig(
                port = env("PORT")?.toIntOrNull() ?: 8080,
                host = env("HOST") ?: "0.0.0.0",
            ),
            database = DatabaseConfig(
                url = env("DATABASE_URL") ?: DEFAULT_DEV_URL,
                user = env("DATABASE_USER") ?: "gatoryap",
                password = env("DATABASE_PASSWORD") ?: "gatoryap",
                maxPoolSize = env("DATABASE_MAX_POOL_SIZE")?.toIntOrNull() ?: 10,
            ),
        )

        /** Matches docker-compose.yml so a clean clone runs with no configuration. */
        private const val DEFAULT_DEV_URL = "jdbc:postgresql://localhost:5433/gatoryap"
    }
}

data class HttpConfig(
    val port: Int,
    val host: String,
)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)
