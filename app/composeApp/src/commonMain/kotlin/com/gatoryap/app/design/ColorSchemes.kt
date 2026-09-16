// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** One step above the Gator Ink background, so cards read as raised. */
private val SurfaceRaised = Color(0xFF163026)

/** Muted lifted toward Bone; the light-mode Muted fails contrast on Gator Ink. */
private val OutlineDark = Color(0xFF9DB0A8)

/**
 * Brand Muted darkened for body text. The board uses Muted for captions, but at
 * 3.9:1 on Bone it misses WCAG AA; this holds the same hue at 5.0:1.
 */
private val MutedText = Color(0xFF5A6B64)

/**
 * Light scheme. Mirrors the identity board: Deep Green carries primary surfaces,
 * Signal Green is the interactive accent, Bone is the page background.
 */
internal val GatorYapLightColors: ColorScheme = lightColorScheme(
    primary = BrandColors.DeepGreen,
    onPrimary = BrandColors.Bone,
    primaryContainer = BrandColors.SignalGreen,
    onPrimaryContainer = BrandColors.GatorInk,
    secondary = BrandColors.SignalGreen,
    onSecondary = BrandColors.GatorInk,
    secondaryContainer = BrandColors.YapLime,
    onSecondaryContainer = BrandColors.GatorInk,
    tertiary = BrandColors.YapLime,
    onTertiary = BrandColors.GatorInk,
    background = BrandColors.Bone,
    onBackground = BrandColors.GatorInk,
    surface = BrandColors.Surface,
    onSurface = BrandColors.GatorInk,
    surfaceVariant = BrandColors.Bone,
    onSurfaceVariant = MutedText,
    outline = BrandColors.Muted,
)

/**
 * Dark scheme. Derived rather than brand-specified — the identity board only
 * documents light surfaces. Gator Ink becomes the background and the lighter
 * greens move forward so text keeps its contrast against it.
 */
internal val GatorYapDarkColors: ColorScheme = darkColorScheme(
    primary = BrandColors.SignalGreen,
    onPrimary = BrandColors.GatorInk,
    primaryContainer = BrandColors.DeepGreen,
    onPrimaryContainer = BrandColors.Bone,
    secondary = BrandColors.YapLime,
    onSecondary = BrandColors.GatorInk,
    secondaryContainer = BrandColors.DeepGreen,
    onSecondaryContainer = BrandColors.Bone,
    tertiary = BrandColors.YapLime,
    onTertiary = BrandColors.GatorInk,
    background = BrandColors.GatorInk,
    onBackground = BrandColors.Bone,
    surface = SurfaceRaised,
    onSurface = BrandColors.Bone,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = OutlineDark,
    outline = OutlineDark,
)
