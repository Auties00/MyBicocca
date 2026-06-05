package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The page's status anchor in the Piano di Studi rule-header language: a headline,
// a date caption, and a trailing pill in the rule-progress style. A grade takes the
// leading chip slot the plan compiler uses for CFU.
@Composable
fun AssignmentStatusTile(
    assignment: Assignment,
    now: Instant,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    when (val status = assignment.submissionStatus) {
        is SubmissionStatus.Graded -> SegmentHeaderTile(
            title = "Valutato",
            modifier = modifier,
            subtitle = status.submittedAt
                ?.let { "Inviato ${FullDateFmt.format(it)} alle ${TimeFmt.format(it)}" }
                ?: "Consegna valutata",
            isLast = isLast,
            leadingChip = { GradeChip(grade = status.grade, maxGrade = status.maxGrade) },
        )

        is SubmissionStatus.Submitted -> {
            val late = assignment.dueDate != null && status.submittedAt?.isAfter(assignment.dueDate) == true
            SegmentHeaderTile(
                title = "Consegnato",
                modifier = modifier,
                subtitle = status.submittedAt
                    ?.let { "Inviato ${FullDateFmt.format(it)} alle ${TimeFmt.format(it)}" }
                    ?: "Consegna registrata",
                isLast = isLast,
                pill = {
                    if (late) {
                        SegmentPill(
                            label = "In ritardo",
                            container = scheme.errorContainer,
                            content = scheme.onErrorContainer,
                            icon = Icons.Rounded.Schedule,
                        )
                    } else {
                        SegmentPill(
                            label = "In orario",
                            container = scheme.primaryContainer,
                            content = scheme.onPrimaryContainer,
                            icon = Icons.Rounded.Check,
                        )
                    }
                },
            )
        }

        is SubmissionStatus.Draft -> {
            val due = assignment.dueDate
            SegmentHeaderTile(
                title = "Bozza salvata",
                modifier = modifier,
                subtitle = "La bozza non vale come consegna: completa l'invio sul sito",
                isLast = isLast,
                pill = {
                    if (due != null && due.isAfter(now)) {
                        SegmentPill(
                            label = countdownLabel(now, due),
                            container = scheme.tertiaryContainer,
                            content = scheme.onTertiaryContainer,
                            icon = Icons.Rounded.Schedule,
                        )
                    } else {
                        SegmentPill(
                            label = "Bozza",
                            container = scheme.secondaryContainer,
                            content = scheme.onSecondaryContainer,
                            icon = Icons.Rounded.Edit,
                        )
                    }
                },
            )
        }

        SubmissionStatus.NotSubmitted -> {
            val due = assignment.dueDate
            if (due != null && due.isBefore(now)) {
                val lateUntil = assignment.cutoffDate?.takeIf { it.isAfter(now) }
                SegmentHeaderTile(
                    title = "Non consegnato",
                    modifier = modifier,
                    subtitle = buildString {
                        append("Scaduto ${FullDateFmt.format(due)} alle ${TimeFmt.format(due)}")
                        if (lateUntil != null) {
                            append(" · consegne in ritardo fino al ${FullDateFmt.format(lateUntil)}")
                        }
                    },
                    isLast = isLast,
                    pill = {
                        SegmentPill(
                            label = "Scaduto",
                            container = scheme.errorContainer,
                            content = scheme.onErrorContainer,
                            icon = Icons.Rounded.Close,
                        )
                    },
                )
            } else {
                val opensAt = assignment.allowSubmissionsFrom?.takeIf { it.isAfter(now) }
                SegmentHeaderTile(
                    title = "Da consegnare",
                    modifier = modifier,
                    subtitle = when {
                        opensAt != null -> "La consegna apre ${FullDateFmt.format(opensAt)} alle ${TimeFmt.format(opensAt)}"
                        due != null -> "Scade ${FullDateFmt.format(due)} alle ${TimeFmt.format(due)}"
                        else -> "Puoi consegnare in qualsiasi momento"
                    },
                    isLast = isLast,
                    pill = {
                        SegmentPill(
                            label = if (due != null) countdownLabel(now, due) else "Senza scadenza",
                            container = scheme.tertiaryContainer,
                            content = scheme.onTertiaryContainer,
                            icon = Icons.Rounded.Schedule,
                        )
                    },
                )
            }
        }
    }
}

// The grade in the plan compiler's leading-chip language: value on top, scale below,
// on the course accent fill. CourseDetailTheme computes a contrast-correct onPrimary.
@Composable
private fun GradeChip(grade: Double?, maxGrade: Double?) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(scheme.primary, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatGradeValue(grade),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
                maxLines = 1,
            )
            if (maxGrade != null) {
                Text(
                    text = "su ${formatGradeValue(maxGrade)}",
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = scheme.onPrimary.copy(alpha = 0.8f),
                    maxLines = 1,
                )
            }
        }
    }
}

private fun countdownLabel(now: Instant, due: Instant): String {
    val duration = Duration.between(now, due)
    if (duration.isNegative) return "Scaduto"
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    return when {
        days >= 1 -> "Tra ${days}g ${hours}h"
        hours >= 1 -> "Tra ${hours}h ${minutes}min"
        else -> "Tra ${minutes}min"
    }
}

private fun formatGradeValue(grade: Double?): String = when {
    grade == null -> "—"
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(Locale.ITALIAN, "%.1f", grade)
}

private val FullDateFmt = DateTimeFormatter
    .ofPattern("EEE d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
