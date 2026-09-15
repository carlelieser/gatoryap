// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RefreshTokenServiceTest {

    private val service = RefreshTokenService()

    @Test
    fun `never generates the same token twice`() {
        val tokens = List(TOKEN_SAMPLE_SIZE) { service.generate().value }
        assertEquals(TOKEN_SAMPLE_SIZE, tokens.toSet().size)
    }

    @Test
    fun `generates tokens carrying 256 bits of entropy`() {
        // 32 bytes as unpadded base64url.
        assertEquals(43, service.generate().value.length)
    }

    @Test
    fun `generates url-safe tokens`() {
        val token = service.generate().value
        assertTrue(token.none { it == '+' || it == '/' || it == '=' })
    }

    @Test
    fun `hashes to a stable value`() {
        val token = service.generate()
        assertEquals(service.hash(token), service.hash(token))
    }

    @Test
    fun `hashes different tokens differently`() {
        assertNotEquals(service.hash(service.generate()), service.hash(service.generate()))
    }

    @Test
    fun `hashes to 64 hex characters`() {
        val hash = service.hash(service.generate())
        assertEquals(64, hash.length)
        assertTrue(hash.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `hash does not contain the token`() {
        val token = service.generate()
        assertTrue(!service.hash(token).contains(token.value))
    }

    @Test
    fun `matches the known SHA-256 of a fixed input`() {
        val known = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertEquals(known, service.hash(RawRefreshToken("")))
    }

    private companion object {
        const val TOKEN_SAMPLE_SIZE = 500
    }
}
