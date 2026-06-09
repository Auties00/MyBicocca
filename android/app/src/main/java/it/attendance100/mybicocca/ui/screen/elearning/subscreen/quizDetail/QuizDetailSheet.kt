package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.formatGradeValue
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The quiz overview as a nested-pager modal, mirroring the maps "Edifici" sheet: a single
// PredictiveModalBottomSheet with a pinned morphing header over a two-level body pager.
// Level 0 is the overview (facts + start/resume); "Storico" pushes level 1, the full list of
// attempts, each tappable to review (finished) or resume (in progress). The sheet never hosts
// the quiz-taking itself — that navigates to the full-screen attempt/review.
// The quiz overview as a single sheet entry: BottomSheetSceneStrategy owns the container; this
// keeps its own two-level (overview <-> Storico) pager and morphing header.
@Composable
fun QuizDetailPage(
    quizId: Int,
    courseId: Int,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (attemptId: Int) -> Unit,
    onReviewAttempt: (attemptId: Int) -> Unit,
    viewModel: QuizDetailViewModel = hiltViewModel<QuizDetailViewModel, QuizDetailViewModel.Factory>(
        key = "quiz-sheet-$quizId",
        creationCallback = { it.create(AppRoute.QuizDetail(quizId = quizId, courseId = courseId)) },
    ),
) {
    val quizLoadable by viewModel.quiz.collectAsStateWithLifecycle()
    val attemptsLoadable by viewModel.attempts.collectAsStateWithLifecycle()
    val bestGradeLoadable by viewModel.bestGrade.collectAsStateWithLifecycle()

    CourseDetailTheme(courseId = remember(courseId) { CourseId(courseId) }) {
        run {
            val quiz = quizLoadable.valueOrNull()
            val attempts = attemptsLoadable.valueOrNull().orEmpty().filterNot { it.previewMode }
            val bestGrade = bestGradeLoadable.valueOrNull()

            var page by remember { mutableIntStateOf(0) }
            BackHandler(enabled = page == 1) { page = 0 }

            Column {
                SheetPagerHeader(
                    depth = page,
                    title = if (page == 0) (quiz?.name ?: "Quiz") else "Storico",
                    subtitle = if (page == 1) attemptsCountLabel(attempts.size) else null,
                    onBack = if (page == 1) ({ page = 0 }) else null,
                )
                AnimatedContent(
                    targetState = page,
                    transitionSpec = { sheetPageTransform(forward = targetState >= initialState) },
                    contentKey = { it },
                    label = "quiz_sheet_pages",
                ) { target ->
                    when {
                        quiz == null -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }

                        target == 0 -> QuizOverviewPage(
                            quiz = quiz,
                            attempts = attempts,
                            bestGrade = bestGrade,
                            onStartAttempt = onStartAttempt,
                            onResumeAttempt = onResumeAttempt,
                            onShowHistory = { page = 1 },
                        )

                        else -> QuizHistoryPage(
                            attempts = attempts,
                            onResumeAttempt = onResumeAttempt,
                            onReviewAttempt = onReviewAttempt,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizOverviewPage(
    quiz: Quiz,
    attempts: List<QuizAttempt>,
    bestGrade: BestGrade?,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (Int) -> Unit,
    onShowHistory: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val now = remember(quiz, attempts) { Instant.now() }

    val resumable = attempts.firstOrNull { it.state == AttemptState.InProgress }
    val closed = quiz.timeClose != null && now.isAfter(quiz.timeClose)
    val notYetOpen = quiz.timeOpen != null && now.isBefore(quiz.timeOpen)
    val attemptsLeft = quiz.maxAttempts.let { max -> max == null || max <= 0 || attempts.size < max }
    val canStart = resumable != null || (!closed && !notYetOpen && attemptsLeft)

    Column(
        modifier = Modifier
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
                    label = "VOTO MIGLIORE",
                    value = buildString {
                        append(formatGradeValue(bestGrade.grade))
                        bestGrade.maxGrade?.takeIf { it > 0 }?.let { append(" / ${formatGradeValue(it)}") }
                    },
                )
            }
            IconRow(
                icon = Icons.Outlined.Repeat,
                label = "TENTATIVI",
                value = attemptsValue(quiz, attempts.size),
            )
            quiz.timeLimitSeconds?.takeIf { it > 0 }?.let { limit ->
                IconRow(
                    icon = Icons.Outlined.Timer,
                    label = "TEMPO",
                    value = "${limit / 60} minuti",
                )
            }
            availabilityValue(quiz, now)?.let { (label, value) ->
                IconRow(icon = Icons.Outlined.Schedule, label = label, value = value)
            }
            quiz.intro?.takeIf { it.isNotBlank() }?.let { intro ->
                IconRow(icon = Icons.AutoMirrored.Outlined.Notes, label = "ISTRUZIONI") {
                    HtmlBody(html = intro, color = scheme.onSurface)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        QuizActionRow(
            canStart = canStart,
            primaryLabel = primaryLabel(resumable != null, notYetOpen, closed, attemptsLeft),
            primaryIsResume = resumable != null,
            hasHistory = attempts.isNotEmpty(),
            onPrimary = { if (resumable != null) onResumeAttempt(resumable.id.value) else onStartAttempt() },
            onShowHistory = onShowHistory,
        )
    }
}

// Level 1: the full attempt list as a segmented Material Expressive group. Tapping a finished
// attempt opens its review; an in-progress one resumes it.
@Composable
private fun QuizHistoryPage(
    attempts: List<QuizAttempt>,
    onResumeAttempt: (Int) -> Unit,
    onReviewAttempt: (Int) -> Unit,
) {
    // Newest first reads more naturally in a history list.
    val ordered = remember(attempts) { attempts.sortedByDescending { it.attemptNumber } }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(ordered, key = { _, attempt -> attempt.id.value }) { index, attempt ->
            AttemptRow(
                attempt = attempt,
                isFirst = index == 0,
                isLast = index == ordered.lastIndex,
                onClick = {
                    if (attempt.state == AttemptState.InProgress) onResumeAttempt(attempt.id.value)
                    else onReviewAttempt(attempt.id.value)
                },
            )
        }
    }
}

// Segmented row: 28dp corners cap the group's ends, 6dp where rows touch — same language as the
// maps building list.
@Composable
private fun AttemptRow(
    attempt: QuizAttempt,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val inProgress = attempt.state == AttemptState.InProgress
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                .clickable(onClick = onClick)
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
                    text = "Tentativo ${attempt.attemptNumber}",
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

// Connected button group: a wide brand leading button (start / resume) and a neutral tonal
// "Storico" trailing half when there are attempts to look back at. Collapses to a single
// full-width button when there's no history yet.
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
                enabled = canStart,
                modifier = Modifier.weight(1.4f),
            )
            FilledTonalButton(
                onClick = onShowHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Storico", fontWeight = FontWeight.SemiBold)
            }
        } else {
            BrandButton(
                label = primaryLabel,
                icon = if (primaryIsResume) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                bg = brandBg,
                fg = brandFg,
                shape = RoundedCornerShape(28.dp),
                onClick = onPrimary,
                enabled = canStart,
                modifier = Modifier.weight(1f),
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

private fun attemptsCountLabel(count: Int): String =
    if (count == 1) "1 tentativo" else "$count tentativi"

private fun attemptSubtitle(attempt: QuizAttempt): String {
    val state = when (attempt.state) {
        AttemptState.InProgress -> "In corso"
        AttemptState.Overdue -> "Scaduto"
        AttemptState.Finished -> "Concluso"
        AttemptState.Abandoned -> "Abbandonato"
        AttemptState.Unknown -> "Tentativo"
    }
    val instant = attempt.timeFinish ?: attempt.timeStart
    return if (instant != null) "$state · ${DateFmt.format(instant)}" else state
}

private fun primaryLabel(
    resumable: Boolean,
    notYetOpen: Boolean,
    closed: Boolean,
    attemptsLeft: Boolean,
): String = when {
    resumable -> "Riprendi"
    notYetOpen -> "Non ancora aperto"
    closed -> "Quiz chiuso"
    !attemptsLeft -> "Tentativi esauriti"
    else -> "Inizia tentativo"
}

private fun attemptsValue(quiz: Quiz, used: Int): String {
    val max = quiz.maxAttempts
    return when {
        max != null && max > 0 -> "$used di $max"
        used == 0 -> "Tentativi illimitati"
        else -> "$used effettuati · illimitati"
    }
}

// (label, value) for the availability IconRow, or null when the quiz is always open.
private fun availabilityValue(quiz: Quiz, now: Instant): Pair<String, String>? {
    quiz.timeOpen?.takeIf { it.isAfter(now) }?.let {
        return "APERTURA" to "Apre ${DateFmt.format(it)}"
    }
    quiz.timeClose?.let {
        return if (it.isAfter(now)) "CHIUSURA" to "Chiude ${DateFmt.format(it)}"
        else "CHIUSURA" to "Chiuso il ${DateFmt.format(it)}"
    }
    return null
}

private val DateFmt = DateTimeFormatter
    .ofPattern("EEE d MMM, HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
