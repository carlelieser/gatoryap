// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.db

import com.gatoryap.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource

/**
 * Owns the connection pool and schema migration. Migrations run on boot so there
 * is no separate migrate step in local development or deployment.
 */
class DatabaseFactory(private val config: DatabaseConfig) {

    fun connect(): DataSource {
        val dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                maximumPoolSize = config.maxPoolSize
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        )

        migrate(dataSource)
        Database.connect(dataSource)
        return dataSource
    }

    private fun migrate(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            // The migrations directory is intentionally empty; this keeps boot
            // working until the first migration lands.
            .validateOnMigrate(true)
            .load()
            .migrate()
    }
}
