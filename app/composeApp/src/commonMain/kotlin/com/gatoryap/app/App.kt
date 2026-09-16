// SPDX-License-Identifier: Apache-2.0
package com.gatoryap.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gatoryap.app.design.GatorYapTheme

/**
 * Shared UI root. Each platform entry point renders this.
 */
@Composable
fun App() {
    GatorYapTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(GatorYapTheme.spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "GatorYap",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Hot reload is live.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = GatorYapTheme.spacing.small),
                )
            }
        }
    }
}
