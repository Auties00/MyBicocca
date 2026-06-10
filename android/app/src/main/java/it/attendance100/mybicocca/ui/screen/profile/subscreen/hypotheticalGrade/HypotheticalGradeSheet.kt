package it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import java.util.Locale

private const val MIN_PASSING_GRADE = 18
private const val MAX_GRADE = 31

/**
 * Hypothetical-average calculator sheet: a stat card showing the current arithmetic or
 * weighted average with an animated arrow to the projected value, a signed delta chip
 * beneath it, and the grade input — plus an optional CFU input in weighted mode. The
 * projection is an O(1) recompute over the pre-aggregated [GradeRollup]: one hypothetical
 * entry added to the stored sums, one division. Out-of-range input is flagged inline and
 * produces no projection.
 */
@Composable
fun HypotheticalGradeSheet(
    rollup: GradeRollup?,
    currentArithmetic: Float?,
    currentWeighted: Float?,
    isWeighted: Boolean,
    onDismiss: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val grayColor = MaterialTheme.colorScheme.onSurfaceVariant

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        var gradeText by remember { mutableStateOf("") }
        var cfuText by remember { mutableStateOf("") }

        val grade = gradeText.toIntOrNull()
        val cfu = cfuText.toIntOrNull()
        val gradeValid = grade != null && grade in MIN_PASSING_GRADE..MAX_GRADE

        val current = if (isWeighted) currentWeighted else currentArithmetic
        val projected: Float? = when {
            rollup == null || grade == null || !gradeValid -> null
            !isWeighted -> (rollup.gradeSum + grade).toFloat() / (rollup.gradedExamCount + 1).toFloat()
            cfu != null && cfu > 0 ->
                ((rollup.weightedGradeSum + grade.toDouble() * cfu).toFloat()) / (rollup.gradedCreditsSum + cfu)
            else -> null
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Calculate, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calcola Media Ipotetica",
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HypotheticalStatCard(
                    title = if (isWeighted) "Media Ponderata" else "Media Aritmetica",
                    currentValue = current,
                    newValue = projected,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor,
                )
                DifferenceIndicator(difference = if (projected != null && current != null) projected - current else null)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = { if (it.length <= 2) gradeText = it.filter(Char::isDigit) },
                    label = { Text("Voto") },
                    placeholder = { Text(">17", color = grayColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = grade != null && grade < MIN_PASSING_GRADE,
                    supportingText = when {
                        grade != null && grade < MIN_PASSING_GRADE -> { { Text("Voto non valido (>17)") } }
                        grade != null && grade > MAX_GRADE -> { { Text("Voto troppo alto!") } }
                        else -> null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )

                if (isWeighted) {
                    OutlinedTextField(
                        value = cfuText,
                        onValueChange = { if (it.length <= 2) cfuText = it.filter(Char::isDigit) },
                        label = { Text("CFU") },
                        placeholder = { Text("Opzionale", color = grayColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = cfu != null && cfu <= 0,
                        supportingText = if (cfu != null && cfu <= 0) { { Text("CFU non validi") } } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Current average with a "→ projected" suffix that expands in once a projection exists. */
@Composable
private fun HypotheticalStatCard(
    modifier: Modifier = Modifier,
    title: String,
    currentValue: Float?,
    newValue: Float?,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, color = grayColor, fontSize = 11.sp, maxLines = 1)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = currentValue?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "—",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                AnimatedVisibility(
                    visible = newValue != null,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = "→", color = grayColor, fontSize = 14.sp)
                        Text(
                            text = newValue?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "",
                            color = primaryColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Signed delta chip — green for gains, red for losses — in a fixed-height slot so the
 * layout does not shift as the chip appears and disappears.
 */
@Composable
private fun DifferenceIndicator(
    modifier: Modifier = Modifier,
    difference: Float?,
) {
    val isPositive = difference != null && difference >= 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = difference != null, enter = fadeIn(), exit = fadeOut()) {
            val chipColor = if (isPositive) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFF44336).copy(alpha = 0.15f)
            val chipTextColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(chipColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.offset(y = (-2.85).dp),
                    text = difference?.let {
                        String.format(Locale.getDefault(), "%s%.2f", if (it >= 0) "+" else "", it)
                    } ?: "",
                    color = chipTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
