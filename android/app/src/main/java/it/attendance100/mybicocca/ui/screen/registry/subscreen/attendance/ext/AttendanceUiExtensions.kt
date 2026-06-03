package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.studyplan.Semester

// Container/content/accent trio for a course's attendance status, in proper
// MD3 tonal pairs (container + onContainer; accent is the base role for text
// on plain surfaces).
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

// Expressive shape-as-meaning: the badge silhouette tracks the status.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ClassroomAttendanceStatus?.badgePolygon(): RoundedPolygon = when (this) {
    ClassroomAttendanceStatus.Attending -> MaterialShapes.Sunny
    ClassroomAttendanceStatus.InProgress -> MaterialShapes.Cookie6Sided
    ClassroomAttendanceStatus.NotAttending -> MaterialShapes.SoftBurst
    null -> MaterialShapes.Circle
}

fun ClassroomAttendanceStatus.label(): String = when (this) {
    ClassroomAttendanceStatus.Attending -> "Frequentante"
    ClassroomAttendanceStatus.InProgress -> "In frequenza"
    ClassroomAttendanceStatus.NotAttending -> "Non frequentante"
}

fun Semester.label(): String? = when (this) {
    Semester.First -> "1° semestre"
    Semester.Second -> "2° semestre"
    Semester.Unknown -> null
}

// The single glanceable percentage for a course: progress towards the in-aula
// requirement when badge tracking exists, otherwise the recorded-session rate.
fun CourseAttendance.headlinePercentage(): Double? =
    classroomAttendance?.requirementProgressPercentage
        ?: sessionAttendance.firstNotNullOfOrNull { it.recordedPercentage }
