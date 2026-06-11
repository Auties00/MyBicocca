package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.library.LibraryCrowd

private val OccupancyLow = Color(0xFF2E9E55)
private val OccupancyMid = Color(0xFFE0A100)
private val OccupancyHigh = Color(0xFFD3302F)

/**
 * Status green/amber/red for the occupancy indicators — constant rather than theme-derived so
 * "how busy" reads the same in light and dark.
 */
fun occupancyColor(percentage: Int): Color = when {
    percentage < 40 -> OccupancyLow
    percentage < 75 -> OccupancyMid
    else -> OccupancyHigh
}

@Composable
fun occupancyLabel(percentage: Int): String = when {
    percentage < 40 -> stringResource(R.string.library_crowded_low)
    percentage < 75 -> stringResource(R.string.library_crowded_mid)
    else -> stringResource(R.string.library_crowded_high)
}

@Composable
fun crowdLabel(crowd: LibraryCrowd): String = when (crowd) {
    LibraryCrowd.Fluid -> stringResource(R.string.library_crowded_low)
    LibraryCrowd.Moderate -> stringResource(R.string.library_crowded_mid)
    LibraryCrowd.Dense -> stringResource(R.string.library_crowded_high)
}

/** Formats a duration as "30 min", "1 h" or "1 h 30". */
@Composable
fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> stringResource(R.string.library_duration_minutes_only, rest)
        rest == 0 -> stringResource(R.string.library_duration_hours_only, hours)
        else -> stringResource(R.string.library_duration_hours_minutes, hours, rest)
    }
}

/** Decodes a check-in QR delivered as a "data:image/png;base64,…" data URL. */
fun decodeQrDataUrl(dataUrl: String): ImageBitmap? = runCatching {
    val encoded = dataUrl.substringAfter("base64,", missingDelimiterValue = "")
        .takeIf { it.isNotBlank() } ?: return null
    val bytes = Base64.decode(encoded, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
