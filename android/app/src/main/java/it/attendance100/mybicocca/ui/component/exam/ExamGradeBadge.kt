package it.attendance100.mybicocca.ui.component.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import it.attendance100.mybicocca.domain.model.exam.ExamGrade

// The grade as an expressive polygon: a sun for the lode, a soft burst for the full 30, a
// cookie for any other passing grade, a clover for the idoneità, a plain circle for the
// sober ones. Shared by Esiti and Prenotazioni > Passate so a grade reads the same wherever
// it appears; color stays on the app's container roles.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExamGradeBadge(
    grade: ExamGrade,
    size: Dp,
    style: TextStyle,
    showDenominator: Boolean = false,
) {
    val (container, content) = grade.containerColors()
    Box(
        modifier = Modifier
            .size(size)
            .clip(grade.badgeShape())
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        val denominator = grade.outOfThirtySymbol().takeIf { showDenominator }
        if (denominator != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = grade.shortLabel(), style = style, color = content)
                Text(
                    text = denominator,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = content.copy(alpha = 0.7f),
                )
            }
        } else {
            Text(
                text = grade.shortLabel(),
                style = style,
                color = content,
            )
        }
    }
}

// "/30" only for the numeric grades actually expressed out of thirty; the lode (30L) and
// the qualitative outcomes carry no denominator.
private fun ExamGrade.outOfThirtySymbol(): String? = when (this) {
    is ExamGrade.Numeric -> if (value in 18..30) "/30" else null
    else -> null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExamGrade.badgeShape(): Shape = when (this) {
    is ExamGrade.Numeric -> when {
        value >= 31 -> MaterialShapes.Sunny
        value == 30 -> MaterialShapes.SoftBurst
        else -> MaterialShapes.Cookie12Sided
    }
    ExamGrade.Passed -> MaterialShapes.Clover4Leaf
    ExamGrade.NotPassed,
    ExamGrade.Withdrew,
    ExamGrade.Absent,
    ExamGrade.Unknown -> MaterialShapes.Circle
}.toShape()

@Composable
private fun ExamGrade.containerColors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        is ExamGrade.Numeric ->
            if (value >= 31) scheme.tertiaryContainer to scheme.onTertiaryContainer
            else scheme.primaryContainer to scheme.onPrimaryContainer

        ExamGrade.Passed -> scheme.tertiaryContainer to scheme.onTertiaryContainer

        ExamGrade.NotPassed,
        ExamGrade.Withdrew,
        ExamGrade.Absent -> scheme.errorContainer to scheme.onErrorContainer

        ExamGrade.Unknown -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
}

fun ExamGrade.shortLabel(): String = when (this) {
    is ExamGrade.Numeric -> if (value >= 31) "30L" else value.toString()
    ExamGrade.Passed -> "Id."
    ExamGrade.NotPassed -> "Resp."
    ExamGrade.Withdrew -> "Rit."
    ExamGrade.Absent -> "Ass."
    ExamGrade.Unknown -> "—"
}
