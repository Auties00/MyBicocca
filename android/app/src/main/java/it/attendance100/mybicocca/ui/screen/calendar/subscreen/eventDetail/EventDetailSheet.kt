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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.ui.screen.calendar.CalendarTestTags
import it.attendance100.mybicocca.ui.screen.calendar.ext.durationMinutes
import it.attendance100.mybicocca.ui.screen.calendar.ext.formatTimeRange
import it.attendance100.mybicocca.ui.screen.calendar.ext.isPointInTime
import it.attendance100.mybicocca.ui.screen.calendar.ext.locationLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.peopleLine
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.CourseEditionPickerSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Modal bottom sheet showing one calendar event in full: the headline title with the kind
 * label under it — struck through and dimmed when the event is cancelled — above
 * [EventDetailContent]. Hosted in a predictive-back-aware sheet on the low surface tone,
 * scrolling internally when the content outgrows the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: CalendarEvent,
    elearningCourses: List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit,
    onOpenReservation: (CalendarEvent) -> Unit,
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
                modifier = Modifier.testTag(CalendarTestTags.EVENT_TITLE),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (cancelled) scheme.onSurfaceVariant else scheme.onSurface,
                textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = activityLabel(event),
                modifier = Modifier.testTag(CalendarTestTags.EVENT_ACTIVITY_LABEL),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
            EventDetailContent(
                event = event,
                elearningCourses = elearningCourses,
                onOpenCourse = onOpenCourse,
                onOpenAssignment = onOpenAssignment,
                onOpenReservation = onOpenReservation,
            )
        }
    }
}

/**
 * Event body shared by the detail sheet and the agenda's inline expansion: a segmented
 * group of icon info rows — time with a duration suffix, place, teacher, plus
 * kind-specific rows such as the exam booking, its cancellation window or the library
 * seat — followed by the action area. The rows are materialized up front so the first and
 * last know to cap the group's outer corners.
 *
 * The primary action follows the event kind: deadlines open the assignment;
 * reservation-backed events — booked exams, appointments, library seats — lead to their
 * managing sheet, where the booking can be inspected or cancelled; lessons open their
 * e-learning course when one matches, with the map demoted to the secondary slot. A
 * single matching course edition navigates directly, several hand off to
 * [CourseEditionPickerSheet]. Locations open externally through geo: URIs.
 */
