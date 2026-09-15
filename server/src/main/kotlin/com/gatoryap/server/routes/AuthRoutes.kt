// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.routes

import com.gatoryap.core.auth.LoginRequest
import com.gatoryap.core.auth.LogoutRequest
import com.gatoryap.core.auth.RefreshRequest
import com.gatoryap.core.auth.RegisterRequest
import com.gatoryap.server.auth.AuthService
import com.gatoryap.server.auth.AuthenticatedUser
import com.gatoryap.server.auth.LoginResult
import com.gatoryap.server.auth.LogoutResult
import com.gatoryap.server.auth.RawRefreshToken
import com.gatoryap.server.auth.RefreshResult
import com.gatoryap.server.auth.RegisterResult
import com.gatoryap.server.auth.toPublicUser
import com.gatoryap.server.db.UserRepository
import com.gatoryap.server.plugins.ErrorResponse
import com.gatoryap.server.plugins.JWT_PROVIDER
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.authRoutes(service: AuthService, users: UserRepository) {
    routing {
        route("/auth") {
            post("/register") { register(service) }
            post("/login") { login(service) }
            post("/refresh") { refresh(service) }
            post("/logout") { logout(service) }

            authenticate(JWT_PROVIDER) {
                get("/me") { currentUser(users) }
            }
        }
    }
}

private suspend fun RoutingContext.register(service: AuthService) {
    when (val result = service.register(call.receive<RegisterRequest>())) {
        is RegisterResult.Success -> call.respond(HttpStatusCode.Created, result.tokens)
        is RegisterResult.InvalidCredentials ->
            call.respondError(HttpStatusCode.BadRequest, result.problem.code)
        RegisterResult.EmailAlreadyRegistered ->
            call.respondError(HttpStatusCode.Conflict, "email_already_registered")
    }
}

private suspend fun RoutingContext.login(service: AuthService) {
    when (val result = service.login(call.receive<LoginRequest>())) {
        is LoginResult.Success -> call.respond(result.tokens)
        LoginResult.InvalidCredentials ->
            call.respondError(HttpStatusCode.Unauthorized, "invalid_credentials")
    }
}

private suspend fun RoutingContext.refresh(service: AuthService) {
    val token = RawRefreshToken(call.receive<RefreshRequest>().refreshToken)
    when (val result = service.refresh(token)) {
        is RefreshResult.Success -> call.respond(result.tokens)
        RefreshResult.InvalidRefreshToken ->
            call.respondError(HttpStatusCode.Unauthorized, "invalid_refresh_token")
        RefreshResult.ExpiredRefreshToken ->
            call.respondError(HttpStatusCode.Unauthorized, "expired_refresh_token")
    }
}

/**
 * Answers the same way whether or not the token was live, so logging out twice
 * is not an error and the endpoint reveals nothing about token validity.
 */
private suspend fun RoutingContext.logout(service: AuthService) {
    val token = RawRefreshToken(call.receive<LogoutRequest>().refreshToken)
    when (service.logout(token)) {
        LogoutResult.Success, LogoutResult.UnknownRefreshToken ->
            call.respond(HttpStatusCode.NoContent)
    }
}

/**
 * Reads the user rather than answering from the token alone, so a token
 * outliving its account stops working immediately.
 */
private suspend fun RoutingContext.currentUser(users: UserRepository) {
    val principal = call.principal<AuthenticatedUser>()
        ?: return call.respondError(HttpStatusCode.Unauthorized, "unauthorized")

    val user = users.findById(principal.id)
        ?: return call.respondError(HttpStatusCode.Unauthorized, "unauthorized")

    call.respond(user.toPublicUser())
}

private suspend fun io.ktor.server.application.ApplicationCall.respondError(
    status: HttpStatusCode,
    code: String,
) = respond(status, ErrorResponse(error = code))
