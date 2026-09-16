// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.gatoryap.app.design.GatorYapTheme

/**
 * Shared UI root. Each platform entry point renders this.
 */
@Composable
fun App() {
    GatorYapTheme {
        Surface {}
    }
}
