// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** A refresh token in the form the client holds it. Never stored. */
@JvmInline
value class RawRefreshToken(val value: String)

/**
 * Generates opaque refresh tokens and reduces them to the form kept in the
 * database.
 *
 * Storage uses SHA-256 rather than a password hash. The token is 256 bits of
 * [SecureRandom] output, so there is no small guess space for a slow hash to
 * defend; it only needs to be irreversible, which SHA-256 is. Being
 * deterministic also lets the hash serve as the lookup key.
 */
class RefreshTokenService {

    private val random = SecureRandom()
    private val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    fun generate(): RawRefreshToken {
        val bytes = ByteArray(TOKEN_BYTES).also(random::nextBytes)
        return RawRefreshToken(encoder.encodeToString(bytes))
    }

    fun hash(token: RawRefreshToken): String =
        MessageDigest.getInstance(DIGEST_ALGORITHM)
            .digest(token.value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        const val TOKEN_BYTES = 32
        const val DIGEST_ALGORITHM = "SHA-256"
    }
}
