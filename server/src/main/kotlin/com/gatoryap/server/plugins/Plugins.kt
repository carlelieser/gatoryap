// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.plugins

import com.gatoryap.server.auth.AuthenticatedUser
import com.gatoryap.server.auth.TokenIssuer
import com.gatoryap.server.config.AuthConfig
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        // A body the server cannot read is the caller's mistake, not a fault.
        // The parser message is withheld: it names internal fields.
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "invalid_request"))
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = "internal_error"),
            )
        }
    }
}

/**
 * Verifies access tokens. The challenge is spelled out because Ktor's default
 * answer to a missing token is an empty body, and every error this server
 * returns is an [ErrorResponse].
 */
fun Application.configureAuthentication(tokenIssuer: TokenIssuer, config: AuthConfig) {
    install(Authentication) {
        jwt(JWT_PROVIDER) {
            realm = config.issuer
            verifier(tokenIssuer.verifier)

            validate { credential ->
                val id = credential.payload.subject?.toUuidOrNull()
                val email = credential.payload.getClaim(TokenIssuer.CLAIM_EMAIL).asString()
                if (id == null || email == null) null else AuthenticatedUser(id, email)
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "unauthorized"))
            }
        }
    }
}

const val JWT_PROVIDER: String = "auth-jwt"

private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

@Serializable
data class ErrorResponse(val error: String)
