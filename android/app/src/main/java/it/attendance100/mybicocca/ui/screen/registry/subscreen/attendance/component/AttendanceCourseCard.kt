package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.badgePolygon
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.headlinePercentage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.tone
import kotlin.math.roundToInt

// One segment of the connected course stack: glanceable status only — the full
// breakdown lives in the detail sheet opened on tap.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val percentage = course.headlinePercentage()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(status.badgePolygon().toShape())
                        .background(tone.container),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CoPresent,
                        contentDescription = null,
                        tint = tone.onContainer,
                        modifier = Modifier.size(22.dp),
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
                    course.teacherName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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

            percentage?.let {
                Spacer(Modifier.height(12.dp))
                LinearWavyProgressIndicator(
                    progress = { (it / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

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
