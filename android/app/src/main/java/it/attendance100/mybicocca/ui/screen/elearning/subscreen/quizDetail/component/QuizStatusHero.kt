package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The page's anchor card: answers "a che punto sono con questo quiz?" at a glance.
// State priority reflects real Bicocca data: a paused attempt (very common — students
// abandon self-assessment quizzes mid-way) beats everything, then the earned grade,
// then availability windows (rare).
@Composable
fun QuizStatusHero(
    quiz: Quiz,
    attempts: List<QuizAttempt>,
    bestGrade: BestGrade?,
    now: Instant,
    onStartAttempt: () -> Unit,
    onResumeAttempt: (AttemptId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val inProgress = attempts.firstOrNull { it.state == AttemptState.InProgress && !it.previewMode }
    val finishedCount = attempts.count { it.state == AttemptState.Finished && !it.previewMode }
    val closed = quiz.timeClose != null && now.isAfter(quiz.timeClose)
    val notYetOpen = quiz.timeOpen != null && now.isBefore(quiz.timeOpen)
    val attemptsLeft = quiz.maxAttempts.let { max ->
        max == null || max <= 0 || attempts.count { !it.previewMode } < max
    }
    val canAttempt = !closed && !notYetOpen && attemptsLeft

    when {
        inProgress != null -> HeroCard(
            container = scheme.secondaryContainer,
            accent = scheme.secondary,
            onContainer = scheme.onSecondaryContainer,
            decorShape = OrganicShapes.SmoothCookie6,
            eyebrow = "✦ TENTATIVO IN CORSO",
            headline = "Riprendi da dove eri",
            caption = inProgress.timeStart
                ?.let { "Iniziato ${FullDateFmt.format(it)} alle ${TimeFmt.format(it)}" }
                ?: "Hai un tentativo aperto",
            cta = "Riprendi il tentativo",
            onCta = { onResumeAttempt(inProgress.id) },
            modifier = modifier,
        )

        bestGrade?.grade != null -> GradeHero(
            grade = bestGrade.grade,
            maxGrade = bestGrade.maxGrade,
            finishedCount = finishedCount,
            cta = if (canAttempt) "Nuovo tentativo" else null,
            onCta = onStartAttempt,
            modifier = modifier,
        )

        closed -> HeroCard(
            container = scheme.errorContainer,
            accent = scheme.error,
            onContainer = scheme.onErrorContainer,
            decorShape = OrganicShapes.Burst,
            eyebrow = "✸ CHIUSO",
            headline = if (finishedCount > 0) "Quiz concluso" else "Non svolto",
            caption = "Chiuso ${FullDateFmt.format(quiz.timeClose)} alle ${TimeFmt.format(quiz.timeClose)}",
            cta = null,
            onCta = {},
            modifier = modifier,
        )

        notYetOpen -> HeroCard(
            container = scheme.tertiaryContainer,
            accent = scheme.tertiary,
            onContainer = scheme.onTertiaryContainer,
            decorShape = OrganicShapes.Puffy,
            eyebrow = "✦ IN ARRIVO",
            headline = "Non ancora aperto",
            caption = "Apre ${FullDateFmt.format(quiz.timeOpen)} alle ${TimeFmt.format(quiz.timeOpen)}",
            cta = null,
            onCta = {},
            modifier = modifier,
        )

        else -> HeroCard(
            container = scheme.tertiaryContainer,
            accent = scheme.tertiary,
            onContainer = scheme.onTertiaryContainer,
            decorShape = OrganicShapes.Puffy,
            eyebrow = "✦ DA SVOLGERE",
            headline = "Pronto quando vuoi",
            caption = openCaption(quiz, now),
            cta = if (canAttempt) "Inizia il tentativo" else null,
            onCta = onStartAttempt,
            modifier = modifier,
        )
    }
}

@Composable
private fun GradeHero(
    grade: Double,
    maxGrade: Double?,
    finishedCount: Int,
    cta: String?,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(scheme.primary, OrganicShapes.Sunny),
            )
            Column {
                Text(
                    text = "❀ VOTO MIGLIORE",
                    color = scheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = formatGradeValue(grade),
                        color = scheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        fontSize = 52.sp,
                        lineHeight = 48.sp,
                        letterSpacing = (-2.5).sp,
                    )
                    if (maxGrade != null && maxGrade > 0) {
                        Text(
                            text = "/ ${formatGradeValue(maxGrade)}",
                            color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when (finishedCount) {
                        0 -> "Nessun tentativo concluso"
                        1 -> "Su un tentativo concluso"
                        else -> "Su $finishedCount tentativi conclusi"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onPrimaryContainer,
                    fontStyle = FontStyle.Italic,
                )
                if (cta != null) {
                    Spacer(Modifier.height(14.dp))
                    HeroCta(label = cta, container = scheme.primaryContainer, onContainer = scheme.onPrimaryContainer, onClick = onCta)
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    container: Color,
    accent: Color,
    onContainer: Color,
    decorShape: Shape,
    eyebrow: String,
    headline: String,
    caption: String,
    cta: String?,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = container,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(accent, decorShape),
            )
            Column {
                Text(
                    text = eyebrow,
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    color = onContainer,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer,
                    fontStyle = FontStyle.Italic,
                )
                if (cta != null) {
                    Spacer(Modifier.height(14.dp))
                    HeroCta(label = cta, container = container, onContainer = onContainer, onClick = onCta)
                }
            }
        }
    }
}

@Composable
private fun HeroCta(
    label: String,
    container: Color,
    onContainer: Color,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = onContainer,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = container,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
        )
    }
}

private fun openCaption(quiz: Quiz, now: Instant): String {
    val parts = mutableListOf<String>()
    quiz.timeClose?.takeIf { it.isAfter(now) }?.let {
        parts += "Chiude ${FullDateFmt.format(it)} alle ${TimeFmt.format(it)}"
    }
    quiz.timeLimitSeconds?.takeIf { it > 0 }?.let { parts += "limite di ${it / 60} minuti" }
    val max = quiz.maxAttempts
    parts += if (max == null || max <= 0) "tentativi illimitati" else "massimo $max tentativi"
    return parts.joinToString(" · ").replaceFirstChar { it.uppercaseChar() }
}

fun formatGradeValue(grade: Double): String = when {
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(Locale.ITALIAN, "%.1f", grade)
}

private val FullDateFmt = DateTimeFormatter
    .ofPattern("EEE d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
