// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

interface PasswordHasher {
    fun hash(password: String): String

    /** False for a wrong password and for a hash this hasher cannot read. */
    fun verify(password: String, encodedHash: String): Boolean
}

/**
 * Argon2id through BouncyCastle, which is pure Java — no native library has to
 * be present in the deployment image, unlike the JNA-based Argon2 wrappers.
 *
 * Verification uses the parameters stored in the hash rather than the current
 * defaults, so raising the cost does not lock out existing users.
 */
class Argon2PasswordHasher(
    private val params: Argon2Params = Argon2Params(),
) : PasswordHasher {

    private val random = SecureRandom()

    override fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).also(random::nextBytes)
        val digest = derive(password, salt, params)
        return Argon2Phc.encode(Argon2Hash(params, salt, digest))
    }

    override fun verify(password: String, encodedHash: String): Boolean {
        val stored = Argon2Phc.decode(encodedHash) ?: return false
        val candidate = derive(password, stored.salt, stored.params)
        // Constant-time: a byte-by-byte comparison would leak how much of the
        // digest matched.
        return MessageDigest.isEqual(candidate, stored.digest)
    }

    private fun derive(password: String, salt: ByteArray, cost: Argon2Params): ByteArray {
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(salt)
            .withMemoryAsKB(cost.memoryKib)
            .withIterations(cost.iterations)
            .withParallelism(cost.parallelism)
            .build()

        val generator = Argon2BytesGenerator().apply { init(parameters) }
        val digest = ByteArray(DIGEST_LENGTH)
        generator.generateBytes(password.toCharArray(), digest)
        return digest
    }

    private companion object {
        const val SALT_LENGTH = 16
        const val DIGEST_LENGTH = 32
    }
}
