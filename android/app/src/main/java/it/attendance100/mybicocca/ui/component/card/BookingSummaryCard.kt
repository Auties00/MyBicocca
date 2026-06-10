package it.attendance100.mybicocca.ui.component.card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayOfWeekFormat = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)

/** "Oggi" / "Domani" for the next two days, otherwise the capitalized Italian weekday ("Gio"). */
fun relativeDayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Oggi"
        today.plusDays(1) -> "Domani"
        else -> date.format(DayOfWeekFormat).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
}

/** A footer entry (icon + label) for [BookingSummaryCard]'s tonal strip; [muted] drops it to the outline tint. */
data class BookingFooterEntry(
    val icon: ImageVector,
    val label: String,
    val muted: Boolean = false,
)

/**
 * Booking list card shared by the exam, segreterie and biblioteca lists, so every booking reads
 * as the same object: a leading date tile, title/subtitle, and a tonal "when & where" footer
 * strip holding up to two [BookingFooterEntry] items pushed to opposite edges. Matches the
 * booked-exam (Appelli) card. Press feedback is expressive — the corners tighten while the
 * finger is down.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookingSummaryCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    dayOfMonth: String? = null,
    month: String? = null,
    footer: List<BookingFooterEntry> = emptyList(),
) {
    val scheme = MaterialTheme.colorScheme

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (pressed) 12.dp else 24.dp,
        label = "bookingCardCorner",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(corner),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                if (dayOfMonth != null && month != null) {
                    DateTile(dayOfMonth = dayOfMonth, month = month)
                    Spacer(Modifier.width(14.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (footer.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    footer.firstOrNull()?.let { FooterEntry(it) }
                    Box(modifier = Modifier.weight(1f))
                    footer.getOrNull(1)?.let { FooterEntry(it) }
                }
            }
        }
    }
}

@Composable
private fun DateTile(dayOfMonth: String, month: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfMonth,
                color = scheme.onPrimaryContainer,
                fontSize = 15.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = month,
                color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontSize = 8.sp,
                lineHeight = 9.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FooterEntry(entry: BookingFooterEntry) {
    val scheme = MaterialTheme.colorScheme
    val tint = if (entry.muted) scheme.outline else scheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(entry.icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            text = entry.label,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
