// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.routes

import com.gatoryap.core.auth.LoginRequest
import com.gatoryap.core.auth.RefreshRequest
import com.gatoryap.server.plugins.ErrorResponse
import com.gatoryap.server.withAuthServer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRateLimitTest {

    @Test
    fun `refuses further attempts once the limit is reached`() = withAuthServer(LIMIT_OF_THREE) { client ->
        repeat(3) { client.attemptLogin() }

        val blocked = client.attemptLogin()
        assertEquals(HttpStatusCode.TooManyRequests, blocked.status)
    }

    @Test
    fun `answers a blocked attempt in the usual error shape`() = withAuthServer(LIMIT_OF_THREE) { client ->
        repeat(4) { client.attemptLogin() }

        val blocked = client.attemptLogin()
        assertEquals("too_many_requests", blocked.body<ErrorResponse>().error)
    }

    @Test
    fun `allows attempts up to the limit`() = withAuthServer(LIMIT_OF_THREE) { client ->
        val statuses = (1..3).map { client.attemptLogin().status }
        assertTrue(statuses.none { it == HttpStatusCode.TooManyRequests })
    }

    /** Refreshing presents a token the caller already holds, so it is not limited. */
    @Test
    fun `does not limit refreshing`() = withAuthServer(LIMIT_OF_THREE) { client ->
        repeat(6) {
            val response = client.post("/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest("not-a-real-token"))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    private suspend fun HttpClient.attemptLogin() = post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest("person@example.com", "not-the-password"))
    }

    private companion object {
        val LIMIT_OF_THREE = mapOf(
            "AUTH_RATE_LIMIT_ATTEMPTS" to "3",
            "AUTH_RATE_LIMIT_WINDOW_MINUTES" to "5",
        )
    }
}
