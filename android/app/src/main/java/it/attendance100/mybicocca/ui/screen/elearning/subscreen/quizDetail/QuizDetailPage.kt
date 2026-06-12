package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.AttemptReviewContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.AttemptWizard
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.formatGradeValue
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.AttemptUiState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuizDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The whole quiz lives in a single sheet entry: BottomSheetSceneStrategy owns the container,
 * this drives an internal state machine over one morphing [SheetPagerHeader]. Level 0 is the
 * overview (facts + start/resume); "Storico" pushes the attempt list; starting/resuming/
 * reviewing an attempt morphs the SAME sheet onto the question wizard or the read-only review,
 * with the header subtitle reading "Pagina X di Y" while a question page is up.
 *
 * Submit and close don't pop dialogs — they morph onto in-sheet confirm pages. Those pages put
 * the encouraged choice in the emphasized brand slot (the confirm page's keep slot): "Consegna"
 * on the submit confirm, "Continua" on the close confirm, whose exit leaves the attempt in
 * progress server-side. Mid-attempt the sheet locks and turns a stray dismissal (scrim tap,
 * swipe) into the "chiudere il quiz?" confirm via the scene's [LocalSheetDismissControl], and
 * refuses dismissal outright while loading or submitting. System back walks the in-sheet pages
 * up one level; on the attempt page the wizard's own deeper handlers (step back / close
 * confirm) win.
 *
 * The last live attempt and review are retained so the outgoing page still has data to render
 * while the page transition slides it away; the attempt-loading spinner is held for a minimum
 * duration so a fast start or review load doesn't flash it; and the confirm flags reset once
 * the attempt flow settles back to idle, so the next open starts clean on the overview.
 */
