package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailTestTags
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.ui.component.button.PrimaryActionButton
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import java.time.Instant

/**
 * The compito overview, root page of the in-sheet pager: a column of fact rows, the
 * instructions / your-submission / teacher-feedback sections, and the submission CTAs. The
 * compito name and deadline live in the modal's pager header.
 *
 * The body scrolls inside a weighted region while the action footer stays pinned and visible
 * even when the body overflows. Everything is handled in-app — submission via the editor, and
 * closed / not-yet-open / graded states surfaced inline, never via the web portal.
 */
@Composable
internal fun AssignmentOverviewPage(
    assignment: Assignment,
    onOpenFile: (String, String, String?, Long?, Boolean) -> Unit,
    onCompose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val now = remember(assignment) { Instant.now() }
    val status = assignment.submissionStatus

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                (status as? SubmissionStatus.Graded)?.takeIf { it.grade != null }?.let { graded ->
                    IconRow(
                        icon = Icons.Outlined.EmojiEvents,
                        label = stringResource(R.string.elearning_assign_grade_label),
                        value = buildString {
                            append(formatGrade(graded.grade!!))
                            graded.maxGrade?.takeIf { it > 0 }?.let { append(" / ${formatGrade(it)}") }
                        },
                    )
                }
                assignment.allowSubmissionsFrom?.takeIf { it.isAfter(now) }?.let {
                    IconRow(
                        Icons.Outlined.Schedule,
                        stringResource(R.string.elearning_assign_opening_label),
                        stringResource(R.string.elearning_assign_opens_later, DateFmt.format(it)),
                    )
                }
                assignment.cutoffDate?.takeIf { it.isAfter(now) && assignment.dueDate?.isBefore(now) == true }?.let {
                    IconRow(
                        Icons.Outlined.EventBusy,
                        stringResource(R.string.elearning_assign_late_submission_label),
                        stringResource(R.string.elearning_assign_late_submission, DateFmt.format(it)),
                    )
                }
                (status as? SubmissionStatus.Submitted)?.submittedAt?.let {
                    IconRow(
                        Icons.Outlined.CheckCircle,
                        stringResource(R.string.elearning_assign_submitted_label),
                        stringResource(R.string.elearning_assign_submitted_date, DateFmt.format(it)),
                    )
                }
                (status as? SubmissionStatus.Graded)?.submittedAt?.let {
                    IconRow(
                        Icons.Outlined.CheckCircle,
                        stringResource(R.string.elearning_assign_submitted_label),
                        stringResource(R.string.elearning_assign_submitted_date, DateFmt.format(it)),
                    )
                }
                assignment.maxAttempts?.takeIf { it >= 1 }?.let {
                    IconRow(
                        Icons.Outlined.Repeat,
                        stringResource(R.string.elearning_assign_attempts_label),
                        pluralStringResource(R.plurals.elearning_assign_max_attempts, it, it),
                    )
                }
            }

            val intro = assignment.intro?.takeIf { it.isNotBlank() }
            if (intro != null || assignment.introFiles.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                if (intro != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = scheme.surfaceContainerLowest,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        HtmlBody(html = intro, modifier = Modifier.padding(14.dp))
                    }
                    if (assignment.introFiles.isNotEmpty()) Spacer(Modifier.height(8.dp))
                }
                AttachmentList(files = assignment.introFiles, onOpenFile = onOpenFile)
            }

            (status as? SubmissionStatus.Submitted)?.let { submitted ->
                val text = submitted.onlineText?.takeIf { it.isNotBlank() }
                if (submitted.files.isNotEmpty() || text != null) {
                    SectionLabel(stringResource(R.string.elearning_assign_submission_title))
                    AttachmentList(files = submitted.files, onOpenFile = onOpenFile)
                    if (text != null) {
                        if (submitted.files.isNotEmpty()) Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = scheme.surfaceContainerLowest,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            HtmlBody(html = text, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }

            (status as? SubmissionStatus.Graded)?.feedback?.takeIf { it.isNotBlank() }?.let { feedback ->
                SectionLabel(stringResource(R.string.elearning_assign_teacher_feedback))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = scheme.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HtmlBody(html = feedback, modifier = Modifier.padding(14.dp))
                }
            }
        }

        OverviewActions(
            assignment = assignment,
            now = now,
            onCompose = onCompose,
            onRemove = onRemove,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
        )
    }
}

