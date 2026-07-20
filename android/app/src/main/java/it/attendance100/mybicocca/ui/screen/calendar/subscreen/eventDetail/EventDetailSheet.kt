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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
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
    /** Total students booked on the exam's call, joined from the live bookable list; null when unknown. */
    examTotalBookings: Int? = null,
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
                examTotalBookings = examTotalBookings,
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
    /** Total students booked on the exam's call, joined from the live bookable list; null when unknown. */
    examTotalBookings: Int? = null,
) {
    val context = LocalContext.current
    var showEditionPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.testTag(CalendarTestTags.EVENT_CONTENT)) {
        Spacer(Modifier.height(24.dp))

        val rows = buildList {
            add(
                Triple(
                    Icons.Outlined.Schedule,
                    stringResource(R.string.event_detail_time_label),
                    orarioValue(event)
                )
            )
            event.locationLine().takeIf { it.isNotBlank() }?.let {
                add(
                    Triple(
                        Icons.Outlined.LocationOn,
                        stringResource(R.string.event_detail_location_label),
                        it
                    )
                )
            }
            event.peopleLine().takeIf { it.isNotBlank() }?.let {
                add(
                    Triple(
                        Icons.Outlined.Person,
                        stringResource(R.string.event_detail_teacher_label),
                        it
                    )
                )
            }
            if (event is CalendarEvent.Exam) {
                bookingLine(event, examTotalBookings)?.let {
                    add(
                        Triple(
                            Icons.Outlined.ConfirmationNumber,
                            stringResource(R.string.event_detail_booking_label),
                            it
                        )
                    )
                }
                event.cancellableUntil?.let {
                    add(
                        Triple(
                            Icons.Outlined.EventBusy,
                            stringResource(R.string.event_detail_cancellable_label),
                            stringResource(
                                R.string.event_detail_cancellable_until,
                                it.formatItalian()
                            )
                        )
                    )
                }
            }
            if (event is CalendarEvent.LibraryReservation) {
                add(
                    Triple(
                        Icons.Outlined.Chair,
                        stringResource(R.string.event_detail_seat_label),
                        event.seatName
                    )
                )
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
            label = stringResource(R.string.event_detail_go_to_reservation),
            onClick = { onOpenReservation(event) },
        )
        val (primary, secondary) = when (event) {
            is CalendarEvent.AssignmentDeadline -> EventAction(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                label = stringResource(R.string.event_detail_open_assignment),
                onClick = { onOpenAssignment(event.assignmentId, event.courseId) },
            ) to null
            is CalendarEvent.Exam,
            is CalendarEvent.LibraryReservation,
            -> goToReservation to null
            is CalendarEvent.Appointment ->
                goToReservation to openMap?.let {
                    EventAction(
                        Icons.Default.Navigation,
                        stringResource(R.string.event_detail_maps),
                        it
                    )
                }
            is CalendarEvent.Lesson -> {
                val map = openMap?.let {
                    EventAction(
                        Icons.Default.Navigation,
                        stringResource(R.string.event_detail_maps),
                        it
                    )
                }
                val course = openCourse?.let {
                    EventAction(
                        Icons.Default.School,
                        stringResource(R.string.event_detail_open_course),
                        it
                    )
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
            Intent(Intent.ACTION_VIEW, url.toUri())
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
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val primaryBg = if (dark) scheme.primaryContainer else scheme.primary
    val primaryFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    if (secondary == null) {
        Button(
            onClick = { haptic.tap(); primary.onClick() },
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
            onClick = { haptic.tap(); secondary.onClick() },
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
            onClick = { haptic.tap(); primary.onClick() },
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

@Composable
private fun activityLabel(event: CalendarEvent): String = when (event) {
    is CalendarEvent.Lesson -> stringResource(R.string.event_detail_type_lesson)
    is CalendarEvent.Exam -> {
        val type = event.examTypeLabel?.takeIf { it.isNotBlank() }
        if (type != null) stringResource(
            R.string.event_detail_type_exam_with_type,
            type
        ) else stringResource(R.string.event_detail_type_exam)
    }

    is CalendarEvent.AssignmentDeadline -> stringResource(R.string.event_detail_type_assignment_deadline)
    is CalendarEvent.Appointment ->
        event.serviceGroup?.takeIf { it.isNotBlank() }
            ?.let { stringResource(R.string.event_detail_type_appointment_with_group, it) }
            ?: stringResource(R.string.event_detail_type_appointment)

    is CalendarEvent.LibraryReservation -> stringResource(R.string.event_detail_type_library_reservation)
}

@Composable
/** Time-row value: "Entro le HH:MM" for point-in-time events, the range plus a compact duration suffix otherwise. */
private fun orarioValue(event: CalendarEvent): String {
    if (event.isPointInTime) return stringResource(
        R.string.event_detail_instant_time,
        event.formatTimeRange()
    )
    val durMin = event.durationMinutes()
    val durLabel = when {
        durMin == 0 -> stringResource(R.string.event_detail_duration_instant)
        durMin < 60 -> stringResource(R.string.event_detail_duration_minutes, durMin)
        durMin % 60 == 0 -> stringResource(R.string.event_detail_duration_hours, durMin / 60)
        else -> stringResource(
            R.string.event_detail_duration_hours_minutes,
            durMin / 60,
            durMin % 60
        )
    }
    return stringResource(R.string.event_detail_duration_format, event.formatTimeRange(), durLabel)
}

@Composable
/** Booking-row value composed from whichever facts the exam carries (position with the call's total when known, booking date); null when it has none. */
private fun bookingLine(event: CalendarEvent.Exam, totalBookings: Int?): String? {
    val position = event.bookingPosition?.let { p ->
        totalBookings?.takeIf { it >= p }
            ?.let { stringResource(R.string.event_detail_booking_position_of_total, p, it) }
            ?: stringResource(R.string.event_detail_booking_position, p)
    }
    val booked = event.bookedAt?.let {
        stringResource(
            R.string.event_detail_booking_date,
            it.toLocalDate().formatItalian()
        )
    }
    val line = listOfNotNull(position, booked).joinToString(" · ")
    return line.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
}

private val ItalianDate = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())

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
