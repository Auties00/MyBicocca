package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.MapStyleOptions
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R

// Map id bound to the Bicocca cloud styles (light + dark) in the Cloud console. Comes from
// MAPS_MAP_ID in local.properties; blank means no cloud style — the map then falls back to the
// bundled legacy JSON styles below.
val BICOCCA_MAP_ID: String = BuildConfig.MAPS_MAP_ID

// Picks the Bicocca-branded Google Maps style by the same light/dark decision the app theme
// uses (isSystemInDarkTheme), so the map follows the rest of the UI.
// Null when a cloud map id is configured: cloud styling must not be combined with embedded JSON,
// and only cloud styles can color the road-detail "Surface" features that replace roads at zoom
// 17+ (default since Aug 2025) — with JSON, streets flip to Google's stock colors up close.
@Composable
fun rememberBicoccaMapStyle(): MapStyleOptions? {
    if (BICOCCA_MAP_ID.isNotEmpty()) return null
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (dark) R.raw.map_style_bicocca_dark else R.raw.map_style_bicocca_light,
        )
    }
}
