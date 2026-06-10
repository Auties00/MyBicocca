package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

fun occupancyLabel(percentage: Int): String = when {
    percentage < 40 -> "Poco affollata"
    percentage < 75 -> "Moderatamente affollata"
    else -> "Molto affollata"
}

fun crowdLabel(crowd: LibraryCrowd): String = when (crowd) {
    LibraryCrowd.Fluid -> "Poco affollata"
    LibraryCrowd.Moderate -> "Moderatamente affollata"
    LibraryCrowd.Dense -> "Molto affollata"
}

/** Formats a duration as "30 min", "1 h" or "1 h 30". */
fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest min"
        rest == 0 -> "$hours h"
        else -> "$hours h $rest"
    }
}

/** Decodes a check-in QR delivered as a "data:image/png;base64,…" data URL. */
fun decodeQrDataUrl(dataUrl: String): ImageBitmap? = runCatching {
    val encoded = dataUrl.substringAfter("base64,", missingDelimiterValue = "")
        .takeIf { it.isNotBlank() } ?: return null
    val bytes = Base64.decode(encoded, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
