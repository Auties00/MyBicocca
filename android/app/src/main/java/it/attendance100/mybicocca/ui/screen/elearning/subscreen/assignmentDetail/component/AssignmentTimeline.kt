package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Vertical mini-timeline of the assignment's lifecycle dates. Only renders the dates the
// teacher actually set — real courses often configure just one of the three.
@Composable
fun AssignmentTimeline(
    assignment: Assignment,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val entries = buildList {
        assignment.allowSubmissionsFrom?.let { add(TimelineEntry("APERTURA", it)) }
        assignment.dueDate?.let { add(TimelineEntry("SCADENZA", it)) }
        assignment.cutoffDate?.let { add(TimelineEntry("CHIUSURA", it)) }
    }
    if (entries.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    // The first entry still ahead of us is "where we are" in the lifecycle.
    val nextIndex = entries.indexOfFirst { it.at.isAfter(now) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            entries.forEachIndexed { index, entry ->
                val isNext = index == nextIndex
                val past = entry.at.isBefore(now)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier.width(18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isNext) 16.dp else 10.dp)
                                .background(
                                    color = when {
                                        isNext -> scheme.tertiary
                                        past -> scheme.outlineVariant
                                        else -> scheme.primary.copy(alpha = 0.45f)
                                    },
                                    shape = if (isNext) OrganicShapes.Sunny else OrganicShapes.SmoothCookie6,
                                ),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.label,
                            color = if (isNext) scheme.tertiary else scheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.6.sp,
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = "${TimelineDateFmt.format(entry.at)} · ${TimelineTimeFmt.format(entry.at)}",
                            color = if (past && !isNext) scheme.onSurfaceVariant else scheme.onSurface,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 14.sp,
                            letterSpacing = (-0.2).sp,
                        )
                    }
                }
                if (index < entries.lastIndex) {
                    Row {
                        Box(
                            modifier = Modifier.width(18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(18.dp)
                                    .background(scheme.outlineVariant, RoundedCornerShape(1.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TimelineEntry(val label: String, val at: Instant)

private val TimelineDateFmt = DateTimeFormatter
    .ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimelineTimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