@Composable
fun QuizDetailPage(
    quizId: Int,
    courseId: Int,
    viewModel: QuizDetailViewModel = hiltViewModel<QuizDetailViewModel, QuizDetailViewModel.Factory>(
        key = "quiz-sheet-$quizId",
        creationCallback = { it.create(SheetRoute.QuizDetail(quizId = quizId, courseId = courseId)) },
    ),
) {
    val snackbar = LocalAppSnackbarController.current
    val operationFailed = stringResource(R.string.elearning_quiz_operation_failed)
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is QuizDetailOneShotEvent.AttemptStarted -> Unit
                is QuizDetailOneShotEvent.AttemptSubmitted -> Unit
                is QuizDetailOneShotEvent.RefreshFailed ->
                    snackbar.showError(operationFailed, event.cause)
            }
        }
    }

    val quizLoadable by viewModel.quiz.collectAsStateWithLifecycle()
    val attemptsLoadable by viewModel.attempts.collectAsStateWithLifecycle()
    val bestGradeLoadable by viewModel.bestGrade.collectAsStateWithLifecycle()
    val attemptUi by viewModel.attemptUi.collectAsStateWithLifecycle()

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        val quiz = quizLoadable.valueOrNull()
        val attempts = attemptsLoadable.valueOrNull().orEmpty().filterNot { it.previewMode }
        val bestGrade = bestGradeLoadable.valueOrNull()

        var showHistory by remember { mutableStateOf(false) }
        var confirmSubmit by remember { mutableStateOf(false) }
        var confirmClose by remember { mutableStateOf(false) }

        val inProgress = attemptUi as? AttemptUiState.InProgress
        val reviewing = attemptUi as? AttemptUiState.Reviewing
        var lastInProgress by remember { mutableStateOf<AttemptUiState.InProgress?>(null) }
        if (inProgress != null) lastInProgress = inProgress
        var lastReview by remember { mutableStateOf<AttemptReview?>(null) }
        if (reviewing != null) lastReview = reviewing.review

        val showAttemptLoading = rememberMinDurationLoading(attemptUi == AttemptUiState.Loading)

        val page = when {
            confirmSubmit -> QuizSheetPage.ConfirmSubmit
            confirmClose -> QuizSheetPage.ConfirmClose
            attemptUi is AttemptUiState.Submitting -> QuizSheetPage.Submitting
            showAttemptLoading -> QuizSheetPage.AttemptLoading
            attemptUi is AttemptUiState.Reviewing -> QuizSheetPage.Review
            attemptUi is AttemptUiState.InProgress -> QuizSheetPage.Attempt
            showHistory -> QuizSheetPage.History
            else -> QuizSheetPage.Overview
        }

        LaunchedEffect(attemptUi) {
            if (attemptUi == AttemptUiState.Idle) {
                confirmSubmit = false
                confirmClose = false
            }
        }

        val currentAttempt = remember(attempts, inProgress) {
            attempts.firstOrNull { it.id == inProgress?.attemptId }
        }
        val totalPages = remember(currentAttempt?.layout) {
            currentAttempt?.layout?.split(',')?.count { it.trim() == "0" }?.takeIf { it > 0 }
        }
        val attemptSubtitle = inProgress?.let {
            if (totalPages != null) {
                stringResource(R.string.elearning_quiz_page_of, it.page.pageIndex + 1, totalPages)
            } else {
                stringResource(R.string.elearning_quiz_page, it.page.pageIndex + 1)
            }
        }

        val control = LocalSheetDismissControl.current
        val attemptActive = rememberUpdatedState(attemptUi is AttemptUiState.InProgress)
        val busy = rememberUpdatedState(
            attemptUi is AttemptUiState.Submitting || attemptUi == AttemptUiState.Loading
        )
        val onConfirmDismiss = remember {
            {
                when {
                    attemptActive.value -> {
                        confirmClose = true
                        false
                    }

                    busy.value -> false
                    else -> true
                }
            }
        }
        SideEffect {
            control?.gesturesEnabled = !attemptActive.value && !busy.value
            control?.confirmDismiss = onConfirmDismiss
        }

        val seekableState =
            remember { androidx.compose.animation.core.SeekableTransitionState(page) }
        val transition = androidx.compose.animation.core.rememberTransition(
            seekableState,
            label = "quiz_sheet_pages"
        )

        LaunchedEffect(page) {
            if (seekableState.targetState != page) {
                seekableState.animateTo(page)
            }
        }

        androidx.activity.compose.PredictiveBackHandler(
            enabled = page == QuizSheetPage.History ||
                    page == QuizSheetPage.Review ||
                    page == QuizSheetPage.ConfirmSubmit ||
                    page == QuizSheetPage.ConfirmClose,
        ) { progress ->
            try {
                val fallback = when (page) {
                    QuizSheetPage.ConfirmSubmit -> QuizSheetPage.Attempt
                    QuizSheetPage.ConfirmClose -> QuizSheetPage.Attempt
                    QuizSheetPage.Review -> if (showHistory) QuizSheetPage.History else QuizSheetPage.Overview
                    QuizSheetPage.History -> QuizSheetPage.Overview
                    else -> page
                }
                progress.collect { event ->
                    seekableState.seekTo(event.progress, targetState = fallback)
                }
                seekableState.animateTo(fallback)
                when (page) {
                    QuizSheetPage.ConfirmSubmit -> confirmSubmit = false
                    QuizSheetPage.ConfirmClose -> confirmClose = false
                    QuizSheetPage.Review -> viewModel.closeAttempt()
                    QuizSheetPage.History -> showHistory = false
                    else -> Unit
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                seekableState.animateTo(page)
            }
        }

        Column(modifier = Modifier.testTag(QuizDetailTestTags.ROOT)) {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    QuizSheetPage.Overview,
                    QuizSheetPage.AttemptLoading,
                    QuizSheetPage.Attempt,
                    QuizSheetPage.Submitting,
                    QuizSheetPage.Review -> quiz?.name ?: stringResource(R.string.elearning_quiz_title)

                    QuizSheetPage.History -> stringResource(R.string.elearning_quiz_history)
                    QuizSheetPage.ConfirmSubmit -> stringResource(R.string.elearning_quiz_confirm_submit_title)
                    QuizSheetPage.ConfirmClose -> stringResource(R.string.elearning_quiz_confirm_close_title)
                },
                subtitle = when (page) {
                    QuizSheetPage.History -> attemptsCountLabel(attempts.size)
                    QuizSheetPage.Attempt -> attemptSubtitle
                    QuizSheetPage.Submitting -> stringResource(R.string.elearning_quiz_submitting)
                    else -> null
                },
                onBack = when (page) {
                    QuizSheetPage.History -> ({ showHistory = false })
                    QuizSheetPage.Review -> viewModel::closeAttempt
                    QuizSheetPage.ConfirmSubmit -> ({ confirmSubmit = false })
                    QuizSheetPage.ConfirmClose -> ({ confirmClose = false })
                    else -> null
                },
            )
            transition.AnimatedContent(
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { it.key },
            ) { target ->
                when (target) {
                    QuizSheetPage.Overview ->
                        if (quiz == null) {
                            SheetLoadingIndicator(
                                label = stringResource(R.string.elearning_quiz_loading),
                                modifier = Modifier.testTag(QuizDetailTestTags.STATE_LOADING),
                            )
                        } else {
                            QuizOverviewPage(
                                quiz = quiz,
                                attempts = attempts,
                                bestGrade = bestGrade,
                                onStartAttempt = viewModel::onStartAttempt,
                                onResumeAttempt = { viewModel.resumeAttempt(AttemptId(it)) },
                                onShowHistory = { showHistory = true },
                                modifier = Modifier.testTag(QuizDetailTestTags.OVERVIEW),
                            )
                        }

                    QuizSheetPage.History -> QuizHistoryPage(
                        attempts = attempts,
                        onResumeAttempt = { viewModel.resumeAttempt(AttemptId(it)) },
                        onReviewAttempt = { viewModel.viewReview(AttemptId(it)) },
                    )

                    QuizSheetPage.AttemptLoading ->
                        SheetLoadingIndicator(label = stringResource(R.string.elearning_quiz_loading_attempt))

                    QuizSheetPage.Attempt -> lastInProgress?.let { state ->
                        AttemptWizard(
                            state = state,
                            quiz = quiz,
                            attempt = attempts.firstOrNull { it.id == state.attemptId },
                            viewModel = viewModel,
                            onRequestSubmit = { confirmSubmit = true },
                            onRequestClose = { confirmClose = true },
                        )
                    }

                    QuizSheetPage.Submitting ->
                        SheetLoadingIndicator(label = stringResource(R.string.elearning_quiz_submitting))

                    QuizSheetPage.Review -> lastReview?.let { review ->
                        AttemptReviewContent(
                            review = review,
                            onClose = viewModel::closeAttempt,
                        )
                    }

                    QuizSheetPage.ConfirmSubmit -> SheetConfirmPage(
                        body = stringResource(R.string.elearning_quiz_confirm_submit_body),
                        onConfirm = { confirmSubmit = false },
                        onKeep = {
                            confirmSubmit = false
                            viewModel.onSubmit()
                        },
                        confirmLabel = stringResource(R.string.common_cancel),
                        keepLabel = stringResource(R.string.elearning_quiz_submit_attempt),
                    )

                    QuizSheetPage.ConfirmClose -> SheetConfirmPage(
                        body = stringResource(R.string.elearning_quiz_confirm_close_body),
                        onConfirm = {
                            confirmClose = false
                            viewModel.onSaveDraft()
                            viewModel.closeAttempt()
                        },
                        onKeep = { confirmClose = false },
                        confirmLabel = stringResource(R.string.common_exit),
                        keepLabel = stringResource(R.string.common_continue),
                    )
                }
            }
        }
    }
}

