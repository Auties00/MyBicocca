package it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.ui.screen.calendar.ext.durationMinutes
import it.attendance100.mybicocca.ui.screen.calendar.ext.formatTimeRange
import it.attendance100.mybicocca.ui.screen.calendar.ext.isInProgress
import it.attendance100.mybicocca.ui.screen.calendar.ext.locationLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.minutesRemaining
import it.attendance100.mybicocca.ui.screen.calendar.ext.peopleLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.CourseEditionPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: CalendarEvent,
    elearningCourses: List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onDismiss: () -> Unit,
) {
    val cancelled = event.status == EventStatus.CANCELLED
    val scheme = MaterialTheme.colorScheme

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modalColor = scheme.surfaceContainerLow,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (cancelled) scheme.onSurfaceVariant else scheme.onSurface,
                textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
            )
            EventDetailContent(
                event = event,
                elearningCourses = elearningCourses,
                onOpenCourse = onOpenCourse,
            )
        }
    }
}

@Composable
fun EventDetailContent(
    event: CalendarEvent,
    elearningCourses: List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by rememberCurrentTime()
    val cancelled = event.status == EventStatus.CANCELLED
    val inProgress = event.isInProgress(now)
    val context = LocalContext.current
    var showEditionPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (cancelled || inProgress) {
            Spacer(Modifier.height(12.dp))
            StatusChip(cancelled = cancelled, inProgress = inProgress, minutesLeft = event.minutesRemaining(now))
        }
        Spacer(Modifier.height(24.dp))

        // Collected up front so each card knows whether it caps the segmented group.
        val rows = buildList {
            add(
                Triple(
                    if (event is CalendarEvent.Exam) Icons.Outlined.Quiz else Icons.Outlined.School,
                    "ATTIVITÀ",
                    activityLabel(event),
                ),
            )
            add(Triple(Icons.Outlined.Schedule, "ORARIO", orarioValue(event)))
            event.locationLine().takeIf { it.isNotBlank() }?.let {
                add(Triple(Icons.Outlined.LocationOn, "LUOGO", it))
            }
            event.peopleLine().takeIf { it.isNotBlank() }?.let {
                add(Triple(Icons.Outlined.Person, if (event is CalendarEvent.Exam) "COMMISSIONE" else "DOCENTE", it))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            rows.forEachIndexed { index, (icon, label, value) ->
                IconRowCard(
                    icon = icon,
                    label = label,
                    value = value,
                    isFirst = index == 0,
                    isLast = index == rows.lastIndex,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        ActionRow(
            onOpenCourse = elearningCourses.takeIf { it.isNotEmpty() }?.let { courses ->
                {
                    // One edition opens straight away; multiple hand off to the picker.
                    courses.singleOrNull()?.let { onOpenCourse(it.id) }
                        ?: run { showEditionPicker = true }
                }
            },
            onMap = {
                val label = event.locationLine().takeIf { it.isNotBlank() }
                val url = event.location?.mapsUrl
                    ?.let { mapsUrlToGeoUri(it, label) }
                    ?: label?.let { "geo:0,0?q=${Uri.encode(it)}" }
                if (url != null) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }
            },
        )
    }

    if (showEditionPicker) {
        CourseEditionPickerSheet(
            courses = elearningCourses,
            onPick = { courseId ->
                showEditionPicker = false
                onOpenCourse(courseId)
            },
            onDismiss = { showEditionPicker = false },
        )
    }
}

@Composable
private fun StatusChip(cancelled: Boolean, inProgress: Boolean, minutesLeft: Int) {
    val scheme = MaterialTheme.colorScheme
    val (label, fg, bg) = when {
        cancelled -> Triple("Annullato", scheme.onErrorContainer, scheme.errorContainer)
        inProgress -> Triple("In corso · $minutesLeft min", scheme.onTertiaryContainer, scheme.tertiaryContainer)
        else -> Triple("", scheme.onSurface, scheme.surfaceContainerHigh)
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = fg,
        ),
        border = null,
    )
}

// List flavor of the segmented M3E group language used by the map sheets: 28dp corners cap the
// group's outer edges, 6dp where rows touch.
@Composable
private fun IconRowCard(icon: ImageVector, label: String, value: String, isFirst: Boolean, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 28.dp else 6.dp,
        topEnd = if (isFirst) 28.dp else 6.dp,
        bottomStart = if (isLast) 28.dp else 6.dp,
        bottomEnd = if (isLast) 28.dp else 6.dp,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = scheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

// "Apri corso" is the primary trailing action when the event's course exists in elearning, with
// "Mappa" as the tonal leading action; without a course, "Mappa" takes the primary slot alone.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(onOpenCourse: (() -> Unit)?, onMap: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val primaryBg = if (dark) scheme.primaryContainer else scheme.primary
    val primaryFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    if (onOpenCourse == null) {
        Button(
            onClick = onMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBg,
                contentColor = primaryFg,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Mappa", fontWeight = FontWeight.SemiBold)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onMap,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Mappa", fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onOpenCourse,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBg,
                contentColor = primaryFg,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Apri corso", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun activityLabel(event: CalendarEvent): String = when (event) {
    is CalendarEvent.Lesson -> "Lezione"
    is CalendarEvent.Exam -> {
        val type = event.examTypeLabel?.takeIf { it.isNotBlank() }
        if (type != null) "Esame · $type" else "Esame"
    }
}

private fun orarioValue(event: CalendarEvent): String {
    val durMin = event.durationMinutes()
    val durLabel = when {
        durMin == 0 -> "istantaneo"
        durMin < 60 -> "${durMin}min"
        durMin % 60 == 0 -> "${durMin / 60}h"
        else -> "${durMin / 60}h ${durMin % 60}min"
    }
    return "${event.formatTimeRange()} · $durLabel"
}

private val MapsEmbedCoordRegex = Regex("!2d(-?\\d+\\.?\\d*)!3d(-?\\d+\\.?\\d*)")

private fun mapsUrlToGeoUri(rawUrl: String, label: String?): String {
    val match = MapsEmbedCoordRegex.find(rawUrl) ?: return rawUrl
    val lng = match.groupValues[1]
    val lat = match.groupValues[2]
    val labelPart = label?.takeIf { it.isNotBlank() }?.let { "(${Uri.encode(it)})" }.orEmpty()
    return "geo:$lat,$lng?q=$lat,$lng$labelPart"
}
