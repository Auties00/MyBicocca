package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext

import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.state.EnrollmentBadge
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)

/** "2024/2025"-style academic-year label. */
fun AnnualEnrollment.academicYearLabel(): String =
    "$academicYear/${academicYear + 1}"

/** "1° anno", "2° anno", …; falls back gracefully when the course year is missing. */
fun AnnualEnrollment.courseYearLabel(): String =
    if (courseYear > 0) "$courseYear° anno" else "Anno di corso n/d"

/** Italian status line for the timeline row and the detail header. */
fun AnnualEnrollment.statusLabel(): String = when (status) {
    EnrollmentStatus.Active -> "Attiva"
    EnrollmentStatus.Canceled -> "Annullata"
    EnrollmentStatus.Suspended -> "Sospesa"
    EnrollmentStatus.Unknown -> "Stato n/d"
}

fun AnnualEnrollment.statusTone(): RegistryBadgeTone = when (status) {
    EnrollmentStatus.Active -> RegistryBadgeTone.Ok
    EnrollmentStatus.Canceled -> RegistryBadgeTone.Alert
    EnrollmentStatus.Suspended -> RegistryBadgeTone.Attention
    EnrollmentStatus.Unknown -> RegistryBadgeTone.Neutral
}

/** Italian enrollment-type label; unknown types fall back to the raw description. */
fun AnnualEnrollment.typeLabel(): String = when (type) {
    EnrollmentType.InProgress -> "In corso"
    EnrollmentType.OutOfCourse -> "Fuori corso"
    EnrollmentType.Repeating -> "Ripetente"
    EnrollmentType.Unknown -> typeDescription ?: "Tipo n/d"
}

/**
 * Highlight chips for the enrollment. The plain "in corso / attiva" case yields no chips
 * — it is the default and keeping it badge-free leaves the list uncluttered; only the
 * situations a student needs to notice (fuori corso, ripetente, sospesa, annullata,
 * part-time, condizionata, attesa di laurea, esonero) surface a badge.
 */
fun AnnualEnrollment.badges(): List<EnrollmentBadge> {
    val out = mutableListOf<EnrollmentBadge>()
    when (type) {
        EnrollmentType.OutOfCourse -> out += EnrollmentBadge(
            label = if (outOfCourseYears > 0) "Fuori corso · ${outOfCourseYears}°" else "Fuori corso",
            tone = RegistryBadgeTone.Attention,
        )
        EnrollmentType.Repeating -> out += EnrollmentBadge("Ripetente", RegistryBadgeTone.Attention)
        else -> Unit
    }
    if (status == EnrollmentStatus.Suspended) {
        out += EnrollmentBadge("Sospesa", RegistryBadgeTone.Attention)
    }
    if (status == EnrollmentStatus.Canceled) {
        out += EnrollmentBadge("Annullata", RegistryBadgeTone.Alert)
    }
    if (partTime != null) {
        val credits = partTime.credits
        out += EnrollmentBadge(
            label = if (credits != null) "Part-time · $credits CFU" else "Part-time",
            tone = RegistryBadgeTone.Info,
        )
    }
    if (conditional) {
        out += EnrollmentBadge("Condizionata", RegistryBadgeTone.Info)
    }
    if (awaitingDegree) {
        out += EnrollmentBadge("In attesa di laurea", RegistryBadgeTone.Info)
    }
    if (exemptionDescription != null) {
        out += EnrollmentBadge("Esonero", RegistryBadgeTone.Info)
    }
    return out
}

/** "12 settembre 2024"-style Italian day label. */
fun LocalDate.toEnrollmentDateLabel(): String = format(DayFormat)
