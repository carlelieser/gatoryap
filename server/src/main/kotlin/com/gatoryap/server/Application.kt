// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server

import com.gatoryap.server.config.AppConfig
import com.gatoryap.server.db.DatabaseFactory
import com.gatoryap.server.plugins.configureAuthentication
import com.gatoryap.server.plugins.configureMonitoring
import com.gatoryap.server.plugins.configureRateLimit
import com.gatoryap.server.plugins.configureSerialization
import com.gatoryap.server.plugins.configureStatusPages
import com.gatoryap.server.routes.authRoutes
import com.gatoryap.server.routes.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = AppConfig.fromEnv()
    val dataSource = DatabaseFactory(config.database).connect()
    val dependencies = ServerDependencies.assemble(config, dataSource)

    embeddedServer(
        factory = Netty,
        port = config.http.port,
        host = config.http.host,
        module = { module(dependencies) },
    ).start(wait = true)
}

fun Application.module(dependencies: ServerDependencies) {
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureAuthentication(dependencies.tokenIssuer, dependencies.authConfig)
    configureRateLimit(dependencies.authConfig.rateLimit)

    healthRoutes(dependencies.dataSource)
    authRoutes(dependencies.authService, dependencies.users)
}
