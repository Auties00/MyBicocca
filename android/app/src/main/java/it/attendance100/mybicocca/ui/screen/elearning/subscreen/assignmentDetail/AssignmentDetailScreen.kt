package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AssignmentStatusHero
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AssignmentTimeline
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component.AttachmentCard
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
        val pullState = rememberPullToRefreshState()
        val pullScope = rememberCoroutineScope()
        var pullIndicatorVisible by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.surfaceContainer),
        ) {
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
                        CircularProgressIndicator()
                    }
                    is Loadable.Loaded -> {
                        val now = remember(loadable) { Instant.now() }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
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
        }
    }
}

private fun LazyListScope.assignmentItems(
    assignment: Assignment,
    now: Instant,
    onOpenFile: (url: String, fileName: String?) -> Unit,
) {
    val openPage = { assignment.pageUrl?.let { onOpenFile(it, null) } ?: Unit }

    item("title") {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text(
                text = "✦ COMPITO",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = assignment.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.6).sp,
            )
        }
    }

    item("hero") {
        AssignmentStatusHero(
            assignment = assignment,
            now = now,
            onOpenPage = openPage,
        )
    }

    item("timeline") {
        // Self-skips when the teacher set no dates at all.
        AssignmentTimeline(
            assignment = assignment,
            now = now,
            modifier = Modifier.padding(top = 10.dp),
        )
    }

    submissionItems(assignment, onOpenFile)
    instructionsItems(assignment, onOpenFile)
    feedbackItems(assignment)

    if (assignment.pageUrl != null) {
        item("open_on_site") {
            OpenOnSiteButton(onClick = { openPage() })
        }
    }
}

// Real Bicocca data: submitted hand-ins carry 1-2 files and almost never online text,
// so files lead the section and the text body is the exception.
private fun LazyListScope.submissionItems(
    assignment: Assignment,
    onOpenFile: (url: String, fileName: String?) -> Unit,
) {
    val status = assignment.submissionStatus as? SubmissionStatus.Submitted ?: return
    if (status.files.isEmpty() && status.onlineText.isNullOrBlank()) return

    item("submission_header") {
        DetailSectionLabel(
            title = "La tua consegna",
            mark = "❀",
            subtitle = maxAttemptsLabel(assignment.maxAttempts),
        )
    }
    status.files.forEachIndexed { index, file ->
        item("submission_file_$index") {
            AttachmentCard(
                file = file,
                onOpen = { file.fileUrl?.let { onOpenFile(it, file.fileName) } },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
    val onlineText = status.onlineText
    if (!onlineText.isNullOrBlank()) {
        item("submission_text") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                HtmlBody(
                    html = onlineText,
                    modifier = Modifier.padding(14.dp),
                )
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

    item("instructions_header") {
        DetailSectionLabel(title = "Istruzioni", mark = "✦")
    }
    if (intro != null) {
        item("instructions_body") {
            HtmlBody(
                html = intro,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
    }
    assignment.introFiles.forEachIndexed { index, file ->
        item("instructions_file_$index") {
            AttachmentCard(
                file = file,
                onOpen = { file.fileUrl?.let { onOpenFile(it, file.fileName) } },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

private fun LazyListScope.feedbackItems(assignment: Assignment) {
    val status = assignment.submissionStatus as? SubmissionStatus.Graded ?: return
    val feedback = status.feedback?.takeIf { it.isNotBlank() } ?: return

    item("feedback_header") {
        DetailSectionLabel(title = "Feedback del docente", mark = "❀")
    }
    item("feedback_body") {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            HtmlBody(
                html = feedback,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

@Composable
private fun DetailSectionLabel(
    title: String,
    mark: String,
    subtitle: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 22.dp, bottom = 8.dp),
    ) {
        Text(
            text = "$mark ${title.uppercase()}",
            color = scheme.tertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Composable
private fun OpenOnSiteButton(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = scheme.surfaceContainerLow,
            border = BorderStroke(1.5.dp, scheme.outline),
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = scheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Apri su e-learning",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

private fun maxAttemptsLabel(maxAttempts: Int?): String? = when {
    maxAttempts == null || maxAttempts < 1 -> null
    maxAttempts == 1 -> "È consentito un solo tentativo"
    else -> "Massimo $maxAttempts tentativi"
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
