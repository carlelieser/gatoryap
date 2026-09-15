// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import com.gatoryap.core.auth.CredentialProblem
import com.gatoryap.core.auth.TokenResponse

/**
 * Outcomes are values rather than exceptions: a taken email address or a wrong
 * password is an expected result of a signup form, not a failure of the server.
 */
sealed interface RegisterResult {
    data class Success(val tokens: TokenResponse) : RegisterResult

    data class InvalidCredentials(val problem: CredentialProblem) : RegisterResult

    data object EmailAlreadyRegistered : RegisterResult
}

sealed interface LoginResult {
    data class Success(val tokens: TokenResponse) : LoginResult

    /** Deliberately does not say whether the address or the password was wrong. */
    data object InvalidCredentials : LoginResult
}

sealed interface RefreshResult {
    data class Success(val tokens: TokenResponse) : RefreshResult

    /** Unknown, already rotated, or belonging to a user that no longer exists. */
    data object InvalidRefreshToken : RefreshResult

    data object ExpiredRefreshToken : RefreshResult
}

sealed interface LogoutResult {
    data object Success : LogoutResult

    data object UnknownRefreshToken : LogoutResult
}
