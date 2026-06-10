package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.tone
import kotlin.math.roundToInt

/**
 * One segment of the connected course stack: glanceable status only — the full breakdown
 * lives in the detail page opened on tap. The leading badge carries the attendance
 * percentage on a fill that ramps red -> orange -> green as it grows ("?" when nothing has
 * been recorded yet); saturated ramp fills always carry white text, in dark mode too.
 */
@Composable
fun AttendanceCourseCard(
    course: CourseAttendance,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val status = course.classroomAttendance?.status
    val tone = status.tone()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val percent = course.attendancePercent()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        percent?.let { percentRampColor(it / 100f) }
                            ?: scheme.surfaceContainerHighest,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = percent?.let { "$it%" } ?: "?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (percent != null) Color.White else scheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = course.statusCaption(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (status != null) tone.accent else scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * The same percentage the caption leads with: classroom attendance first, then the
 * recorded-session percentage when only mod_attendance data exists.
 */
private fun CourseAttendance.attendancePercent(): Int? =
    classroomAttendance?.attendancePercentage?.roundToInt()
        ?: sessionAttendance.firstOrNull { it.recordedPercentage != null }
            ?.recordedPercentage?.roundToInt()

/** Continuous red -> orange -> green ramp following the percentage. */
private fun percentRampColor(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    val low = Color(0xFFD93025)
    val mid = Color(0xFFE8710A)
    val high = Color(0xFF1E8E3E)
    return if (f < 0.5f) lerp(low, mid, f / 0.5f) else lerp(mid, high, (f - 0.5f) / 0.5f)
}

@Composable
private fun CourseAttendance.statusCaption(): String {
    classroomAttendance?.let {
        return "${it.status.label()} · ${it.lessonsAttended} lezioni · ${it.attendancePercentage.roundToInt()}%"
    }
    sessionAttendance.firstOrNull { it.attendedSessions != null && it.totalSessions != null }?.let {
        return "${it.attendedSessions}/${it.totalSessions} sessioni" +
            (it.recordedPercentage?.let { pct -> " · ${pct.roundToInt()}% registrate" } ?: "")
    }
    return "Nessuna rilevazione presenze"
}
