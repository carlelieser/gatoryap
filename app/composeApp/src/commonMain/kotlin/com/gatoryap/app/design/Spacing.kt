// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The spacing ramp used across the identity board — a 4dp base, doubling through
 * the mid range. Reach for these instead of literal dp values so screens stay on
 * the same rhythm.
 */
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 40.dp,
    val huge: Dp = 72.dp,
)

/**
 * Read via `GatorYapTheme.spacing`. Static because the ramp never changes at
 * runtime — a reads-cause-recomposition local would cost more than it buys.
 */
internal val LocalSpacing = staticCompositionLocalOf { Spacing() }