/**
 * Footer actions derived from the submission window and status. The compito is editable when it
 * is not graded, submissions have opened, and the window is still open — or a draft/submission
 * already exists to manage — yielding the compose CTA plus a remove option. Graded or locked
 * submissions show no footer action: the grade / feedback / submission above is the whole
 * story. Otherwise a disabled button states inline that submissions are not yet open or already
 * closed; no state ever hands off to the web portal.
 */
@Composable
private fun OverviewActions(
    assignment: Assignment,
    now: Instant,
    onCompose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOnline = LocalIsOnline.current
    val scheme = MaterialTheme.colorScheme
    val status = assignment.submissionStatus
    val due = assignment.dueDate
    val cutoff = assignment.cutoffDate
    val opensLater = assignment.allowSubmissionsFrom?.isAfter(now) == true
    val overdue = due != null && due.isBefore(now)
    val lateOpen = cutoff?.isAfter(now) == true
    val isGraded = status is SubmissionStatus.Graded
    val isSubmitted = status is SubmissionStatus.Submitted
    val isDraft = status is SubmissionStatus.Draft

    val windowOpen = !overdue || lateOpen || isDraft || isSubmitted
    val canEdit = !isGraded && !opensLater && windowOpen

    when {
        canEdit -> Column(modifier) {
            val label = when {
                isDraft -> stringResource(R.string.elearning_assign_complete_submission)
                isSubmitted -> stringResource(R.string.elearning_assign_modify_submission)
                overdue && lateOpen -> stringResource(R.string.elearning_assign_late_submission_button)
                else -> stringResource(R.string.elearning_assign_add_submission)
            }
            PrimaryActionButton(
                text = label,
                onClick = onCompose,
                enabled = isOnline,
                leadingIcon = Icons.AutoMirrored.Rounded.Send,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(AssignmentDetailTestTags.COMPOSE_ACTION),
            )
            if (isDraft || isSubmitted) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onRemove,
                    enabled = isOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AssignmentDetailTestTags.REMOVE_ACTION),
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.elearning_assign_remove_submission),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        isGraded || isSubmitted -> Unit

        else -> Column(modifier) {
            val label = if (opensLater) {
                stringResource(R.string.elearning_assign_not_open_yet)
            } else {
                stringResource(R.string.elearning_assign_closed)
            }
            FilledTonalButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                    disabledContainerColor = scheme.surfaceContainerHigh,
                    disabledContentColor = scheme.onSurfaceVariant,
                ),
            ) {
                Text(label, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** A list of files rendered as one connected segment group of [AttachmentTile]s. */
@Composable
internal fun AttachmentList(
    files: List<Assignment.AttachmentRef>,
    onOpenFile: (String, String, String?, Long?, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        files.forEachIndexed { index, file ->
            AttachmentTile(
                file = file,
                isFirst = index == 0,
                isLast = index == files.lastIndex,
                onOpen = {
                    file.fileUrl?.let { onOpenFile(file.fileName, it, file.mimeType, file.sizeBytes, false) }
                },
                onLongOpen = {
                    file.fileUrl?.let { onOpenFile(file.fileName, it, file.mimeType, file.sizeBytes, true) }
                },
            )
        }
    }
}

/** Flower-prefixed uppercase eyebrow in tertiary that opens a section of the page. */
@Composable
internal fun SectionLabel(title: String) {
    Column(modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)) {
        Text(
            text = "❀ ${title.uppercase()}",
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun IconRow(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceContainerHigh,
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

/** Whole grades render without a decimal; fractional ones keep one place. */
internal fun formatGrade(grade: Double): String = when {
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(java.util.Locale.getDefault(), "%.1f", grade)
}

internal val DateFmt: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter
    .ofPattern("EEE d MMM, HH:mm", java.util.Locale.getDefault())
    .withZone(java.time.ZoneId.systemDefault())
