// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.routes

import com.gatoryap.core.auth.LoginRequest
import com.gatoryap.core.auth.LogoutRequest
import com.gatoryap.core.auth.PublicUser
import com.gatoryap.core.auth.RefreshRequest
import com.gatoryap.core.auth.RegisterRequest
import com.gatoryap.core.auth.TokenResponse
import com.gatoryap.server.plugins.ErrorResponse
import com.gatoryap.server.withAuthServer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    @Test
    fun `registering returns tokens and the new user`() = withAuthServer { client ->
        val response = client.register(EMAIL, PASSWORD)
        assertEquals(HttpStatusCode.Created, response.status)

        val tokens = response.body<TokenResponse>()
        assertEquals(EMAIL, tokens.user.email)
        assertTrue(tokens.accessToken.isNotBlank())
        assertTrue(tokens.refreshToken.isNotBlank())
    }

    @Test
    fun `registering twice with the same address is rejected`() = withAuthServer { client ->
        client.register(EMAIL, PASSWORD)

        val response = client.register(EMAIL, "a-different-password")
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("email_already_registered", response.errorCode())
    }

    @Test
    fun `an address differing only in case is the same account`() = withAuthServer { client ->
        client.register("Person@Example.COM", PASSWORD)

        val response = client.login("person@example.com", PASSWORD)
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `registering stores the address in lower case`() = withAuthServer { client ->
        val tokens = client.register("Person@Example.COM", PASSWORD).body<TokenResponse>()
        assertEquals("person@example.com", tokens.user.email)
    }

    @Test
    fun `a short password is rejected`() = withAuthServer { client ->
        val response = client.register(EMAIL, "short")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("password_too_short", response.errorCode())
    }

    @Test
    fun `a malformed address is rejected`() = withAuthServer { client ->
        val response = client.register("not-an-address", PASSWORD)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("email_malformed", response.errorCode())
    }

    @Test
    fun `logging in with the right password returns tokens`() = withAuthServer { client ->
        client.register(EMAIL, PASSWORD)

        val response = client.login(EMAIL, PASSWORD)
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<TokenResponse>().accessToken.isNotBlank())
    }

    @Test
    fun `a wrong password is rejected`() = withAuthServer { client ->
        client.register(EMAIL, PASSWORD)

        val response = client.login(EMAIL, "not-the-password")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("invalid_credentials", response.errorCode())
    }

    /** An unknown address must be indistinguishable from a wrong password. */
    @Test
    fun `an unknown address answers exactly as a wrong password does`() = withAuthServer { client ->
        client.register(EMAIL, PASSWORD)

        val wrongPassword = client.login(EMAIL, "not-the-password")
        val unknownAddress = client.login("nobody@example.com", "not-the-password")

        assertEquals(wrongPassword.status, unknownAddress.status)
        assertEquals(wrongPassword.errorCode(), unknownAddress.errorCode())
    }

    @Test
    fun `refreshing returns a new pair`() = withAuthServer { client ->
        val original = client.register(EMAIL, PASSWORD).body<TokenResponse>()

        val refreshed = client.refresh(original.refreshToken)
        assertEquals(HttpStatusCode.OK, refreshed.status)
        assertNotEquals(original.refreshToken, refreshed.body<TokenResponse>().refreshToken)
    }

    @Test
    fun `a rotated token stops working`() = withAuthServer { client ->
        val original = client.register(EMAIL, PASSWORD).body<TokenResponse>()
        client.refresh(original.refreshToken)

        val reused = client.refresh(original.refreshToken)
        assertEquals(HttpStatusCode.Unauthorized, reused.status)
        assertEquals("invalid_refresh_token", reused.errorCode())
    }

    /**
     * The security-critical case: presenting a retired token means it leaked,
     * so the replacement issued in its place must die with it.
     */
    @Test
    fun `reusing a rotated token revokes the whole family`() = withAuthServer { client ->
        val original = client.register(EMAIL, PASSWORD).body<TokenResponse>()
        val rotated = client.refresh(original.refreshToken).body<TokenResponse>()

        client.refresh(original.refreshToken)

        val afterReuse = client.refresh(rotated.refreshToken)
        assertEquals(HttpStatusCode.Unauthorized, afterReuse.status)
    }

    @Test
    fun `an unknown refresh token is rejected`() = withAuthServer { client ->
        val response = client.refresh("not-a-real-token")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("invalid_refresh_token", response.errorCode())
    }

    @Test
    fun `logging out retires the refresh token`() = withAuthServer { client ->
        val tokens = client.register(EMAIL, PASSWORD).body<TokenResponse>()

        assertEquals(HttpStatusCode.NoContent, client.logout(tokens.refreshToken).status)
        assertEquals(HttpStatusCode.Unauthorized, client.refresh(tokens.refreshToken).status)
    }

    @Test
    fun `logging out twice is not an error`() = withAuthServer { client ->
        val tokens = client.register(EMAIL, PASSWORD).body<TokenResponse>()

        client.logout(tokens.refreshToken)
        assertEquals(HttpStatusCode.NoContent, client.logout(tokens.refreshToken).status)
    }

    @Test
    fun `me returns the signed-in user`() = withAuthServer { client ->
        val tokens = client.register(EMAIL, PASSWORD).body<TokenResponse>()

        val response = client.get("/auth/me") { bearerAuth(tokens.accessToken) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(EMAIL, response.body<PublicUser>().email)
    }

    @Test
    fun `me refuses a request with no token`() = withAuthServer { client ->
        val response = client.get("/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("unauthorized", response.errorCode())
    }

    @Test
    fun `me refuses a token that is not a token`() = withAuthServer { client ->
        val response = client.get("/auth/me") { bearerAuth("not.a.jwt") }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `me refuses a token signed with the wrong secret`() = withAuthServer { client ->
        // Valid JWT structure, signed by something else.
        val foreign = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        val response = client.get("/auth/me") { bearerAuth(foreign) }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `an unreadable body is the caller's error`() = withAuthServer { client ->
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"email\":")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("invalid_request", response.errorCode())
    }

    private suspend fun HttpClient.register(email: String, password: String) =
        post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password))
        }

    private suspend fun HttpClient.login(email: String, password: String) =
        post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

    private suspend fun HttpClient.refresh(token: String) =
        post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(token))
        }

    private suspend fun HttpClient.logout(token: String) =
        post("/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(LogoutRequest(token))
        }

    private suspend fun HttpResponse.errorCode(): String = body<ErrorResponse>().error

    private companion object {
        const val EMAIL = "person@example.com"
        const val PASSWORD = "correct-horse-battery"
    }
}
