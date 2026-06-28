package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.ext.parseQuestion
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.AttemptUiState
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

/**
 * The in-progress attempt as one page inside the quiz sheet, paginating server-side one Moodle
 * page at a time. The quiz name and "Pagina X di Y" ride the sheet's pinned header; the live
 * timer + progress strip and the connected Indietro/Avanti footer stay FIXED (like the
 * modifica-piano wizard) while only the question list slides between pages, so the transition
 * and the predictive-back scrub read cleanly. Page moves save a draft first.
 *
 * Predictive back scrubs the current page right and out during the gesture, with the backward
 * slide taking over on commit; previous page content isn't prefetched (server-side paging), so
 * the visual dismissal of the current page is the whole gesture feedback. On the first page,
 * system back asks to close the attempt, kept in-sheet as a confirm page. Submit / close don't
 * pop dialogs — they ask the sheet to morph onto its in-sheet confirm pages via
 * [onRequestSubmit] / [onRequestClose].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AttemptWizard(
    state: AttemptUiState.InProgress,
    quiz: Quiz?,
    attempt: QuizAttempt?,
    viewModel: QuizDetailViewModel,
    onRequestSubmit: () -> Unit,
    onRequestClose: () -> Unit,
) {
    val backProgress = remember { Animatable(0f) }

    PredictiveBackHandler(enabled = state.page.pageIndex > 0) { progress ->
        try {
            progress.collect { event -> backProgress.snapTo(event.progress) }
            backProgress.snapTo(0f)
            viewModel.onSaveDraft()
            viewModel.loadPage(state.page.pageIndex - 1)
        } catch (_: CancellationException) {
            backProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    BackHandler(enabled = state.page.pageIndex == 0) { onRequestClose() }

    val totalPages = remember(attempt?.layout) { attempt?.layout.totalPagesFromLayout() }
    val pageIndex = state.page.pageIndex
    val isLastPage = state.page.nextPage == null
    val showDots = totalPages != null && totalPages in 2..MAX_PAGE_DOTS

    val limit = quiz?.timeLimitSeconds?.takeIf { it > 0 }
    val start = attempt?.timeStart
    val showTimer = limit != null && start != null

    Column(Modifier.fillMaxWidth()) {
        if (showTimer || !showDots) {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 2.dp, end = 20.dp, bottom = 8.dp),
            ) {
                if (showTimer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        RemainingTimePill(start = start, limitSeconds = limit)
                    }
                }
                if (!showDots) {
                    if (showTimer) Spacer(Modifier.height(10.dp))
                    LinearWavyProgressIndicator(
                        progress = {
                            if (totalPages == null || totalPages == 0) 0f
                            else ((pageIndex + 1).toFloat() / totalPages).coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        AnimatedContent(
            targetState = state,
            contentKey = { it.page.pageIndex },
            transitionSpec = {
                val forward = targetState.page.pageIndex >= initialState.page.pageIndex
                val direction = if (forward) 1 else -1
                (slideInHorizontally { it / 4 * direction } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 * direction } + fadeOut())
            },
            label = "quiz_attempt_pages",
        ) { current ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .graphicsLayer {
                        val p = backProgress.value
                        val scale = 1f - 0.08f * p
                        scaleX = scale
                        scaleY = scale
                        translationX = size.width * 0.12f * p
                        alpha = 1f - 0.3f * p
                    },
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                current.page.questions.forEach { question ->
                    item("question_${question.slot}") {
                        val parsed = remember(question.html) { question.parseQuestion() }
                        QuestionCard(
                            question = question,
                            parsed = parsed,
                            answerFields = current.answers.firstOrNull { it.slot == question.slot }?.fields.orEmpty(),
                            flagged = question.slot in current.flagged,
                            readOnly = false,
                            onAnswer = { fields -> viewModel.setAnswer(question.slot, fields) },
                            onToggleFlag = { viewModel.toggleFlag(question.slot) },
                        )
                    }
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
                onRequestSubmit()
            },
        )
    }
}

/**
 * Countdown pill that ticks once a second, washes errorContainer when under a minute remains,
 * and clamps at 00:00 once expired.
 */
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

/**
 * The app's connected button pair (see StudyPlanEditPage/EventDetailSheet), fixed under the
 * sliding question list: an icon-only tonal back that springs in past the first page, and the
 * primary action morphing between Avanti and Consegna on the last page. The primary stays a
 * full pill while alone and flattens its start corners when the back button joins; its
 * primary/onPrimary colors are safe because the course accent computes its own readable
 * on-color.
 */
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
    val isOnline = LocalIsOnline.current
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val spatialOffsetSpec = motion.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = motion.defaultEffectsSpec<Float>()
    val backVisible = currentPage > 0
    val haptic = rememberHapticManager()

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
                    onClick = { haptic.tap(); onBack() },
                    enabled = isOnline,
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
                        contentDescription = stringResource(R.string.common_prev_page),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            val startCorner by animateDpAsState(
                targetValue = if (backVisible) 8.dp else 28.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "actionCorner",
            )
            Button(
                onClick = {
                    haptic.tap()
                    if (isLast) onSubmit() else onNext()
                },
                enabled = isOnline,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(
                    topStart = startCorner,
                    bottomStart = startCorner,
                    topEnd = 28.dp,
                    bottomEnd = 28.dp,
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
            ) {
                AnimatedContent(
                    targetState = isLast,
                    transitionSpec = {
                        (slideInHorizontally(spatialOffsetSpec) { it / 3 } + fadeIn(effectsFloatSpec)) togetherWith
                                (slideOutHorizontally(spatialOffsetSpec) { -it / 3 } + fadeOut(
                                    effectsFloatSpec
                                ))
                    },
                    label = "quizActionLabel",
                ) { last ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (last) {
                            Text(
                                stringResource(R.string.elearning_quiz_submit_attempt),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text(stringResource(R.string.common_next), fontWeight = FontWeight.SemiBold)
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

/** Page indicator: the active dot stretches into a primary pill on spring physics. */
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

/**
 * Stretching dots read at a glance only while they fit on one line; quizzes with more pages
 * show the wavy progress bar in the fixed strip instead.
 */
private const val MAX_PAGE_DOTS = 10

/** Moodle attempt layouts list slots with a 0 closing each page: "1,2,0,3,0" = 2 pages. */
private fun String?.totalPagesFromLayout(): Int? =
    this?.split(',')?.count { it.trim() == "0" }?.takeIf { it > 0 }
