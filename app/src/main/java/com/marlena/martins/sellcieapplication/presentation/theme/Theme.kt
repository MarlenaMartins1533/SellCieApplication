package com.marlena.martins.sellcieapplication.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CieloColorScheme = lightColorScheme(
    primary = CieloBlue,
    onPrimary = CieloOnPrimary,
    primaryContainer = CieloSoftBlue,
    onPrimaryContainer = CieloNavy,
    secondary = CieloNavy,
    background = CieloBackground,
    surface = CieloOnPrimary
)

@Composable
fun CieloTicketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CieloColorScheme,
        content = content
    )
}
