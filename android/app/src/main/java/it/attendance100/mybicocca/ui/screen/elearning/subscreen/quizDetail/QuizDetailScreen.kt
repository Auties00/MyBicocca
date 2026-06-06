package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.QuestionCard
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.formatGradeValue
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.ext.parseQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.AttemptUiState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuizDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuizDetailScreen(
    quizId: Int,
    courseId: Int,
    onExit: () -> Unit = {},
    viewModel: QuizDetailViewModel = hiltViewModel(),
) {
    val snackbar = LocalAppSnackbarController.current
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is QuizDetailOneShotEvent.AttemptStarted -> Unit
                is QuizDetailOneShotEvent.AttemptSubmitted -> snackbar.showInfo("Tentativo inviato")
                is QuizDetailOneShotEvent.RefreshFailed ->
                    snackbar.showError("Operazione non riuscita", event.cause)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") quizId

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        val scheme = MaterialTheme.colorScheme
        val quizLoadable by viewModel.quiz.collectAsStateWithLifecycle()
        val attemptsLoadable by viewModel.attempts.collectAsStateWithLifecycle()
        val attemptUi by viewModel.attemptUi.collectAsStateWithLifecycle()

        // The overview is the sheet; this screen only hosts the attempt/review. Once an action
        // has moved past the initial Idle, returning to Idle (back, close, or a load failure)
        // means we're done — pop the screen.
        var armed by remember { mutableStateOf(false) }
        LaunchedEffect(attemptUi) {
            if (attemptUi != AttemptUiState.Idle) armed = true
            else if (armed) onExit()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.surfaceContainer),
        ) {
            AnimatedContent(
                targetState = attemptUi,
                contentKey = { it.contentKey() },
                transitionSpec = {
                    val forward = transitionGoesForward(initialState, targetState)
                    val direction = if (forward) 1 else -1
                    (slideInHorizontally { it / 4 * direction } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 * direction } + fadeOut())
                },
                label = "quiz-detail-state",
                modifier = Modifier.fillMaxSize(),
            ) { state ->
                when (state) {
                    // Idle is transient here (the requested action kicks in on init); the
                    // armed-pop effect above closes the screen if we ever settle back on it.
                    AttemptUiState.Idle,
                    AttemptUiState.Loading -> CenteredLoading()

                    is AttemptUiState.InProgress -> AttemptContent(
                        state = state,
                        quiz = quizLoadable.valueOrNull(),
                        attempt = attemptsLoadable.valueOrNull().orEmpty()
                            .firstOrNull { it.id == state.attemptId },
                        viewModel = viewModel,
                    )

                    is AttemptUiState.Submitting -> Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(72.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Invio in corso…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }

                    is AttemptUiState.Reviewing -> ReviewContent(
                        review = state.review,
                        onClose = viewModel::closeAttempt,
                    )
                }
            }
        }
    }
}

private fun AttemptUiState.contentKey(): String = when (this) {
    AttemptUiState.Idle -> "idle"
    AttemptUiState.Loading -> "loading"
    is AttemptUiState.InProgress -> "page-${page.pageIndex}"
    is AttemptUiState.Submitting -> "submitting"
    is AttemptUiState.Reviewing -> "review"
}

// Forward = deeper into the flow (overview -> attempt -> next page -> review);
// backward only when stepping to a previous question page or closing back to overview.
private fun transitionGoesForward(from: AttemptUiState, to: AttemptUiState): Boolean = when {
    to is AttemptUiState.InProgress && from is AttemptUiState.InProgress ->
        to.page.pageIndex >= from.page.pageIndex
    to == AttemptUiState.Idle -> false
    else -> true
}

@Composable
private fun CenteredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        LoadingIndicator(modifier = Modifier.size(72.dp))
    }
}

