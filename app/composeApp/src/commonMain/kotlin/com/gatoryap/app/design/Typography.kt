// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Display text: Outfit SemiBold, tracked tight. The identity board sets the
 * wordmark at -1.32sp on 44sp, which is -3% of the size at any scale.
 */
private val DisplayTracking: TextUnit = (-0.03).em

/** Labels are the one place the board tracks outward, at +1.2sp on 12sp. */
private val LabelTracking: TextUnit = 0.1.em

@Composable
internal fun gatorYapTypography(): Typography {
    val display = outfitFamily()
    val body = interFamily()
    return Typography(
        displayLarge = displayStyle(display, 57.sp, 64.sp),
        displayMedium = displayStyle(display, 45.sp, 52.sp),
        displaySmall = displayStyle(display, 36.sp, 44.sp),
        headlineLarge = displayStyle(display, 32.sp, 40.sp),
        headlineMedium = displayStyle(display, 28.sp, 36.sp),
        headlineSmall = displayStyle(display, 24.sp, 32.sp),
        titleLarge = displayStyle(display, 22.sp, 28.sp),
        titleMedium = bodyStyle(body, 16.sp, 24.sp).semiBold(),
        titleSmall = bodyStyle(body, 14.sp, 20.sp).semiBold(),
        bodyLarge = bodyStyle(body, 16.sp, 24.sp),
        bodyMedium = bodyStyle(body, 14.sp, 20.sp),
        bodySmall = bodyStyle(body, 13.sp, 18.sp),
        labelLarge = bodyStyle(body, 14.sp, 20.sp).semiBold(),
        labelMedium = labelStyle(body, 12.sp, 16.sp),
        labelSmall = labelStyle(body, 11.sp, 16.sp),
    )
}

private fun displayStyle(family: FontFamily, size: TextUnit, lineHeight: TextUnit) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.SemiBold,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = DisplayTracking,
)

private fun bodyStyle(family: FontFamily, size: TextUnit, lineHeight: TextUnit) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Normal,
    fontSize = size,
    lineHeight = lineHeight,
)

private fun TextStyle.semiBold() = copy(fontWeight = FontWeight.SemiBold)

private fun labelStyle(family: FontFamily, size: TextUnit, lineHeight: TextUnit) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.SemiBold,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = LabelTracking,
)
