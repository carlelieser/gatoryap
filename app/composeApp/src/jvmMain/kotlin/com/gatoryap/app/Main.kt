// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Desktop entry point. Exists as the Compose Hot Reload sandbox for the shared
 * UI — it is not a shipped target.
 */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "GatorYap") {
        App()
    }
}
