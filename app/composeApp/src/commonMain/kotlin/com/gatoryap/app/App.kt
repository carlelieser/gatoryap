// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

/**
 * Shared UI root. Each platform entry point renders this.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface {}
    }
}
