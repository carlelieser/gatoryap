// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * The app's theme. Wraps [MaterialTheme] so Material components inherit the
 * brand, and adds the spacing ramp Material has no slot for.
 */
@Composable
fun GatorYapTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) GatorYapDarkColors else GatorYapLightColors,
            typography = gatorYapTypography(),
            shapes = GatorYapShapes,
            content = content,
        )
    }
}

/**
 * Design system tokens that Material does not carry. Colors, typography and
 * shapes stay on [MaterialTheme].
 */
object GatorYapTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}
