package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.MapStyleOptions
import it.attendance100.mybicocca.R

// Picks the Bicocca-branded Google Maps style by the same light/dark decision the app theme
// uses (isSystemInDarkTheme), so the map follows the rest of the UI.
@Composable
fun rememberBicoccaMapStyle(): MapStyleOptions {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (dark) R.raw.map_style_bicocca_dark else R.raw.map_style_bicocca_light,
        )
    }
}