/**
 * The in-sheet pages of the quiz modal. [depth] drives the header's directional morph; the
 * confirm pages sit one level deeper than the attempt they interrupt.
 */
private enum class QuizSheetPage(val depth: Int, val key: String) {
    Overview(0, "overview"),
    History(1, "history"),
    AttemptLoading(1, "attempt_loading"),
    Attempt(1, "attempt"),
    Submitting(1, "submitting"),
    Review(1, "review"),
    ConfirmSubmit(2, "confirm_submit"),
    ConfirmClose(2, "confirm_close"),
}

/**
 * The overview level of the quiz sheet: icon fact rows (best grade, attempts used, time limit,
 * availability, instructions) above the start/resume action row. Startability accounts for the
 * open/close window, the attempt budget, and any resumable in-progress attempt.
 */
@Composable
private fun QuizOverviewPage(
    quiz: Quiz,
    attempts: List<QuizAttempt>,
    bestGrade: BestGrade?,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (Int) -> Unit,
    onShowHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val now = remember(quiz, attempts) { Instant.now() }

    val resumable = attempts.firstOrNull { it.state == AttemptState.InProgress }
    val closed = quiz.timeClose != null && now.isAfter(quiz.timeClose)
    val notYetOpen = quiz.timeOpen != null && now.isBefore(quiz.timeOpen)
    val attemptsLeft = quiz.maxAttempts.let { max -> max == null || max <= 0 || attempts.size < max }
    val canStart = resumable != null || (!closed && !notYetOpen && attemptsLeft)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (bestGrade?.grade != null) {
                IconRow(
                    icon = Icons.Outlined.EmojiEvents,
                    label = stringResource(R.string.elearning_quiz_best_grade_label),
                    value = buildString {
                        append(formatGradeValue(bestGrade.grade))
                        bestGrade.maxGrade?.takeIf { it > 0 }?.let { append(" / ${formatGradeValue(it)}") }
                    },
                )
            }
            IconRow(
                icon = Icons.Outlined.Repeat,
                label = stringResource(R.string.elearning_quiz_attempts_label),
                value = attemptsValue(quiz, attempts.size),
            )
            quiz.timeLimitSeconds?.takeIf { it > 0 }?.let { limit ->
                IconRow(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.elearning_quiz_time_label),
                    value = stringResource(R.string.elearning_quiz_time_minutes, limit / 60),
                )
            }
            availabilityValue(quiz, now)?.let { (label, value) ->
                IconRow(icon = Icons.Outlined.Schedule, label = label, value = value)
            }
            quiz.intro?.takeIf { it.isNotBlank() }?.let { intro ->
                IconRow(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    label = stringResource(R.string.elearning_quiz_instructions_label),
                ) {
                    HtmlBody(html = intro, color = scheme.onSurface)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        QuizActionRow(
            canStart = canStart,
            primaryLabel = primaryActionLabel(resumable != null, notYetOpen, closed, attemptsLeft),
            primaryIsResume = resumable != null,
            hasHistory = attempts.isNotEmpty(),
            onPrimary = { if (resumable != null) onResumeAttempt(resumable.id.value) else onStartAttempt() },
            onShowHistory = onShowHistory,
        )
    }
}

