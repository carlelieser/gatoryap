// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Argon2PasswordHasherTest {

    // Cheap parameters: these tests prove the wiring, not the cost setting.
    private val hasher = Argon2PasswordHasher(Argon2Params(memoryKib = 256, iterations = 1))

    @Test
    fun `accepts the password it hashed`() {
        val encoded = hasher.hash(PASSWORD)
        assertTrue(hasher.verify(PASSWORD, encoded))
    }

    @Test
    fun `rejects a wrong password`() {
        val encoded = hasher.hash(PASSWORD)
        assertFalse(hasher.verify("not-the-password", encoded))
    }

    @Test
    fun `rejects a password differing only in case`() {
        val encoded = hasher.hash(PASSWORD)
        assertFalse(hasher.verify(PASSWORD.uppercase(), encoded))
    }

    @Test
    fun `salts every hash separately`() {
        assertNotEquals(hasher.hash(PASSWORD), hasher.hash(PASSWORD))
    }

    @Test
    fun `rejects a hash it cannot parse`() {
        assertFalse(hasher.verify(PASSWORD, "not-a-phc-string"))
    }

    @Test
    fun `rejects a truncated hash`() {
        val encoded = hasher.hash(PASSWORD)
        assertFalse(hasher.verify(PASSWORD, encoded.dropLast(8)))
    }

    @Test
    fun `produces a phc string naming argon2id`() {
        assertTrue(hasher.hash(PASSWORD).startsWith("\$argon2id\$v=19\$"))
    }

    @Test
    fun `verifies against parameters stored in the hash, not current defaults`() {
        val weakerHasher = Argon2PasswordHasher(Argon2Params(memoryKib = 128, iterations = 1))
        val encoded = weakerHasher.hash(PASSWORD)

        // A hasher configured differently must still read the older hash.
        assertTrue(hasher.verify(PASSWORD, encoded))
    }

    private companion object {
        const val PASSWORD = "correct-horse-battery-staple"
    }
}
