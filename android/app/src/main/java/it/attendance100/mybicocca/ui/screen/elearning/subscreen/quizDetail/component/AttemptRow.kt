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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// One row of the attempt history. The number tile carries the row's identity —
// state labels repeat ("Concluso" xN), the ordinal is what distinguishes attempts.
@Composable
fun AttemptRow(
    attempt: QuizAttempt,
    maxSumGrades: Double?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val inProgress = attempt.state == AttemptState.InProgress
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (inProgress) scheme.secondaryContainer else scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = attempt.attemptNumber.toString(),
                        color = if (inProgress) scheme.onSecondaryContainer else scheme.onSurface,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                    )
                    Text(
                        text = "TENT.",
                        color = (if (inProgress) scheme.onSecondaryContainer else scheme.onSurface).copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        letterSpacing = 1.2.sp,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stateLabel(attempt.state),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = attemptCaption(attempt),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when {
                inProgress -> Pill(text = "RIPRENDI", bg = scheme.secondary, fg = scheme.onSecondary)
                attempt.state == AttemptState.Finished && attempt.sumGrades != null -> Box(
                    modifier = Modifier
                        .clip(OrganicShapes.Cookie)
                        .background(scheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = gradeLabel(attempt.sumGrades, maxSumGrades),
                        color = scheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
        )
    }
}

private fun stateLabel(state: AttemptState): String = when (state) {
    AttemptState.InProgress -> "In corso"
    AttemptState.Overdue -> "Tempo scaduto"
    AttemptState.Finished -> "Concluso"
    AttemptState.Abandoned -> "Abbandonato"
    AttemptState.Unknown -> "Tentativo"
}

private fun attemptCaption(attempt: QuizAttempt): String {
    val parts = mutableListOf<String>()
    val finish = attempt.timeFinish
    val start = attempt.timeStart
    when {
        finish != null -> parts += "${DateFmt.format(finish)} alle ${TimeFmt.format(finish)}"
        start != null -> parts += "Iniziato ${DateFmt.format(start)} alle ${TimeFmt.format(start)}"
    }
    if (start != null && finish != null) {
        val minutes = Duration.between(start, finish).toMinutes()
        parts += if (minutes < 1) "meno di un minuto" else "$minutes min"
    }
    return parts.joinToString(" · ").ifEmpty { "—" }
}

private fun gradeLabel(sum: Double, maxSum: Double?): String =
    if (maxSum != null && maxSum > 0) "${formatGradeValue(sum)}/${formatGradeValue(maxSum)}"
    else formatGradeValue(sum)

private val DateFmt = DateTimeFormatter
    .ofPattern("d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
