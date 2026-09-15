// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import javax.sql.DataSource

/**
 * Liveness and readiness. [readyz] reports database reachability so deployments
 * fail loudly rather than serving traffic against a dead pool.
 */
fun Application.healthRoutes(dataSource: DataSource) {
    routing {
        get("/healthz") {
            call.respond(HealthResponse(status = "ok"))
        }

        get("/readyz") {
            val databaseUp = runCatching {
                dataSource.connection.use { it.isValid(TIMEOUT_SECONDS) }
            }.getOrDefault(false)

            if (databaseUp) {
                call.respond(ReadinessResponse(status = "ok", database = "up"))
            } else {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    ReadinessResponse(status = "degraded", database = "down"),
                )
            }
        }
    }
}

private const val TIMEOUT_SECONDS = 2

@Serializable
data class HealthResponse(val status: String)

@Serializable
data class ReadinessResponse(val status: String, val database: String)
