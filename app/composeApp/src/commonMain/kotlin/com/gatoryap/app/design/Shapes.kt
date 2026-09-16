// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii from the identity board: 4dp on small chrome, 12dp on tiles and
 * swatches, 16dp on the primary lockup panel.
 */
internal val GatorYapShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
