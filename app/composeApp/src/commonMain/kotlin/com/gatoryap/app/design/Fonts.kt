// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import gatoryap.app.composeapp.generated.resources.Res
import gatoryap.app.composeapp.generated.resources.inter_variable
import gatoryap.app.composeapp.generated.resources.outfit_variable
import org.jetbrains.compose.resources.Font

/**
 * Outfit — the display face, used for the wordmark and headings.
 *
 * Both files are variable fonts, so every weight resolves to the same resource
 * and the renderer interpolates. Declaring the weights explicitly lets
 * [FontWeight] lookups pick the right instance instead of synthesising bold.
 */
@Composable
internal fun outfitFamily(): FontFamily = FontFamily(
    Font(Res.font.outfit_variable, FontWeight.Normal),
    Font(Res.font.outfit_variable, FontWeight.Medium),
    Font(Res.font.outfit_variable, FontWeight.SemiBold),
    Font(Res.font.outfit_variable, FontWeight.Bold),
)

/** Inter — the body face, used for running text, labels and captions. */
@Composable
internal fun interFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_variable, FontWeight.Normal),
    Font(Res.font.inter_variable, FontWeight.Medium),
    Font(Res.font.inter_variable, FontWeight.SemiBold),
)
