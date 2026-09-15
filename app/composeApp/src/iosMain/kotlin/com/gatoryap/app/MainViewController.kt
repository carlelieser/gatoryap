// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point. Consumed from Swift as MainViewControllerKt.MainViewController().
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
