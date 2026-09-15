// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.core.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class CredentialRulesTest {

    @Test
    fun `accepts an ordinary address`() {
        assertEquals(CredentialCheck.Valid, CredentialRules.checkEmail("person@example.com"))
    }

    @Test
    fun `rejects a blank address`() {
        assertProblem(CredentialProblem.EmailBlank, CredentialRules.checkEmail("   "))
    }

    @Test
    fun `rejects an address without a domain dot`() {
        assertProblem(CredentialProblem.EmailMalformed, CredentialRules.checkEmail("person@localhost"))
    }

    @Test
    fun `rejects an address with two at signs`() {
        assertProblem(CredentialProblem.EmailMalformed, CredentialRules.checkEmail("a@b@example.com"))
    }

    @Test
    fun `rejects an address with no local part`() {
        assertProblem(CredentialProblem.EmailMalformed, CredentialRules.checkEmail("@example.com"))
    }

    @Test
    fun `rejects an address with a trailing domain dot`() {
        assertProblem(CredentialProblem.EmailMalformed, CredentialRules.checkEmail("person@example."))
    }

    @Test
    fun `rejects an address beyond the length limit`() {
        val local = "a".repeat(CredentialRules.EMAIL_MAX_LENGTH)
        assertProblem(CredentialProblem.EmailTooLong, CredentialRules.checkEmail("$local@example.com"))
    }

    @Test
    fun `accepts a password at the minimum length`() {
        val password = "a".repeat(CredentialRules.PASSWORD_MIN_LENGTH)
        assertEquals(CredentialCheck.Valid, CredentialRules.checkPassword(password))
    }

    @Test
    fun `rejects a password one character short`() {
        val password = "a".repeat(CredentialRules.PASSWORD_MIN_LENGTH - 1)
        assertProblem(CredentialProblem.PasswordTooShort, CredentialRules.checkPassword(password))
    }

    @Test
    fun `rejects a password beyond the length limit`() {
        val password = "a".repeat(CredentialRules.PASSWORD_MAX_LENGTH + 1)
        assertProblem(CredentialProblem.PasswordTooLong, CredentialRules.checkPassword(password))
    }

    @Test
    fun `normalizing lowercases and trims`() {
        assertEquals("person@example.com", normalizeEmail("  Person@Example.COM  "))
    }

    private fun assertProblem(expected: CredentialProblem, actual: CredentialCheck) {
        assertEquals(CredentialCheck.Invalid(expected), actual)
    }
}
