package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.state.EnrollmentBadge
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

/** "2024/2025"-style academic-year label. */
fun AnnualEnrollment.academicYearLabel(): String =
    "$academicYear/${academicYear + 1}"

/** "Year 1", "Year 2", …; falls back gracefully when the course year is missing. */
@Composable
fun AnnualEnrollment.courseYearLabel(): String =
    if (courseYear > 0) stringResource(
        R.string.enrollment_year,
        courseYear
    ) else stringResource(R.string.enrollment_year_unknown)

/** Status line for the timeline row and the detail header. */
@Composable
fun AnnualEnrollment.statusLabel(): String = when (status) {
    EnrollmentStatus.Active -> stringResource(R.string.enrollment_status_active)
    EnrollmentStatus.Canceled -> stringResource(R.string.enrollment_status_canceled)
    EnrollmentStatus.Suspended -> stringResource(R.string.enrollment_status_suspended)
    EnrollmentStatus.Unknown -> stringResource(R.string.enrollment_status_unknown)
}

fun AnnualEnrollment.statusTone(): RegistryBadgeTone = when (status) {
    EnrollmentStatus.Active -> RegistryBadgeTone.Ok
    EnrollmentStatus.Canceled -> RegistryBadgeTone.Alert
    EnrollmentStatus.Suspended -> RegistryBadgeTone.Attention
    EnrollmentStatus.Unknown -> RegistryBadgeTone.Neutral
}

/** Enrollment-type label; unknown types fall back to the raw description. */
@Composable
fun AnnualEnrollment.typeLabel(): String = when (type) {
    EnrollmentType.InProgress -> stringResource(R.string.enrollment_type_in_progress)
    EnrollmentType.OutOfCourse -> stringResource(R.string.enrollment_type_out_of_course)
    EnrollmentType.Repeating -> stringResource(R.string.enrollment_type_repeating)
    EnrollmentType.Unknown -> typeDescription ?: stringResource(R.string.enrollment_type_unknown)
}

/**
 * Highlight chips for the enrollment. The plain "in corso / attiva" case yields no chips
 * — it is the default and keeping it badge-free leaves the list uncluttered; only the
 * situations a student needs to notice (fuori corso, ripetente, sospesa, annullata,
 * part-time, condizionata, attesa di laurea, esonero) surface a badge.
 */
@Composable
fun AnnualEnrollment.badges(): List<EnrollmentBadge> {
    val out = mutableListOf<EnrollmentBadge>()
    when (type) {
        EnrollmentType.OutOfCourse -> out += EnrollmentBadge(
            label = if (outOfCourseYears > 0) stringResource(
                R.string.enrollment_badge_out_of_course_year,
                outOfCourseYears
            ) else stringResource(R.string.enrollment_badge_out_of_course),
            tone = RegistryBadgeTone.Attention,
        )

        EnrollmentType.Repeating -> out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_repeating),
            RegistryBadgeTone.Attention
        )
        else -> Unit
    }
    if (status == EnrollmentStatus.Suspended) {
        out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_suspended),
            RegistryBadgeTone.Attention
        )
    }
    if (status == EnrollmentStatus.Canceled) {
        out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_canceled),
            RegistryBadgeTone.Alert
        )
    }
    if (partTime != null) {
        val credits = partTime.credits
        out += EnrollmentBadge(
            label = if (credits != null) stringResource(
                R.string.enrollment_badge_part_time_credits,
                credits.toString()
            ) else stringResource(R.string.enrollment_badge_part_time),
            tone = RegistryBadgeTone.Info,
        )
    }
    if (conditional) {
        out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_conditional),
            RegistryBadgeTone.Info
        )
    }
    if (awaitingDegree) {
        out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_awaiting_degree),
            RegistryBadgeTone.Info
        )
    }
    if (exemptionDescription != null) {
        out += EnrollmentBadge(
            stringResource(R.string.enrollment_badge_exemption),
            RegistryBadgeTone.Info
        )
    }
    return out
}

/** "12 settembre 2024"-style Italian day label. */
fun LocalDate.toEnrollmentDateLabel(): String = format(DayFormat)
