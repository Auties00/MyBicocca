package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.courseAttendanceDetail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.StatusTone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.badgePolygon
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.tone
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseAttendanceDetailSheet(
    course: CourseAttendance,
    onDismiss: () -> Unit,
) {
    PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        val scheme = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        ) {
            val status = course.classroomAttendance?.status
            val tone = status.tone()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(status.badgePolygon().toShape())
                        .background(tone.container),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CoPresent,
                        contentDescription = null,
                        tint = tone.onContainer,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    course.teacherName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                course.code?.let { DetailRow("Codice", it) }
                course.year.takeIf { it != StudyYear.Unknown }?.let {
                    DetailRow("Anno di corso", "${it.value}°")
                }
                course.semester.label()?.let { DetailRow("Semestre", it) }
                course.credits.takeIf { it > 0f }?.let {
                    DetailRow("Crediti", "${if (it % 1f == 0f) it.toInt().toString() else it.toString()} CFU")
                }
            }

            val classroom = course.classroomAttendance
            val sessions = course.sessionAttendance
            if (classroom == null && sessions.isEmpty()) {
                SectionDivider()
                Text(
                    text = "Nessuna rilevazione presenze per questo corso.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                classroom?.let {
                    SectionDivider()
                    ClassroomSection(it)
                }
                sessions.forEach {
                    SectionDivider()
                    SessionSection(it)
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClassroomSection(classroom: ClassroomAttendance) {
    val tone = classroom.status.tone()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Frequenza in aula",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            StatusPill(text = classroom.status.label(), tone = tone)
        }
        LinearWavyProgressIndicator(
            progress = { (classroom.requirementProgressPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            DetailRow("Lezioni frequentate", classroom.lessonsAttended.toString())
            DetailRow("Ore svolte dal docente", classroom.hoursCompleted.toString())
            DetailRow("Presenza", "${classroom.attendancePercentage.roundToInt()}%")
            DetailRow("Requisito di frequenza", "${classroom.requirementProgressPercentage.roundToInt()}%")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionSection(session: SessionAttendance) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = session.label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        session.recordedPercentage?.let {
            LinearWavyProgressIndicator(
                progress = { (it / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            session.attendedSessions?.let { DetailRow("Sessioni frequentate", it.toString()) }
            session.totalSessions?.let { DetailRow("Sessioni totali", it.toString()) }
            session.recordedPercentage?.let {
                DetailRow("Presenza sulle registrate", "${it.roundToInt()}%")
            }
            session.overallPercentage?.let {
                DetailRow("Presenza sul totale", "${it.roundToInt()}%")
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, tone: StatusTone) {
    Box(
        modifier = Modifier
            .background(tone.container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone.onContainer,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}