/**
 * The full attempt list, newest first, as a segmented Material Expressive group. Tapping a
 * finished attempt opens its review; an in-progress one resumes it.
 */
@Composable
private fun QuizHistoryPage(
    attempts: List<QuizAttempt>,
    onResumeAttempt: (Int) -> Unit,
    onReviewAttempt: (Int) -> Unit,
) {
    val ordered = remember(attempts) { attempts.sortedByDescending { it.attemptNumber } }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp)
            .testTag(QuizDetailTestTags.HISTORY_LIST),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(ordered, key = { _, attempt -> attempt.id.value }) { index, attempt ->
            AttemptRow(
                attempt = attempt,
                isFirst = index == 0,
                isLast = index == ordered.lastIndex,
                modifier = Modifier.testTag(QuizDetailTestTags.attempt(attempt.id.value)),
                onClick = {
                    if (attempt.state == AttemptState.InProgress) onResumeAttempt(attempt.id.value)
                    else onReviewAttempt(attempt.id.value)
                },
            )
        }
    }
}

/**
 * Segmented attempt row: 28dp corners cap the group's ends, 6dp where rows touch — same
 * language as the maps building list. Carries the attempt number in a circle, state and date
 * as subtitle, the grade trailing, and a play glyph when the attempt is resumable.
 */
@Composable
private fun AttemptRow(
    attempt: QuizAttempt,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val inProgress = attempt.state == AttemptState.InProgress
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = LocalIsOnline.current, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = scheme.primaryContainer) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = attempt.attemptNumber.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.elearning_quiz_attempt_number, attempt.attemptNumber),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = scheme.onSurface,
                )
                Text(
                    text = attemptSubtitle(attempt),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            attempt.sumGrades?.let { grade ->
                Text(
                    text = formatGradeValue(grade),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
            }
            Icon(
                imageVector = if (inProgress) Icons.Filled.PlayArrow else Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(if (inProgress) 20.dp else 24.dp),
            )
        }
    }
}

