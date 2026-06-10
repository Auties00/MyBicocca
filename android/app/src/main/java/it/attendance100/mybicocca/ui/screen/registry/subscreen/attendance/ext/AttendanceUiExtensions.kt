package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.graphics.shapes.RoundedPolygon
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

/**
 * Container/content/accent trio for a course's attendance status, in proper MD3 tonal pairs
 * (container + onContainer; accent is the base role for text on plain surfaces).
 */
data class StatusTone(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
)

@Composable
fun ClassroomAttendanceStatus?.tone(): StatusTone {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        ClassroomAttendanceStatus.Attending ->
            StatusTone(scheme.tertiaryContainer, scheme.onTertiaryContainer, scheme.tertiary)

        ClassroomAttendanceStatus.InProgress ->
            StatusTone(scheme.secondaryContainer, scheme.onSecondaryContainer, scheme.secondary)

        ClassroomAttendanceStatus.NotAttending ->
            StatusTone(scheme.errorContainer, scheme.onErrorContainer, scheme.error)

        null -> StatusTone(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, scheme.onSurfaceVariant)
    }
}

/** Expressive shape-as-meaning: the badge silhouette tracks the status. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ClassroomAttendanceStatus?.badgePolygon(): RoundedPolygon = when (this) {
    ClassroomAttendanceStatus.Attending -> MaterialShapes.Sunny
    ClassroomAttendanceStatus.InProgress -> MaterialShapes.Cookie6Sided
    ClassroomAttendanceStatus.NotAttending -> MaterialShapes.SoftBurst
    null -> MaterialShapes.Circle
}

@Composable
fun ClassroomAttendanceStatus.label(): String = when (this) {
    ClassroomAttendanceStatus.Attending -> stringResource(R.string.attendance_status_attending)
    ClassroomAttendanceStatus.InProgress -> stringResource(R.string.attendance_status_in_progress)
    ClassroomAttendanceStatus.NotAttending -> stringResource(R.string.attendance_status_not_attending)
}

@Composable
fun Semester.label(): String? = when (this) {
    Semester.First -> stringResource(R.string.attendance_semester_first)
    Semester.Second -> stringResource(R.string.attendance_semester_second)
    Semester.Unknown -> null
}

/** Year-group header title; StudyYear(0) is the "not tied to a year" bucket. */
@Composable
fun StudyYear.label(): String = if (value > 0) stringResource(
    R.string.attendance_year,
    value
) else stringResource(R.string.attendance_year_other)

/** Icon + tonal palette + headline for a presence-registration outcome. */
data class OutcomeVisual(
    val icon: ImageVector,
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val title: String,
)

@Composable
fun PresenceMarkOutcome.visual(): OutcomeVisual {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        is PresenceMarkOutcome.Recorded -> OutcomeVisual(
            Icons.Rounded.CheckCircle, scheme.tertiaryContainer, scheme.onTertiaryContainer,
            scheme.tertiary, stringResource(R.string.attendance_presence_recorded),
        )

        is PresenceMarkOutcome.AlreadyRecorded -> OutcomeVisual(
            Icons.Outlined.Info, scheme.secondaryContainer, scheme.onSecondaryContainer,
            scheme.secondary, stringResource(R.string.attendance_already_recorded),
        )

        is PresenceMarkOutcome.WrongCredential -> OutcomeVisual(
            Icons.Outlined.Key, scheme.errorContainer, scheme.onErrorContainer,
            scheme.error, stringResource(R.string.attendance_wrong_credential),
        )

        is PresenceMarkOutcome.NotOpen -> OutcomeVisual(
            Icons.Outlined.Schedule, scheme.secondaryContainer, scheme.onSecondaryContainer,
            scheme.secondary, stringResource(R.string.attendance_session_unavailable),
        )

        is PresenceMarkOutcome.Failed -> OutcomeVisual(
            Icons.Outlined.ErrorOutline, scheme.errorContainer, scheme.onErrorContainer,
            scheme.error, stringResource(R.string.attendance_registration_failed),
        )
    }
}
