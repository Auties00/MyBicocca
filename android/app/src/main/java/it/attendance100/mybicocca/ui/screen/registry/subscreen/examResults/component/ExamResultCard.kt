package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)
private val DeadlineFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

@Composable
fun ExamResultCard(
    result: ExamResult,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val examDate = remember(result.examDateTime) {
        result.examDateTime?.toLocalDate()?.format(DateFormat)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradeBadge(grade = result.grade)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                examDate?.let {
                    Text(
                        text = "Sostenuto $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                AcknowledgmentLine(result = result)
            }
        }
    }
}

@Composable
private fun GradeBadge(grade: ExamGrade) {
    val (bg, fg, text, sub) = gradeAppearance(grade)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = fg,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (sub != null) {
                Text(
                    text = sub,
                    color = fg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = (-2).dp),
                )
            }
        }
    }
}

@Composable
private fun AcknowledgmentLine(result: ExamResult) {
    val scheme = MaterialTheme.colorScheme
    val (label, color) = acknowledgmentLabel(result.acknowledgment) ?: return
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium,
        )
        result.acknowledgmentDeadline?.let { deadline ->
            Text(
                text = "· entro ${deadline.format(DeadlineFormat)}",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

private data class GradeAppearance(
    val background: Color,
    val foreground: Color,
    val text: String,
    val sub: String?,
)

@Composable
private fun gradeAppearance(grade: ExamGrade): GradeAppearance {
    val scheme = MaterialTheme.colorScheme
    return when (grade) {
        is ExamGrade.Numeric -> when {
            grade.value >= 31 -> GradeAppearance(scheme.primary, scheme.onPrimary, "30", "L")
            else -> GradeAppearance(scheme.primary, scheme.onPrimary, grade.value.toString(), null)
        }
        ExamGrade.Passed -> GradeAppearance(scheme.primary, scheme.onPrimary, "P", null)
        ExamGrade.NotPassed -> GradeAppearance(scheme.error, scheme.onError, "NP", null)
        ExamGrade.Withdrew -> GradeAppearance(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, "Rit", null)
        ExamGrade.Absent -> GradeAppearance(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, "Ass", null)
        ExamGrade.Unknown -> GradeAppearance(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, "—", null)
    }
}

@Composable
private fun acknowledgmentLabel(status: AcknowledgmentStatus): Pair<String, Color>? {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        AcknowledgmentStatus.NotViewed -> "Da visualizzare" to scheme.primary
        AcknowledgmentStatus.Viewed -> "Da accettare o rifiutare" to scheme.tertiary
        AcknowledgmentStatus.Accepted -> "Voto accettato" to scheme.primary
        AcknowledgmentStatus.Rejected -> "Voto rifiutato" to scheme.error
        AcknowledgmentStatus.Unknown -> null
    }
}
