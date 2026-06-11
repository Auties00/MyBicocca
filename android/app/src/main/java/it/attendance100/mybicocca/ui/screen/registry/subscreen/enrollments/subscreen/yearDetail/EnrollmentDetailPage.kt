package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.subscreen.yearDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Accessible
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.component.EnrollmentBadgeChip
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.badges
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.toEnrollmentDateLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.typeLabel

/**
 * Full breakdown for one academic year, as a headerless page inside the iscrizioni sheet
 * pager: the sheet's morphing header carries the year and status line. Badge chips lead
 * when present, then each populated topic is a grouped tonal block led by an icon chip,
 * with the fields as divider-separated key/value rows — sections appear only when they
 * have content, so a clean career shows just the essentials while a fuori-corso /
 * part-time / suspended year reveals the extra fields.
 */
@Composable
fun EnrollmentDetailPage(
    enrollment: AnnualEnrollment,
    modifier: Modifier = Modifier,
) {
    val badges = enrollment.badges()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (badges.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    badges.forEach { EnrollmentBadgeChip(it) }
                }
            }
        }

        item {
            DetailGroup(
                icon = Icons.Outlined.HowToReg,
                title = stringResource(R.string.enrollments_enrollment),
                rows = buildList {
                    add(stringResource(R.string.enrollments_type) to enrollment.typeLabel())
                    enrollment.studentTypeDescription?.let { add(stringResource(R.string.enrollments_student_type) to it) }
                    if (enrollment.outOfCourseYears > 0) {
                        add(stringResource(R.string.enrollments_out_of_course_years) to enrollment.outOfCourseYears.toString())
                    }
                    if (enrollment.conditional) add(
                        stringResource(R.string.enrollments_enrollment) to stringResource(
                            R.string.enrollments_conditional
                        )
                    )
                    if (enrollment.reconstructed) add(
                        stringResource(R.string.enrollments_data_origin) to stringResource(
                            R.string.enrollments_reconstructed
                        )
                    )
                    enrollment.statusReasonCode?.let { add(stringResource(R.string.enrollments_status_reason) to it) }
                },
            )
        }

        enrollment.partTime?.let { pt ->
            item {
                DetailGroup(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.enrollments_part_time),
                    rows = buildList {
                        pt.credits?.let { add(stringResource(R.string.enrollments_planned_credits) to "$it CFU") }
                        pt.extraCredits?.let { add(stringResource(R.string.enrollments_extra_credits) to "$it CFU") }
                        add(
                            stringResource(R.string.enrollments_changes) to if (pt.locked) stringResource(
                                R.string.enrollments_locked
                            ) else stringResource(R.string.enrollments_allowed)
                        )
                    },
                )
            }
        }

        enrollment.suspension?.let { susp ->
            item {
                DetailGroup(
                    icon = Icons.Outlined.PauseCircle,
                    title = stringResource(R.string.enrollments_suspension),
                    rows = buildList {
                        susp.reasonCode?.let { add(stringResource(R.string.enrollments_reason) to it) }
                            ?: add(stringResource(R.string.enrollments_status) to stringResource(R.string.enrollments_suspended))
                    },
                )
            }
        }

        if (enrollment.awaitingDegree || enrollment.degreeAwardDate != null) {
            item {
                DetailGroup(
                    icon = Icons.Outlined.School,
                    title = stringResource(R.string.enrollments_degree),
                    rows = buildList {
                        if (enrollment.awaitingDegree) add(
                            stringResource(R.string.enrollments_status) to stringResource(
                                R.string.enrollments_awaiting_degree
                            )
                        )
                        enrollment.degreeAwardDate?.let {
                            add(stringResource(R.string.enrollments_expected_degree_date) to it.toEnrollmentDateLabel())
                        }
                    },
                )
            }
        }

        if (enrollment.exemptionDescription != null || enrollment.incomeBandId != null ||
            enrollment.canteenBandId != null || enrollment.meritBandId != null || enrollment.meritNote != null
        ) {
            item {
                DetailGroup(
                    icon = Icons.Outlined.Savings,
                    title = stringResource(R.string.enrollments_benefits),
                    rows = buildList {
                        enrollment.exemptionDescription?.let { add(stringResource(R.string.enrollments_exemption) to it) }
                        enrollment.incomeBandId?.let { add(stringResource(R.string.enrollments_income_band) to it.toString()) }
                        enrollment.canteenBandId?.let { add(stringResource(R.string.enrollments_canteen_band) to it.toString()) }
                        enrollment.meritBandId?.let { add(stringResource(R.string.enrollments_merit_band) to it.toString()) }
                        enrollment.meritNote?.let { add(stringResource(R.string.enrollments_merit_note) to it) }
                    },
                )
            }
        }

        if (enrollment.disabilityTypeDescription != null || enrollment.disabilityPercentage != null) {
            item {
                DetailGroup(
                    icon = Icons.AutoMirrored.Outlined.Accessible,
                    title = stringResource(R.string.enrollments_disability),
                    rows = buildList {
                        enrollment.disabilityTypeDescription?.let { add(stringResource(R.string.enrollments_disability_type) to it) }
                        enrollment.disabilityPercentage?.let {
                            add(stringResource(R.string.enrollments_disability_percentage) to "${it.toInt()}%")
                        }
                    },
                )
            }
        }

        item {
            DetailGroup(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = stringResource(R.string.enrollments_course_of_study),
                rows = buildList {
                    enrollment.courseDescription?.let { add(stringResource(R.string.enrollments_course) to it) }
                    enrollment.courseTypeDescription?.let { add(stringResource(R.string.enrollments_course_type) to it) }
                    enrollment.degreeClassDescription?.let { add(stringResource(R.string.enrollments_class) to it) }
                    enrollment.degreeClassCode?.let { add(stringResource(R.string.enrollments_class_code) to it) }
                    enrollment.addressDescription
                        ?.takeIf { !it.equals("PERCORSO COMUNE", ignoreCase = true) }
                        ?.let { add(stringResource(R.string.enrollments_path) to it) }
                    enrollment.orientationDescription?.let { add(stringResource(R.string.enrollments_orientation) to it) }
                    enrollment.studyOrderDescription?.let { add(stringResource(R.string.enrollments_study_order) to it) }
                    enrollment.minimumCredits?.let { add(stringResource(R.string.enrollments_credits_for_degree) to "$it CFU") }
                    enrollment.courseDuration?.let { add(stringResource(R.string.enrollments_duration) to "$it anni") }
                    enrollment.teachingLanguage?.let { add(stringResource(R.string.enrollments_language) to it) }
                    enrollment.regulationCode?.let { add(stringResource(R.string.enrollments_regulation) to it) }
                },
            )
        }

        if (enrollment.universityDescription != null || enrollment.siteDescription != null) {
            item {
                DetailGroup(
                    icon = Icons.Outlined.Place,
                    title = stringResource(R.string.enrollments_location),
                    rows = buildList {
                        enrollment.universityDescription?.let { add(stringResource(R.string.enrollments_university) to it) }
                        enrollment.siteDescription?.let { add(stringResource(R.string.enrollments_site) to it) }
                    },
                )
            }
        }

        if (enrollment.enrollmentDate != null || enrollment.insertionDate != null || enrollment.modificationDate != null) {
            item {
                DetailGroup(
                    icon = Icons.Outlined.Event,
                    title = stringResource(R.string.enrollments_dates),
                    rows = buildList {
                        enrollment.enrollmentDate?.let { add(stringResource(R.string.enrollments_enrollment_date) to it.toEnrollmentDateLabel()) }
                        enrollment.insertionDate?.let { add(stringResource(R.string.enrollments_inserted) to it.toEnrollmentDateLabel()) }
                        enrollment.modificationDate?.let { add(stringResource(R.string.enrollments_updated) to it.toEnrollmentDateLabel()) }
                    },
                )
            }
        }

        enrollment.enrollmentNote?.let { note ->
            item { NoteGroup(note = note) }
        }
    }
}

/**
 * One topic: an icon-chip header above a single tonal container holding the fields as
 * divider-separated rows. The header sits outside the container so the eye groups the
 * rows under it without the title competing with the surface fill.
 */
@Composable
private fun DetailGroup(
    icon: ImageVector,
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        GroupHeader(icon = icon, title = title)
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                rows.forEachIndexed { index, (label, value) ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    KeyValueRow(label = label, value = value)
                }
            }
        }
    }
}

@Composable
private fun NoteGroup(note: String) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        GroupHeader(
            icon = Icons.AutoMirrored.Outlined.Notes,
            title = stringResource(R.string.enrollments_notes)
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun GroupHeader(icon: ImageVector, title: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}
