package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The assignment's lifecycle dates as connected segments under the status header, in
// the plan compiler's course-tile language: a leading day/month chip (accent-filled on
// the next milestone), the milestone name, and a knob that morphs checked once the
// date is behind us. Only renders the dates the teacher actually set.
@Composable
fun AssignmentTimeline(
    assignment: Assignment,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val entries = buildList {
        assignment.allowSubmissionsFrom?.let { add(TimelineEntry("Apertura", it)) }
        assignment.dueDate?.let { add(TimelineEntry("Scadenza", it)) }
        assignment.cutoffDate?.let { add(TimelineEntry("Chiusura", it)) }
    }
    if (entries.isEmpty()) return

    // The first entry still ahead of us is "where we are" in the lifecycle.
    val nextIndex = entries.indexOfFirst { it.at.isAfter(now) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        entries.forEachIndexed { index, entry ->
            DateTile(
                entry = entry,
                isNext = index == nextIndex,
                past = entry.at.isBefore(now),
                isLast = index == entries.lastIndex,
            )
        }
    }
}

@Composable
private fun DateTile(
    entry: TimelineEntry,
    isNext: Boolean,
    past: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentShape(isFirst = false, isLast = isLast),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DayChip(at = entry.at, highlighted = isNext)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${TimelineDateFmt.format(entry.at)} · ${TimelineTimeFmt.format(entry.at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            SegmentKnob(checked = past)
        }
    }
}

// The plan compiler's CFU chip, holding the calendar day instead: accent fill marks
// the next milestone in the lifecycle.
@Composable
private fun DayChip(at: Instant, highlighted: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val container = if (highlighted) scheme.primary else scheme.primaryContainer
    val content = if (highlighted) scheme.onPrimary else scheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(container, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DayFmt.format(at),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
            )
            Text(
                text = MonthFmt.format(at).uppercase(Locale.ITALIAN),
                fontSize = 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = content.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }
    }
}

private data class TimelineEntry(val label: String, val at: Instant)

private val DayFmt = DateTimeFormatter
    .ofPattern("d", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val MonthFmt = DateTimeFormatter
    .ofPattern("MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimelineDateFmt = DateTimeFormatter
    .ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimelineTimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
