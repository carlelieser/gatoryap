// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server

import com.gatoryap.server.auth.Argon2PasswordHasher
import com.gatoryap.server.auth.Argon2Params
import com.gatoryap.server.config.AppConfig
import com.gatoryap.server.config.DatabaseConfig
import com.gatoryap.server.db.DatabaseFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

/**
 * One Postgres container for the whole suite, migrated through the production
 * [DatabaseFactory] so every test also exercises the Flyway migration.
 */
private object TestDatabase {

    private val container = PostgreSQLContainer("postgres:18.6-alpine")
        .withDatabaseName("gatoryap")
        .apply { start() }

    val config: DatabaseConfig by lazy {
        DatabaseConfig(
            url = container.jdbcUrl,
            user = container.username,
            password = container.password,
            maxPoolSize = 4,
        )
    }

    val dataSource by lazy { DatabaseFactory(config).connect() }
}

/**
 * Argon2 at production cost would add roughly a second to every test that
 * registers a user. The cost setting is covered by its own unit test.
 */
private val testHasher = Argon2PasswordHasher(Argon2Params(memoryKib = 256, iterations = 1))

private val testEnv: (String) -> String? = { name ->
    when (name) {
        "JWT_SECRET" -> "a-test-secret-long-enough-to-sign-tokens"
        "DATABASE_URL" -> TestDatabase.config.url
        "DATABASE_USER" -> TestDatabase.config.user
        "DATABASE_PASSWORD" -> TestDatabase.config.password
        else -> null
    }
}

/**
 * Runs [block] against the real application wired to a throwaway database,
 * starting from an empty users table so tests cannot leak into each other.
 */
fun withAuthServer(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
    val dataSource = TestDatabase.dataSource
    clearDatabase()

    application {
        module(ServerDependencies.assemble(AppConfig.fromEnv(testEnv), dataSource, testHasher))
    }

    val client = createClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    block(client)
}

/** Deleting users cascades to their refresh tokens. */
private fun clearDatabase() {
    transaction { com.gatoryap.server.db.UsersTable.deleteAll() }
}
