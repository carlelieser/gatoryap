// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.core.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

/**
 * Times are epoch seconds rather than a datetime type: the wire format stays
 * explicit and clients pick their own representation.
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_at") val expiresAtEpochSeconds: Long,
    val user: PublicUser,
)

/** Everything safe to expose about a user. Never carries the password hash. */
@Serializable
data class PublicUser(
    val id: String,
    val email: String,
    @SerialName("created_at") val createdAtEpochSeconds: Long,
)
