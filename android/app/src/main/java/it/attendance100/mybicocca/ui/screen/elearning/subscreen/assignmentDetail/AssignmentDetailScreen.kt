package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AssignmentStatusTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AssignmentTimeline
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AttachmentTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.SegmentHeaderTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.SegmentHtmlTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Compito page in the Piano di Studi connected-segment language: a status group
// (header tile + lifecycle date tiles), one segment group per section, and the plan
// compiler's full-pill action bar pinned at the bottom.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AssignmentDetailScreen(
    assignId: Int,
    courseId: Int,
    onOpenFile: (url: String, fileName: String?) -> Unit = { _, _ -> },
    viewModel: AssignmentDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is AssignmentDetailOneShotEvent.OpenFile -> {
                    onOpenFile(event.url, event.fileName)
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }
                is AssignmentDetailOneShotEvent.RefreshFailed ->
                    snackbar.showError("Sincronizzazione del compito non riuscita", event.cause)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") assignId

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        val scheme = MaterialTheme.colorScheme
        val assignmentLoadable by viewModel.assignment.collectAsStateWithLifecycle()
        val now = remember(assignmentLoadable) { Instant.now() }
        val pullState = rememberPullToRefreshState()
        val pullScope = rememberCoroutineScope()
        var pullIndicatorVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.surface),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PullToRefreshBox(
                    isRefreshing = pullIndicatorVisible,
                    onRefresh = {
                        pullIndicatorVisible = true
                        viewModel.pullToRefresh()
                        pullScope.launch {
                            delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                            pullIndicatorVisible = false
                        }
                    },
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val loadable = assignmentLoadable) {
                        Loadable.NotYetLoaded -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator(modifier = Modifier.size(72.dp))
                        }
                        is Loadable.Loaded -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            assignmentItems(
                                assignment = loadable.value,
                                now = now,
                                onOpenFile = viewModel::openFile,
                            )
                        }
                    }
                }
            }

            val assignment = (assignmentLoadable as? Loadable.Loaded)?.value
            if (assignment?.pageUrl != null) {
                AssignmentActionBar(
                    assignment = assignment,
                    now = now,
                    onOpenPage = { assignment.pageUrl?.let { viewModel.openFile(it, null) } },
                )
            }
        }
    }
}

private fun LazyListScope.assignmentItems(
    assignment: Assignment,
    now: Instant,
    onOpenFile: (url: String, fileName: String?) -> Unit,
) {
    item("title") {
        Text(
            text = assignment.name,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    // Status header + lifecycle dates read as one segment group, like a plan rule
    // with its course tiles.
    item("status") {
        val hasDates = assignment.allowSubmissionsFrom != null ||
            assignment.dueDate != null ||
            assignment.cutoffDate != null
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AssignmentStatusTile(assignment = assignment, now = now, isLast = !hasDates)
            AssignmentTimeline(assignment = assignment, now = now)
        }
    }

    submissionItems(assignment, onOpenFile)
    instructionsItems(assignment, onOpenFile)
    feedbackItems(assignment)
}

// Real Bicocca data: submitted hand-ins carry 1-2 files and almost never online text,
// so files lead the section and the text body is the exception.
private fun LazyListScope.submissionItems(
    assignment: Assignment,
    onOpenFile: (url: String, fileName: String?) -> Unit,
) {
    val status = assignment.submissionStatus as? SubmissionStatus.Submitted ?: return
    if (status.files.isEmpty() && status.onlineText.isNullOrBlank()) return

    item("submission") {
        val onlineText = status.onlineText?.takeIf { it.isNotBlank() }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SegmentHeaderTile(
                title = "La tua consegna",
                subtitle = maxAttemptsLabel(assignment.maxAttempts),
            )
            status.files.forEachIndexed { index, file ->
                AttachmentTile(
                    file = file,
                    onOpen = { file.fileUrl?.let { onOpenFile(it, file.fileName) } },
                    isLast = onlineText == null && index == status.files.lastIndex,
                )
            }
            if (onlineText != null) {
                SegmentHtmlTile(html = onlineText, isLast = true)
            }
        }
    }
}

private fun LazyListScope.instructionsItems(
    assignment: Assignment,
    onOpenFile: (url: String, fileName: String?) -> Unit,
) {
    val intro = assignment.intro?.takeIf { it.isNotBlank() }
    if (intro == null && assignment.introFiles.isEmpty()) return

    item("instructions") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SegmentHeaderTile(title = "Istruzioni")
            if (intro != null) {
                SegmentHtmlTile(html = intro, isLast = assignment.introFiles.isEmpty())
            }
            assignment.introFiles.forEachIndexed { index, file ->
                AttachmentTile(
                    file = file,
                    onOpen = { file.fileUrl?.let { onOpenFile(it, file.fileName) } },
                    isLast = index == assignment.introFiles.lastIndex,
                )
            }
        }
    }
}

private fun LazyListScope.feedbackItems(assignment: Assignment) {
    val status = assignment.submissionStatus as? SubmissionStatus.Graded ?: return
    val feedback = status.feedback?.takeIf { it.isNotBlank() } ?: return

    item("feedback") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SegmentHeaderTile(title = "Feedback del docente")
            SegmentHtmlTile(html = feedback, isLast = true)
        }
    }
}

// The plan compiler's action bar language: one full-pill 56dp button pinned under the
// list — accent-filled when there's a submission to push, tonal when the site is just
// a reference. CourseDetailTheme computes a contrast-correct onPrimary for the accent.
@Composable
private fun AssignmentActionBar(
    assignment: Assignment,
    now: Instant,
    onOpenPage: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val status = assignment.submissionStatus
    val due = assignment.dueDate
    val overdue = status == SubmissionStatus.NotSubmitted && due != null && due.isBefore(now)
    val opensLater = assignment.allowSubmissionsFrom?.isAfter(now) == true
    val lateWindowOpen = assignment.cutoffDate?.isAfter(now) == true

    val (label, submit) = when {
        status is SubmissionStatus.Graded || status is SubmissionStatus.Submitted ->
            "Apri su e-learning" to false
        status is SubmissionStatus.Draft -> "Completa consegna" to true
        overdue && lateWindowOpen -> "Consegna in ritardo" to true
        overdue || opensLater -> "Apri su e-learning" to false
        else -> "Consegna sul sito" to true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
    ) {
        val content: @Composable RowScope.() -> Unit = {
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
                onClick = onOpenPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
                content = content,
            )
        } else {
            FilledTonalButton(
                onClick = onOpenPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
                content = content,
            )
        }
    }
}

private fun maxAttemptsLabel(maxAttempts: Int?): String? = when {
    maxAttempts == null || maxAttempts < 1 -> null
    maxAttempts == 1 -> "È consentito un solo tentativo"
    else -> "Massimo $maxAttempts tentativi"
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
