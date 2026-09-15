// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.routes

import com.gatoryap.core.auth.RegisterRequest
import com.gatoryap.core.auth.TokenResponse
import com.gatoryap.server.db.UsersTable
import com.gatoryap.server.withAuthServer
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DeletedUserTest {
    @Test
    fun `a token outliving its account stops working`() = withAuthServer { client ->
        val tokens = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("person@example.com", "correct-horse-battery"))
        }.body<TokenResponse>()

        assertEquals(HttpStatusCode.OK, client.get("/auth/me") { bearerAuth(tokens.accessToken) }.status)

        transaction { UsersTable.deleteAll() }

        // The JWT is still cryptographically valid; the account is gone.
        val afterDeletion = client.get("/auth/me") { bearerAuth(tokens.accessToken) }
        assertEquals(HttpStatusCode.Unauthorized, afterDeletion.status)
    }
}