// ---------- In-progress attempt ----------

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttemptContent(
    state: AttemptUiState.InProgress,
    quiz: Quiz?,
    attempt: QuizAttempt?,
    viewModel: QuizDetailViewModel,
) {
    val scheme = MaterialTheme.colorScheme
    var confirmSubmit by remember { mutableStateOf(false) }

    // Leaving the attempt saves the draft: students abandon mid-quiz constantly
    // (28 of 78 real attempts surveyed were paused in progress).
    BackHandler {
        viewModel.onSaveDraft()
        viewModel.closeAttempt()
    }

    val totalPages = remember(attempt?.layout) { attempt?.layout.totalPagesFromLayout() }
    val pageIndex = state.page.pageIndex
    val isLastPage = state.page.nextPage == null
    // The stretching dots read at a glance only while they fit on one line; longer
    // quizzes keep the wavy bar in the header instead.
    val showDots = totalPages != null && totalPages in 2..MAX_PAGE_DOTS

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (quiz != null) {
                        Text(
                            text = quiz.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = if (totalPages != null) {
                            "Pagina ${pageIndex + 1} di $totalPages"
                        } else {
                            "Pagina ${pageIndex + 1}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
                val limit = quiz?.timeLimitSeconds?.takeIf { it > 0 }
                val start = attempt?.timeStart
                if (limit != null && start != null) {
                    RemainingTimePill(start = start, limitSeconds = limit)
                }
            }
            if (!showDots) {
                Spacer(Modifier.height(10.dp))
                LinearWavyProgressIndicator(
                    progress = {
                        if (totalPages == null || totalPages == 0) 0f
                        else ((pageIndex + 1).toFloat() / totalPages).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.page.questions.forEach { question ->
                item("question_${question.slot}") {
                    val parsed = remember(question.html) { question.parseQuestion() }
                    QuestionCard(
                        question = question,
                        parsed = parsed,
                        answerFields = state.answers.firstOrNull { it.slot == question.slot }?.fields.orEmpty(),
                        flagged = question.slot in state.flagged,
                        readOnly = false,
                        onAnswer = { fields -> viewModel.setAnswer(question.slot, fields) },
                        onToggleFlag = { viewModel.toggleFlag(question.slot) },
                    )
                }
            }
        }

        QuizWizardBottomBar(
            pageCount = totalPages ?: 0,
            currentPage = pageIndex,
            showDots = showDots,
            isLast = isLastPage,
            onBack = {
                viewModel.onSaveDraft()
                viewModel.loadPage(pageIndex - 1)
            },
            onNext = {
                viewModel.onSaveDraft()
                state.page.nextPage?.let(viewModel::loadPage)
            },
            onSubmit = {
                viewModel.onSaveDraft()
                confirmSubmit = true
            },
        )
    }

    if (confirmSubmit) {
        AlertDialog(
            onDismissRequest = { confirmSubmit = false },
            title = { Text("Consegnare il tentativo?") },
            text = { Text("La consegna è definitiva: le risposte di questo tentativo non potranno più essere modificate.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmSubmit = false
                        viewModel.onSubmit()
                    },
                ) {
                    Text("Consegna")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSubmit = false }) { Text("Annulla") }
            },
        )
    }
}

@Composable
private fun RemainingTimePill(start: Instant, limitSeconds: Long) {
    val scheme = MaterialTheme.colorScheme
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(start) {
        while (true) {
            delay(1_000)
            now = Instant.now()
        }
    }
    val remaining = Duration.between(now, start.plusSeconds(limitSeconds))
    val expired = remaining.isNegative || remaining.isZero
    val critical = !expired && remaining.toMinutes() < 1
    val label = if (expired) {
        "00:00"
    } else {
        "%02d:%02d".format(remaining.toMinutes(), remaining.seconds % 60)
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (expired || critical) scheme.errorContainer else scheme.surfaceContainerHigh,
    ) {
        Text(
            text = label,
            color = if (expired || critical) scheme.onErrorContainer else scheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// The app's connected button pair (see StudyPlanEditScreen/EventDetailSheet): an
// icon-only tonal back that springs in past the first page, and the primary action
// morphing between Avanti and Consegna on the last page.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuizWizardBottomBar(
    pageCount: Int,
    currentPage: Int,
    showDots: Boolean,
    isLast: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val spatialOffsetSpec = motion.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = motion.defaultEffectsSpec<Float>()
    val backVisible = currentPage > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showDots) {
            PageDots(pageCount = pageCount, currentPage = currentPage)
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnimatedVisibility(
                visible = backVisible,
                enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
            ) {
                FilledTonalButton(
                    onClick = onBack,
                    modifier = Modifier
                        .width(64.dp)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Pagina precedente",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // The primary action stays a full pill alone and flattens its start corners
            // when the back button joins the pair.
            val startCorner by animateDpAsState(
                targetValue = if (backVisible) 8.dp else 28.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "actionCorner",
            )
            Button(
                onClick = { if (isLast) onSubmit() else onNext() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(
                    topStart = startCorner,
                    bottomStart = startCorner,
                    topEnd = 28.dp,
                    bottomEnd = 28.dp,
                ),
                // The course accent computes its own readable on-color.
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                AnimatedContent(
                    targetState = isLast,
                    transitionSpec = {
                        (slideInHorizontally(spatialOffsetSpec) { it / 3 } + fadeIn(effectsFloatSpec)) togetherWith
                            (slideOutHorizontally(spatialOffsetSpec) { -it / 3 } + fadeOut(effectsFloatSpec))
                    },
                    label = "quizActionLabel",
                ) { last ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (last) {
                            Text("Consegna", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text("Avanti", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Page indicator: the active dot stretches into a primary pill on spring physics.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(
                targetValue = if (active) 26.dp else 8.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (active) scheme.primary else scheme.surfaceContainerHighest,
                animationSpec = motion.defaultEffectsSpec(),
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

private const val MAX_PAGE_DOTS = 10

// Moodle attempt layouts list slots with a 0 closing each page: "1,2,0,3,0" = 2 pages.
private fun String?.totalPagesFromLayout(): Int? =
    this?.split(',')?.count { it.trim() == "0" }?.takeIf { it > 0 }

// ---------- Review ----------

@Composable
private fun ReviewContent(
    review: AttemptReview,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    BackHandler(onBack = onClose)
    val questions = remember(review) { review.pages.flatMap { it.questions } }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
        ) {
            item("review_hero") {
                ReviewGradeHero(review = review, questions = questions)
            }
            val feedback = review.feedback?.takeIf { it.isNotBlank() }
            if (feedback != null) {
                item("review_feedback") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = scheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        HtmlBody(html = feedback, modifier = Modifier.padding(14.dp))
                    }
                }
            }
            questions.forEach { question ->
                item("review_question_${question.slot}") {
                    val parsed = remember(question.html) { question.parseQuestion() }
                    QuestionCard(
                        question = question,
                        parsed = parsed,
                        answerFields = emptyMap(),
                        flagged = false,
                        readOnly = true,
                        onAnswer = {},
                        onToggleFlag = null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Same closing bar as the attempt wizard, with the single full-pill action.
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
            ),
        ) {
            Text("Chiudi revisione", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReviewGradeHero(review: AttemptReview, questions: List<AttemptQuestion>) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = "❀ REVISIONE",
            color = scheme.tertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
        )
        val grade = review.gradeFormatted?.toDoubleOrNull()
        if (grade != null || review.gradeFormatted != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = grade?.let(::formatGradeValue) ?: review.gradeFormatted.orEmpty(),
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    lineHeight = 44.sp,
                    letterSpacing = (-2).sp,
                )
                review.maxGrade?.takeIf { it > 0 }?.let { max ->
                    Text(
                        text = "/ ${formatGradeValue(max)}",
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
        val right = questions.count { it.state == "gradedright" }
        val partial = questions.count { it.state == "gradedpartial" }
        val wrong = questions.count { it.state == "gradedwrong" }
        val skipped = questions.count { it.state == "gaveup" }
        val summary = listOfNotNull(
            right.takeIf { it > 0 }?.let { "$it corrette" },
            partial.takeIf { it > 0 }?.let { "$it parziali" },
            wrong.takeIf { it > 0 }?.let { "$it sbagliate" },
            skipped.takeIf { it > 0 }?.let { "$it senza risposta" },
        )
        if (summary.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
