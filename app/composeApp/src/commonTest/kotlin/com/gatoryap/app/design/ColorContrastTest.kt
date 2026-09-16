// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards the palette against WCAG AA regressions. The brand colors are fixed, so
 * the risk is in the pairings: a foreground reassigned to the wrong background
 * is invisible on device but silent at compile time.
 */
class ColorContrastTest {

    @Test
    fun lightSchemePairsMeetAa() = assertPairsMeetAa(GatorYapLightColors, "light")

    @Test
    fun darkSchemePairsMeetAa() = assertPairsMeetAa(GatorYapDarkColors, "dark")

    private fun assertPairsMeetAa(scheme: ColorScheme, schemeName: String) {
        val pairs = listOf(
            Triple("onBackground/background", scheme.onBackground, scheme.background),
            Triple("onSurface/surface", scheme.onSurface, scheme.surface),
            Triple("onSurfaceVariant/surface", scheme.onSurfaceVariant, scheme.surface),
            Triple("onPrimary/primary", scheme.onPrimary, scheme.primary),
            Triple("onSecondary/secondary", scheme.onSecondary, scheme.secondary),
            Triple("onTertiary/tertiary", scheme.onTertiary, scheme.tertiary),
            Triple("onPrimaryContainer/primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer),
            Triple("onSecondaryContainer/secondaryContainer", scheme.onSecondaryContainer, scheme.secondaryContainer),
        )
        for ((label, foreground, background) in pairs) {
            val ratio = contrastRatio(foreground, background)
            assertTrue(
                ratio >= AA_NORMAL_TEXT,
                "$schemeName $label contrast is $ratio, below WCAG AA $AA_NORMAL_TEXT",
            )
        }
    }
}

private const val AA_NORMAL_TEXT = 4.5

private fun contrastRatio(foreground: Color, background: Color): Double {
    val lighter = maxOf(relativeLuminance(foreground), relativeLuminance(background))
    val darker = minOf(relativeLuminance(foreground), relativeLuminance(background))
    return (lighter + 0.05) / (darker + 0.05)
}

/** Per WCAG 2.1 relative luminance. */
private fun relativeLuminance(color: Color): Double =
    0.2126 * channelLuminance(color.red) +
        0.7152 * channelLuminance(color.green) +
        0.0722 * channelLuminance(color.blue)

private fun channelLuminance(channel: Float): Double {
    val value = channel.toDouble()
    return if (value <= 0.03928) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
}
