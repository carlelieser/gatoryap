// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import java.util.Base64

/** Argon2id cost parameters. Encoded into every hash so they can be raised later. */
data class Argon2Params(
    val memoryKib: Int = DEFAULT_MEMORY_KIB,
    val iterations: Int = DEFAULT_ITERATIONS,
    val parallelism: Int = DEFAULT_PARALLELISM,
) {
    companion object {
        /** OWASP's recommended Argon2id settings: 19 MiB, two passes. */
        const val DEFAULT_MEMORY_KIB: Int = 19456
        const val DEFAULT_ITERATIONS: Int = 2
        const val DEFAULT_PARALLELISM: Int = 1
    }
}

/** A parsed PHC string: the stored parameters alongside the salt and digest. */
data class Argon2Hash(
    val params: Argon2Params,
    val salt: ByteArray,
    val digest: ByteArray,
) {
    // ByteArray identity would make two equal hashes compare unequal.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Argon2Hash) return false
        return params == other.params &&
            salt.contentEquals(other.salt) &&
            digest.contentEquals(other.digest)
    }

    override fun hashCode(): Int {
        var result = params.hashCode()
        result = 31 * result + salt.contentHashCode()
        return 31 * result + digest.contentHashCode()
    }
}

/**
 * The PHC string format, as used by the reference Argon2 implementation:
 * `$argon2id$v=19$m=19456,t=2,p=1$<salt>$<digest>`. BouncyCastle deals only in
 * raw bytes, so encoding lives here rather than in the hasher.
 */
object Argon2Phc {

    const val VERSION: Int = 19

    private const val ALGORITHM = "argon2id"
    private const val FIELD_COUNT = 6

    private val encoder: Base64.Encoder = Base64.getEncoder().withoutPadding()
    private val decoder: Base64.Decoder = Base64.getDecoder()

    fun encode(hash: Argon2Hash): String {
        val cost = "m=${hash.params.memoryKib},t=${hash.params.iterations},p=${hash.params.parallelism}"
        return "\$$ALGORITHM\$v=$VERSION\$$cost\$${encoder.encodeToString(hash.salt)}" +
            "\$${encoder.encodeToString(hash.digest)}"
    }

    /** Returns null for anything that is not a well-formed argon2id hash. */
    fun decode(encoded: String): Argon2Hash? {
        val fields = encoded.split('$')
        if (fields.size != FIELD_COUNT) return null
        if (fields[1] != ALGORITHM) return null
        if (fields[2] != "v=$VERSION") return null

        val params = parseCost(fields[3]) ?: return null
        return runCatching {
            Argon2Hash(params, decoder.decode(fields[4]), decoder.decode(fields[5]))
        }.getOrNull()
    }

    private fun parseCost(field: String): Argon2Params? {
        val values = field.split(',')
            .mapNotNull { part -> part.split('=').takeIf { it.size == 2 } }
            .associate { (key, value) -> key to value.toIntOrNull() }

        val memoryKib = values["m"] ?: return null
        val iterations = values["t"] ?: return null
        val parallelism = values["p"] ?: return null
        return Argon2Params(memoryKib, iterations, parallelism)
    }
}
