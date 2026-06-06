package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component.formatGradeValue
import it.attendance100.mybicocca.ui.screen.elearning.theme.CourseDetailTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The quiz overview as a modal, built to the app's detail-sheet language (see
// BookedExamDetailSheet / EventDetailSheet): an icon-badged header with a status chip, a
// column of IconRows for the facts, and a connected action group — all in one scrolling
// column. The action navigates to the full-screen attempt/review; this sheet never hosts the
// quiz-taking itself.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailSheet(
    quizId: Int,
    courseId: Int,
    onDismiss: () -> Unit,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (attemptId: Int) -> Unit,
    onReviewAttempt: (attemptId: Int) -> Unit,
    viewModel: QuizDetailViewModel = hiltViewModel<QuizDetailViewModel, QuizDetailViewModel.Factory>(
        key = "quiz-sheet-$quizId",
        creationCallback = { it.create(AppRoute.QuizDetail(quizId = quizId, courseId = courseId)) },
    ),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quizLoadable by viewModel.quiz.collectAsStateWithLifecycle()
    val attemptsLoadable by viewModel.attempts.collectAsStateWithLifecycle()
    val bestGradeLoadable by viewModel.bestGrade.collectAsStateWithLifecycle()

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
                when (val loadable = quizLoadable) {
                    Loadable.NotYetLoaded -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    is Loadable.Loaded -> QuizSheetContent(
                        quiz = loadable.value,
                        attempts = attemptsLoadable.valueOrNull().orEmpty().filterNot { it.previewMode },
                        bestGrade = bestGradeLoadable.valueOrNull(),
                        onStartAttempt = onStartAttempt,
                        onResumeAttempt = onResumeAttempt,
                        onReviewAttempt = onReviewAttempt,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizSheetContent(
    quiz: Quiz,
    attempts: List<QuizAttempt>,
    bestGrade: BestGrade?,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (Int) -> Unit,
    onReviewAttempt: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val now = remember(quiz, attempts) { Instant.now() }

    val resumable = attempts.firstOrNull { it.state == AttemptState.InProgress }
    val lastFinished = attempts.lastOrNull {
        it.state == AttemptState.Finished || it.state == AttemptState.Overdue
    }
    val closed = quiz.timeClose != null && now.isAfter(quiz.timeClose)
    val notYetOpen = quiz.timeOpen != null && now.isBefore(quiz.timeOpen)
    val attemptsLeft = quiz.maxAttempts.let { max -> max == null || max <= 0 || attempts.size < max }
    val canStart = resumable != null || (!closed && !notYetOpen && attemptsLeft)

    // Header: icon badge + title + a single status chip, mirroring the booking/event sheets.
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
                    imageVector = Icons.Outlined.Quiz,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = quiz.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            StatusChip(
                resumable = resumable != null,
                bestGrade = bestGrade,
                closed = closed,
                notYetOpen = notYetOpen,
            )
        }
    }

    Spacer(Modifier.height(24.dp))

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
        hasReview = lastFinished != null,
        onPrimary = { if (resumable != null) onResumeAttempt(resumable.id.value) else onStartAttempt() },
        onReview = { lastFinished?.let { onReviewAttempt(it.id.value) } },
    )
}

@Composable
private fun StatusChip(
    resumable: Boolean,
    bestGrade: BestGrade?,
    closed: Boolean,
    notYetOpen: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val (label, fg, bg) = when {
        resumable -> Triple("Tentativo in corso", scheme.onSecondaryContainer, scheme.secondaryContainer)
        bestGrade?.grade != null -> Triple("Concluso", scheme.onPrimaryContainer, scheme.primaryContainer)
        closed -> Triple("Chiuso", scheme.onErrorContainer, scheme.errorContainer)
        notYetOpen -> Triple("Non ancora aperto", scheme.onTertiaryContainer, scheme.tertiaryContainer)
        else -> Triple("Da svolgere", scheme.onSurface, scheme.surfaceContainerHigh)
    }
    AssistChip(
        onClick = {},
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg),
        border = null,
    )
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

// Connected button group, same arrangement as the booking/event sheets: a wide brand leading
// button (start / resume / review) and a neutral tonal "Rivedi" trailing half when there's a
// finished attempt to look back at. Collapses to a single full-width button when one applies.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuizActionRow(
    canStart: Boolean,
    primaryLabel: String,
    primaryIsResume: Boolean,
    hasReview: Boolean,
    onPrimary: () -> Unit,
    onReview: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when {
            // Can start/resume, with a finished attempt to review: the canonical pair.
            canStart && hasReview -> {
                BrandButton(
                    label = primaryLabel,
                    icon = if (primaryIsResume) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                    bg = brandBg,
                    fg = brandFg,
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    onClick = onPrimary,
                    modifier = Modifier.weight(1.4f),
                )
                FilledTonalButton(
                    onClick = onReview,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Rivedi", fontWeight = FontWeight.SemiBold)
                }
            }
            // Only one action available -> a single full-width button.
            canStart -> BrandButton(
                label = primaryLabel,
                icon = if (primaryIsResume) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                bg = brandBg,
                fg = brandFg,
                shape = RoundedCornerShape(28.dp),
                onClick = onPrimary,
                modifier = Modifier.weight(1f),
            )
            hasReview -> BrandButton(
                label = "Rivedi tentativo",
                icon = Icons.Outlined.Visibility,
                bg = brandBg,
                fg = brandFg,
                shape = RoundedCornerShape(28.dp),
                onClick = onReview,
                modifier = Modifier.weight(1f),
            )
            // Nothing to do (closed / not open / exhausted with no attempts): disabled hint.
            else -> BrandButton(
                label = primaryLabel,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                bg = brandBg,
                fg = brandFg,
                shape = RoundedCornerShape(28.dp),
                onClick = {},
                enabled = false,
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
