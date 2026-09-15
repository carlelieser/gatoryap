// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.core.auth

/** Outcome of checking one credential field. */
sealed interface CredentialCheck {
    data object Valid : CredentialCheck

    data class Invalid(val problem: CredentialProblem) : CredentialCheck
}

/**
 * Codes are stable strings: the server puts them straight into its error
 * envelope and clients switch on them without parsing prose.
 */
enum class CredentialProblem(val code: String) {
    EmailBlank("email_blank"),
    EmailMalformed("email_malformed"),
    EmailTooLong("email_too_long"),
    PasswordTooShort("password_too_short"),
    PasswordTooLong("password_too_long"),
}

/**
 * Shared by the server and the clients so both agree on what is acceptable
 * before a request is made.
 */
object CredentialRules {

    /** RFC 5321 practical maximum. */
    const val EMAIL_MAX_LENGTH: Int = 254

    /** OWASP guidance favours length over composition rules. */
    const val PASSWORD_MIN_LENGTH: Int = 12

    /** Bounds the work the password hasher can be asked to do. */
    const val PASSWORD_MAX_LENGTH: Int = 1024

    fun checkEmail(email: String): CredentialCheck = when {
        email.isBlank() -> invalid(CredentialProblem.EmailBlank)
        email.length > EMAIL_MAX_LENGTH -> invalid(CredentialProblem.EmailTooLong)
        !isEmailShaped(email) -> invalid(CredentialProblem.EmailMalformed)
        else -> CredentialCheck.Valid
    }

    fun checkPassword(password: String): CredentialCheck = when {
        password.length < PASSWORD_MIN_LENGTH -> invalid(CredentialProblem.PasswordTooShort)
        password.length > PASSWORD_MAX_LENGTH -> invalid(CredentialProblem.PasswordTooLong)
        else -> CredentialCheck.Valid
    }

    /**
     * Deliberately permissive. Deliverability is proven by sending mail, not by
     * a regex, so this only rejects what cannot be an address at all.
     */
    private fun isEmailShaped(email: String): Boolean {
        val atIndex = email.indexOf('@')
        val hasSingleAt = atIndex > 0 && atIndex == email.lastIndexOf('@')
        if (!hasSingleAt) return false

        val domain = email.substring(atIndex + 1)
        val hasInteriorDot = domain.contains('.') && !domain.startsWith('.')
        return hasInteriorDot && !domain.endsWith('.')
    }

    private fun invalid(problem: CredentialProblem): CredentialCheck =
        CredentialCheck.Invalid(problem)
}

/**
 * Canonical form for storage and lookup, so the application and the database
 * agree on which addresses are the same.
 */
fun normalizeEmail(email: String): String = email.trim().lowercase()
