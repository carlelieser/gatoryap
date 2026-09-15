// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import java.util.UUID

/**
 * The caller behind an authenticated request, built from the access token.
 *
 * Ktor's `Principal` marker is deprecated in 3.x — `call.principal<T>()` is
 * reified over any type — so this is a plain data class.
 */
data class AuthenticatedUser(
    val id: UUID,
    val email: String,
)
