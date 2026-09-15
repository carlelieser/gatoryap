// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.auth

import com.gatoryap.core.auth.CredentialCheck
import com.gatoryap.core.auth.CredentialRules
import com.gatoryap.core.auth.LoginRequest
import com.gatoryap.core.auth.PublicUser
import com.gatoryap.core.auth.RegisterRequest
import com.gatoryap.core.auth.TokenResponse
import com.gatoryap.core.auth.normalizeEmail
import com.gatoryap.server.config.AuthConfig
import com.gatoryap.server.db.NewRefreshToken
import com.gatoryap.server.db.NewUser
import com.gatoryap.server.db.RefreshTokenRecord
import com.gatoryap.server.db.RefreshTokenRepository
import com.gatoryap.server.db.UserRecord
import com.gatoryap.server.db.UserRepository
import java.util.UUID
import kotlin.time.Clock
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory

/** Everything [AuthService] collaborates with, named so it counts as one dependency. */
class AuthDependencies(
    val users: UserRepository,
    val refreshTokens: RefreshTokenRepository,
    val hasher: PasswordHasher,
    val tokenIssuer: TokenIssuer,
    val refreshTokenService: RefreshTokenService,
    val config: AuthConfig,
)

class AuthService(private val deps: AuthDependencies) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Verifying a password against this when the address is unknown keeps the
     * response time of a failed login independent of whether the account
     * exists. Without it, the difference is a reliable way to harvest
     * registered addresses.
     */
    private val absentUserHash: String by lazy { deps.hasher.hash(deps.refreshTokenService.generate().value) }

    suspend fun register(request: RegisterRequest): RegisterResult {
        val email = normalizeEmail(request.email)
        validate(email, request.password)?.let { return it }

        val hash = deps.hasher.hash(request.password)
        val user = insertUser(NewUser(email, hash)) ?: return RegisterResult.EmailAlreadyRegistered
        return RegisterResult.Success(issueTokens(user))
    }

    suspend fun login(request: LoginRequest): LoginResult {
        val email = normalizeEmail(request.email)
        val user = deps.users.findByEmail(email)

        val isPasswordValid = deps.hasher.verify(request.password, user?.passwordHash ?: absentUserHash)
        if (user == null || !isPasswordValid) return LoginResult.InvalidCredentials

        return LoginResult.Success(issueTokens(user))
    }

    suspend fun refresh(token: RawRefreshToken): RefreshResult {
        val stored = deps.refreshTokens.findByHash(deps.refreshTokenService.hash(token))
            ?: return RefreshResult.InvalidRefreshToken

        if (stored.isRevoked) return handleReuse(stored)
        if (stored.hasExpiredAt(Clock.System.now())) return RefreshResult.ExpiredRefreshToken

        val user = deps.users.findById(stored.userId) ?: return RefreshResult.InvalidRefreshToken
        return RefreshResult.Success(rotateTokens(stored.id, user))
    }

    suspend fun logout(token: RawRefreshToken): LogoutResult {
        val stored = deps.refreshTokens.findByHash(deps.refreshTokenService.hash(token))
            ?: return LogoutResult.UnknownRefreshToken

        deps.refreshTokens.revoke(stored.id)
        return LogoutResult.Success
    }

    /**
     * A retired token being presented again means it outlived its rotation:
     * either it leaked, or the holder replayed it. Which of the two is
     * indistinguishable here, so every live session for the user is ended.
     */
    private suspend fun handleReuse(stored: RefreshTokenRecord): RefreshResult {
        val revoked = deps.refreshTokens.revokeAllForUser(stored.userId)
        logger.warn(
            "Revoked {} refresh tokens for user {} after a retired token was presented",
            revoked,
            stored.userId,
        )
        return RefreshResult.InvalidRefreshToken
    }

    private fun validate(email: String, password: String): RegisterResult.InvalidCredentials? {
        val checks = listOf(CredentialRules.checkEmail(email), CredentialRules.checkPassword(password))
        val failure = checks.filterIsInstance<CredentialCheck.Invalid>().firstOrNull()
        return failure?.let { RegisterResult.InvalidCredentials(it.problem) }
    }

    private suspend fun insertUser(user: NewUser): UserRecord? = try {
        deps.users.insert(user)
    } catch (cause: ExposedSQLException) {
        if (cause.sqlState == UNIQUE_VIOLATION) {
            null
        } else {
            throw IllegalStateException("Failed to register user with email ${user.email}", cause)
        }
    }

    private suspend fun issueTokens(user: UserRecord): TokenResponse {
        val refreshToken = deps.refreshTokenService.generate()
        deps.refreshTokens.insert(newRefreshToken(user.id, refreshToken))
        return tokenResponse(user, refreshToken)
    }

    private suspend fun rotateTokens(previousId: UUID, user: UserRecord): TokenResponse {
        val refreshToken = deps.refreshTokenService.generate()
        deps.refreshTokens.rotate(previousId, newRefreshToken(user.id, refreshToken))
        return tokenResponse(user, refreshToken)
    }

    private fun newRefreshToken(userId: UUID, token: RawRefreshToken) = NewRefreshToken(
        userId = userId,
        tokenHash = deps.refreshTokenService.hash(token),
        expiresAt = Clock.System.now() + deps.config.refreshTokenTtl,
    )

    private fun tokenResponse(user: UserRecord, refreshToken: RawRefreshToken): TokenResponse {
        val accessToken = deps.tokenIssuer.issue(user)
        return TokenResponse(
            accessToken = accessToken.value,
            refreshToken = refreshToken.value,
            expiresAtEpochSeconds = accessToken.expiresAt.epochSeconds,
            user = user.toPublicUser(),
        )
    }

    private companion object {
        /** SQLSTATE for a unique constraint violation. */
        const val UNIQUE_VIOLATION = "23505"
    }
}

fun UserRecord.toPublicUser(): PublicUser = PublicUser(
    id = id.toString(),
    email = email,
    createdAtEpochSeconds = createdAt.epochSeconds,
)
