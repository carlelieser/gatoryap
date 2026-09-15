// SPDX-License-Identifier: AGPL-3.0-or-later
package com.gatoryap.server.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * The single seam for database access. Hikari runs with autocommit off, so
 * every statement needs an explicit transaction, and Exposed binds one to the
 * calling thread. Ktor handlers are suspending, so the blocking work moves to
 * the IO dispatcher rather than occupying an event-loop thread.
 *
 * Exposed's own [org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction]
 * accepts no dispatcher and would run on the caller's, which is why it is not
 * used here.
 */
suspend fun <T> dbQuery(block: JdbcTransaction.() -> T): T =
    withContext(Dispatchers.IO) { transaction(statement = block) }