@Composable
fun EventDetailContent(
    event: CalendarEvent,
    elearningCourses: List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit,
    onOpenReservation: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showEditionPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.testTag(CalendarTestTags.EVENT_CONTENT)) {
        Spacer(Modifier.height(24.dp))

        val rows = buildList {
            add(Triple(Icons.Outlined.Schedule, "ORARIO", orarioValue(event)))
            event.locationLine().takeIf { it.isNotBlank() }?.let {
                add(Triple(Icons.Outlined.LocationOn, "LUOGO", it))
            }
            event.peopleLine().takeIf { it.isNotBlank() }?.let {
                add(Triple(Icons.Outlined.Person, "DOCENTE", it))
            }
            if (event is CalendarEvent.Exam) {
                bookingLine(event)?.let {
                    add(Triple(Icons.Outlined.ConfirmationNumber, "PRENOTAZIONE", it))
                }
                event.cancellableUntil?.let {
                    add(Triple(Icons.Outlined.EventBusy, "CANCELLABILE", "Entro il ${it.formatItalian()}"))
                }
            }
            if (event is CalendarEvent.LibraryReservation) {
                add(Triple(Icons.Outlined.Chair, "POSTO", event.seatName))
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

        val openMap: (() -> Unit)? = event.locationLine().takeIf { it.isNotBlank() }?.let { label ->
            {
                val url = event.location?.mapsUrl
                    ?.let { mapsUrlToGeoUri(it, label) }
                    ?: "geo:0,0?q=${Uri.encode(label)}"
                context.openExternal(url)
            }
        }
        val openCourse: (() -> Unit)? = elearningCourses.takeIf { it.isNotEmpty() }?.let { courses ->
            {
                courses.singleOrNull()?.let { onOpenCourse(it.id) }
                    ?: run { showEditionPicker = true }
            }
        }

        val goToReservation = EventAction(
            icon = Icons.Outlined.ConfirmationNumber,
            label = "Vai alla prenotazione",
            onClick = { onOpenReservation(event) },
        )
        val (primary, secondary) = when (event) {
            is CalendarEvent.AssignmentDeadline -> EventAction(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                label = "Apri compito",
                onClick = { onOpenAssignment(event.assignmentId, event.courseId) },
            ) to null
            is CalendarEvent.Exam,
            is CalendarEvent.LibraryReservation,
            -> goToReservation to null
            is CalendarEvent.Appointment ->
                goToReservation to openMap?.let { EventAction(Icons.Outlined.LocationOn, "Mappa", it) }
            is CalendarEvent.Lesson -> {
                val map = openMap?.let { EventAction(Icons.Outlined.LocationOn, "Mappa", it) }
                val course = openCourse?.let {
                    EventAction(Icons.AutoMirrored.Outlined.MenuBook, "Apri corso", it)
                }
                if (course != null) course to map else map to null
            }
        }
        if (primary != null) {
            Spacer(Modifier.height(24.dp))
            ActionRow(primary = primary, secondary = secondary)
        }
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

/** Opens [url] in an external handler, swallowing resolution failures. */
private fun android.content.Context.openExternal(url: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/**
 * One info row in the list flavor of the segmented M3E group language the map sheets use:
 * 28dp corners cap the group's outer edges, 6dp where rows touch, an icon tile leading a
 * label-over-value column.
 */
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

private data class EventAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Bottom action area: the kind-specific primary action fills the trailing brand slot of a
 * connected button group, with the optional secondary as its tonal leading half. Alone,
 * the primary spans the row as a single filled button.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(primary: EventAction, secondary: EventAction?) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val primaryBg = if (dark) scheme.primaryContainer else scheme.primary
    val primaryFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    if (secondary == null) {
        Button(
            onClick = primary.onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(CalendarTestTags.EVENT_PRIMARY_ACTION),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBg,
                contentColor = primaryFg,
            ),
        ) {
            Icon(
                imageVector = primary.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(primary.label, fontWeight = FontWeight.SemiBold)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = secondary.onClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag(CalendarTestTags.EVENT_SECONDARY_ACTION),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = secondary.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(secondary.label, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = primary.onClick,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp)
                .testTag(CalendarTestTags.EVENT_PRIMARY_ACTION),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBg,
                contentColor = primaryFg,
            ),
        ) {
            Icon(
                imageVector = primary.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(primary.label, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun activityLabel(event: CalendarEvent): String = when (event) {
    is CalendarEvent.Lesson -> "Lezione"
    is CalendarEvent.Exam -> {
        val type = event.examTypeLabel?.takeIf { it.isNotBlank() }
        if (type != null) "Esame · $type" else "Esame"
    }
    is CalendarEvent.AssignmentDeadline -> "Consegna compito"
    is CalendarEvent.Appointment ->
        event.serviceGroup?.takeIf { it.isNotBlank() }?.let { "Appuntamento · $it" } ?: "Appuntamento"
    is CalendarEvent.LibraryReservation -> "Posto in biblioteca"
}

/** Time-row value: "Entro le HH:MM" for point-in-time events, the range plus a compact duration suffix otherwise. */
private fun orarioValue(event: CalendarEvent): String {
    if (event.isPointInTime) return "Entro le ${event.formatTimeRange()}"
    val durMin = event.durationMinutes()
    val durLabel = when {
        durMin == 0 -> "istantaneo"
        durMin < 60 -> "${durMin}min"
        durMin % 60 == 0 -> "${durMin / 60}h"
        else -> "${durMin / 60}h ${durMin % 60}min"
    }
    return "${event.formatTimeRange()} · $durLabel"
}

/** Booking-row value composed from whichever facts the exam carries (position, booking date); null when it has none. */
private fun bookingLine(event: CalendarEvent.Exam): String? {
    val position = event.bookingPosition?.let { "Posizione $it" }
    val booked = event.bookedAt?.let { "prenotato il ${it.toLocalDate().formatItalian()}" }
    val line = listOfNotNull(position, booked).joinToString(" · ")
    return line.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
}

private val ItalianDate = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)

private fun LocalDate.formatItalian(): String = format(ItalianDate)

private val MapsEmbedCoordRegex = Regex("!2d(-?\\d+\\.?\\d*)!3d(-?\\d+\\.?\\d*)")

/**
 * Translates a Google Maps embed URL into a labeled geo: URI by extracting its !2d/!3d
 * coordinate pair; URLs without embedded coordinates pass through unchanged.
 */
private fun mapsUrlToGeoUri(rawUrl: String, label: String?): String {
    val match = MapsEmbedCoordRegex.find(rawUrl) ?: return rawUrl
    val lng = match.groupValues[1]
    val lat = match.groupValues[2]
    val labelPart = label?.takeIf { it.isNotBlank() }?.let { "(${Uri.encode(it)})" }.orEmpty()
    return "geo:$lat,$lng?q=$lat,$lng$labelPart"
}
