package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AttachmentTile
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The compito (assignment) detail as a modal, in the app's detail-sheet language (see
// QuizDetailSheet / BookedExamDetailSheet): icon-badged header with a status chip, a column of
// IconRows for the dates and facts, instructions / your-submission / feedback sections, and a
// single pinned action that hands off to e-learning (submission isn't supported in-app).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailSheet(
    assignId: Int,
    courseId: Int,
    onOpenFile: (fileName: String, fileUrl: String, mimeType: String?, sizeBytes: Long?) -> Unit,
    onOpenPage: (url: String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AssignmentDetailViewModel = hiltViewModel<AssignmentDetailViewModel, AssignmentDetailViewModel.Factory>(
        key = "assign-sheet-$assignId",
        creationCallback = { it.create(AppRoute.AssignmentDetail(assignId = assignId, courseId = courseId)) },
    ),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val assignmentLoadable by viewModel.assignment.collectAsStateWithLifecycle()

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                when (val loadable = assignmentLoadable) {
                    Loadable.NotYetLoaded -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    is Loadable.Loaded -> AssignmentSheetContent(
                        assignment = loadable.value,
                        onOpenFile = onOpenFile,
                        onOpenPage = onOpenPage,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentSheetContent(
    assignment: Assignment,
    onOpenFile: (String, String, String?, Long?) -> Unit,
    onOpenPage: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val now = remember(assignment) { Instant.now() }
    val status = assignment.submissionStatus

    // Header.
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = scheme.primaryContainer,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = assignment.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            StatusChip(assignment = assignment, now = now)
        }
    }

    Spacer(Modifier.height(24.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (status as? SubmissionStatus.Graded)?.takeIf { it.grade != null }?.let { graded ->
            IconRow(
                icon = Icons.Outlined.EmojiEvents,
                label = "VOTO",
                value = buildString {
                    append(formatGrade(graded.grade!!))
                    graded.maxGrade?.takeIf { it > 0 }?.let { append(" / ${formatGrade(it)}") }
                },
            )
        }
        assignment.allowSubmissionsFrom?.takeIf { it.isAfter(now) }?.let {
            IconRow(Icons.Outlined.Schedule, "APERTURA", "Apre ${DateFmt.format(it)}")
        }
        assignment.dueDate?.let { due ->
            IconRow(
                icon = Icons.Outlined.Schedule,
                label = "SCADENZA",
                value = if (due.isBefore(now)) "Scaduto il ${DateFmt.format(due)}"
                else "Entro ${DateFmt.format(due)}",
            )
        }
        assignment.cutoffDate?.takeIf { it.isAfter(now) && assignment.dueDate?.isBefore(now) == true }?.let {
            IconRow(Icons.Outlined.EventBusy, "CONSEGNA TARDIVA", "Possibile fino al ${DateFmt.format(it)}")
        }
        (status as? SubmissionStatus.Submitted)?.submittedAt?.let {
            IconRow(Icons.Outlined.CheckCircle, "CONSEGNATO", "Il ${DateFmt.format(it)}")
        }
        (status as? SubmissionStatus.Graded)?.submittedAt?.let {
            IconRow(Icons.Outlined.CheckCircle, "CONSEGNATO", "Il ${DateFmt.format(it)}")
        }
        assignment.maxAttempts?.takeIf { it >= 1 }?.let {
            IconRow(Icons.Outlined.Repeat, "TENTATIVI", if (it == 1) "Uno solo" else "Massimo $it")
        }
    }

    // Instructions.
    val intro = assignment.intro?.takeIf { it.isNotBlank() }
    if (intro != null || assignment.introFiles.isNotEmpty()) {
        SectionLabel("Istruzioni")
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

    // Your submission.
    (status as? SubmissionStatus.Submitted)?.let { submitted ->
        val text = submitted.onlineText?.takeIf { it.isNotBlank() }
        if (submitted.files.isNotEmpty() || text != null) {
            SectionLabel("La tua consegna")
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

    // Teacher feedback.
    (status as? SubmissionStatus.Graded)?.feedback?.takeIf { it.isNotBlank() }?.let { feedback ->
        SectionLabel("Feedback del docente")
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = scheme.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            HtmlBody(html = feedback, modifier = Modifier.padding(14.dp))
        }
    }

    assignment.pageUrl?.let { url ->
        Spacer(Modifier.height(24.dp))
        ActionButton(assignment = assignment, now = now, onClick = { onOpenPage(url) })
    }
}

@Composable
private fun AttachmentList(
    files: List<Assignment.AttachmentRef>,
    onOpenFile: (String, String, String?, Long?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        files.forEachIndexed { index, file ->
            AttachmentTile(
                file = file,
                isFirst = index == 0,
                isLast = index == files.lastIndex,
                onOpen = {
                    file.fileUrl?.let { onOpenFile(file.fileName, it, file.mimeType, file.sizeBytes) }
                },
            )
        }
    }
}

@Composable
private fun StatusChip(assignment: Assignment, now: Instant) {
    val scheme = MaterialTheme.colorScheme
    val status = assignment.submissionStatus
    val overdue = status == SubmissionStatus.NotSubmitted &&
        assignment.dueDate?.isBefore(now) == true
    val opensLater = assignment.allowSubmissionsFrom?.isAfter(now) == true
    val (label, fg, bg) = when {
        status is SubmissionStatus.Graded -> Triple("Valutato", scheme.onPrimaryContainer, scheme.primaryContainer)
        status is SubmissionStatus.Submitted -> Triple("Consegnato", scheme.onSecondaryContainer, scheme.secondaryContainer)
        status is SubmissionStatus.Draft -> Triple("Bozza", scheme.onTertiaryContainer, scheme.tertiaryContainer)
        overdue -> Triple("In ritardo", scheme.onErrorContainer, scheme.errorContainer)
        opensLater -> Triple("Non ancora aperto", scheme.onTertiaryContainer, scheme.tertiaryContainer)
        else -> Triple("Da consegnare", scheme.onSurface, scheme.surfaceContainerHigh)
    }
    AssistChip(
        onClick = {},
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg),
        border = null,
    )
}

// Single pinned action. Submission upload isn't supported in-app, so every path hands off to
// the e-learning page; brand-filled when there's something to hand in, tonal when it's just a
// reference. Mirrors the old full-screen action bar's wording.
@Composable
private fun ActionButton(assignment: Assignment, now: Instant, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val status = assignment.submissionStatus
    val due = assignment.dueDate
    val overdue = status == SubmissionStatus.NotSubmitted && due != null && due.isBefore(now)
    val opensLater = assignment.allowSubmissionsFrom?.isAfter(now) == true
    val lateWindowOpen = assignment.cutoffDate?.isAfter(now) == true
    val (label, submit) = when {
        status is SubmissionStatus.Graded || status is SubmissionStatus.Submitted -> "Apri su e-learning" to false
        status is SubmissionStatus.Draft -> "Completa consegna" to true
        overdue && lateWindowOpen -> "Consegna in ritardo" to true
        overdue || opensLater -> "Apri su e-learning" to false
        else -> "Consegna sul sito" to true
    }
    val content: @Composable () -> Unit = {
        Text(label, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = if (submit) Icons.AutoMirrored.Rounded.Send else Icons.Rounded.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
    if (submit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
            ),
        ) { Row(verticalAlignment = Alignment.CenterVertically) { content() } }
    } else {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
            ),
        ) { Row(verticalAlignment = Alignment.CenterVertically) { content() } }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Column(modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)) {
        Text(
            text = "❀ ${title.uppercase()}",
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
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

private fun formatGrade(grade: Double): String = when {
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(Locale.ITALIAN, "%.1f", grade)
}

private val DateFmt = DateTimeFormatter
    .ofPattern("EEE d MMM, HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
