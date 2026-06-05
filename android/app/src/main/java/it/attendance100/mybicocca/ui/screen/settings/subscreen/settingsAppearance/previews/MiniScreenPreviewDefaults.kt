package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Preview canvas sizes (dp) shared by the @Preview annotations in this package
const val PhonePreviewW = 120
const val PhonePreviewH = 200
const val TabletPreviewW = 350
const val TabletPreviewH = 190

/** Pill shape used for the mock floating action buttons */
val FabShape = RoundedCornerShape(35)

/** Rounded container for the day/week/month segmented control */
val SegmentedControlShape = RoundedCornerShape(40)

/** Top-rounded corners for the bottom sheets that peek up from the screen edge */
val SheetTopShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)

/** Tiny rounded shape for the placeholder "text" bars */
fun bar(): RoundedCornerShape = RoundedCornerShape(2.dp)

/**
 * Per-Elearning course accent palette: each entry is `[background, foreground]`
 */
val CourseAccents = listOf(
    listOf(Color(0xFF2C3E31), Color(0xFF10B981)),
    listOf(Color(0xFF2A394D), Color(0xFF0EA5E9)),
    listOf(Color(0xFF57361D), Color(0xFFF59E0B)),
)

/**
 * Corner shape for a filter chip at [index] within a strip of [count] chips: the
 * outer corners of the first and last chip are rounded, the inner corners squared
 */
fun chipShape(index: Int, count: Int): RoundedCornerShape {
    val start = if (index == 0) 4.dp else 1.dp
    val end = if (index == count - 1) 4.dp else 1.dp
    return RoundedCornerShape(topStart = start, topEnd = end, bottomStart = start, bottomEnd = end)
}

/** Pseudo-random fractional width for placeholder bars, for visual variety */
fun fakeBarWidth(i: Int): Float = 1 - ((i + 1) / 8f) - (i % 2) * 0.4f

@Composable
fun BoxScope.MapPin(alignment: Alignment, padding: PaddingValues) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .align(alignment)
            .padding(padding)
            .size(9.dp)
            .background(scheme.primary, CircleShape),
    )
}
