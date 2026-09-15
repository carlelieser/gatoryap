// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server

import com.gatoryap.server.config.AppConfig
import com.gatoryap.server.db.DatabaseFactory
import com.gatoryap.server.plugins.configureMonitoring
import com.gatoryap.server.plugins.configureSerialization
import com.gatoryap.server.plugins.configureStatusPages
import com.gatoryap.server.routes.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import javax.sql.DataSource

fun main() {
    val config = AppConfig.fromEnv()
    val dataSource = DatabaseFactory(config.database).connect()

    embeddedServer(
        factory = Netty,
        port = config.http.port,
        host = config.http.host,
        module = { module(dataSource) },
    ).start(wait = true)
}

fun Application.module(dataSource: DataSource) {
    configureSerialization()
    configureMonitoring()
    configureStatusPages()

    healthRoutes(dataSource)
}
