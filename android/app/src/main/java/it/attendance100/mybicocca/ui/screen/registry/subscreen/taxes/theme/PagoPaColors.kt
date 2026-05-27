package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// pagoPA brand palette, scoped to the taxes feature.
val PagoPaColor = Color(0xFF006DCA)
val PagoPaSecondaryColor = Color(0xFF61E7FF)
private val PagoPaBackgroundDark = Color(0xFF0B1016)
private val PagoPaBackgroundLight = Color(0xFFB4D7FF)

@Composable
fun pagoPaBackgroundColor(): Color =
    if (isSystemInDarkTheme()) PagoPaBackgroundDark else PagoPaBackgroundLight