@Composable
private fun IconRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    valueContent: (@Composable () -> Unit)? = null,
) {
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
            if (valueContent != null) {
                valueContent()
            } else {
                Text(
                    text = value.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

/**
 * Connected button group: a wide brand leading button (start / resume) and a neutral tonal
 * "Storico" trailing half when there are attempts to look back at. Collapses to a single
 * full-width button when there's no history yet.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuizActionRow(
    canStart: Boolean,
    primaryLabel: String,
    primaryIsResume: Boolean,
    hasHistory: Boolean,
    onPrimary: () -> Unit,
    onShowHistory: () -> Unit,
) {
    val isOnline = LocalIsOnline.current
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (hasHistory) {
            BrandButton(
                label = primaryLabel,
                icon = if (primaryIsResume) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                bg = brandBg,
                fg = brandFg,
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                onClick = onPrimary,
                enabled = canStart && isOnline,
                modifier = Modifier
                    .weight(1.4f)
                    .testTag(QuizDetailTestTags.PRIMARY_ACTION),
            )
            FilledTonalButton(
                onClick = onShowHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuizDetailTestTags.HISTORY_ACTION),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.elearning_quiz_history), fontWeight = FontWeight.SemiBold)
            }
        } else {
            BrandButton(
                label = primaryLabel,
                icon = if (primaryIsResume) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                bg = brandBg,
                fg = brandFg,
                shape = RoundedCornerShape(28.dp),
                onClick = onPrimary,
                enabled = canStart && isOnline,
                modifier = Modifier
                    .weight(1f)
                    .testTag(QuizDetailTestTags.PRIMARY_ACTION),
            )
        }
    }
}

@Composable
private fun BrandButton(
    label: String,
    icon: ImageVector,
    bg: Color,
    fg: Color,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun attemptsCountLabel(count: Int): String =
    pluralStringResource(R.plurals.elearning_quiz_attempts_count, count, count)

@Composable
private fun attemptSubtitle(attempt: QuizAttempt): String {
    val state = when (attempt.state) {
        AttemptState.InProgress -> stringResource(R.string.elearning_quiz_state_in_progress)
        AttemptState.Overdue -> stringResource(R.string.elearning_quiz_state_overdue)
        AttemptState.Finished -> stringResource(R.string.elearning_quiz_state_finished)
        AttemptState.Abandoned -> stringResource(R.string.elearning_quiz_state_abandoned)
        AttemptState.Unknown -> stringResource(R.string.elearning_quiz_state_unknown)
    }
    val instant = attempt.timeFinish ?: attempt.timeStart
    return if (instant != null) "$state · ${DateFmt.format(instant)}" else state
}

@Composable
private fun primaryActionLabel(
    resumable: Boolean,
    notYetOpen: Boolean,
    closed: Boolean,
    attemptsLeft: Boolean,
): String = when {
    resumable -> stringResource(R.string.elearning_quiz_resume)
    notYetOpen -> stringResource(R.string.elearning_quiz_not_open_yet)
    closed -> stringResource(R.string.elearning_quiz_closed)
    !attemptsLeft -> stringResource(R.string.elearning_quiz_attempts_exhausted)
    else -> stringResource(R.string.elearning_quiz_start_attempt)
}

@Composable
private fun attemptsValue(quiz: Quiz, used: Int): String {
    val max = quiz.maxAttempts
    return when {
        max != null && max > 0 -> stringResource(R.string.elearning_quiz_attempts_used, used, max)
        used == 0 -> stringResource(R.string.elearning_quiz_attempts_unlimited)
        else -> stringResource(R.string.elearning_quiz_attempts_used_unlimited, used)
    }
}

/** (label, value) for the availability fact row, or null when the quiz is always open. */
@Composable
private fun availabilityValue(quiz: Quiz, now: Instant): Pair<String, String>? {
    quiz.timeOpen?.takeIf { it.isAfter(now) }?.let {
        return stringResource(R.string.elearning_quiz_opening_label) to
            stringResource(R.string.elearning_quiz_opens, DateFmt.format(it))
    }
    quiz.timeClose?.let {
        return if (it.isAfter(now)) {
            stringResource(R.string.elearning_quiz_closing_label) to
                stringResource(R.string.elearning_quiz_closes, DateFmt.format(it))
        } else {
            stringResource(R.string.elearning_quiz_closing_label) to
                stringResource(R.string.elearning_quiz_closed_on, DateFmt.format(it))
        }
    }
    return null
}

private val DateFmt: DateTimeFormatter = DateTimeFormatter
    .ofPattern("EEE d MMM, HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())
