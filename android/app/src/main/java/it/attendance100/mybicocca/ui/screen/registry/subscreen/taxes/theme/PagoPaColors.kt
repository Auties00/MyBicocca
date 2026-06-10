package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * pagoPA brand blue, scoped to the taxes feature: it anchors the pagoPA-branded surfaces
 * (receipt stroke, pay action) so they read as pagoPA rather than Bicocca.
 */
val PagoPaColor = Color(0xFF006DCA)
val PagoPaSecondaryColor = Color(0xFF61E7FF)
private val PagoPaBackgroundDark = Color(0xFF0B1016)
private val PagoPaBackgroundLight = Color(0xFFB4D7FF)

/** Theme-aware wash behind the pagoPA receipt card. */
@Composable
fun pagoPaBackgroundColor(): Color =
    if (isSystemInDarkTheme()) PagoPaBackgroundDark else PagoPaBackgroundLight
