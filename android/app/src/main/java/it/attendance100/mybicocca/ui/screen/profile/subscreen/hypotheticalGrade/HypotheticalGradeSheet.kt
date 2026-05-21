package it.attendance100.mybicocca.ui.screen.profile.subscreen.hypotheticalGrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HypotheticalGradeSheet(
    rollup: GradeRollup?,
    currentArithmetic: Float?,
    currentWeighted: Float?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Calcola Media Ipotetica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            var gradeText by remember { mutableStateOf("") }
            var cfuText by remember { mutableStateOf("") }

            val grade = gradeText.toIntOrNull()
            val cfu = cfuText.toFloatOrNull()
            val gradeError = grade != null && (grade < 18 || grade > 31)
            val cfuError = cfu != null && cfu <= 0f

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = { if (it.length <= 2) gradeText = it.filter(Char::isDigit) },
                    label = { Text("Voto") },
                    isError = gradeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = cfuText,
                    onValueChange = { cfuText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("CFU") },
                    isError = cfuError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }

            // O(1) recompute: read pre-aggregated sums, add the hypothetical, divide once.
            // No list traversal, no flow collection, no recomposition outside this scope.
            val newArithmetic: Float? = if (rollup != null && grade != null && !gradeError) {
                (rollup.gradeSum + grade).toFloat() / (rollup.gradedExamCount + 1).toFloat()
            } else null
            val newWeighted: Float? = if (rollup != null && grade != null && cfu != null && !gradeError && !cfuError) {
                ((rollup.weightedGradeSum + grade.toDouble() * cfu).toFloat()) /
                    (rollup.gradedCreditsSum + cfu)
            } else null

            HypotheticalRow(
                label = "Media Aritmetica",
                current = currentArithmetic,
                projected = newArithmetic,
            )
            HypotheticalRow(
                label = "Media Ponderata",
                current = currentWeighted,
                projected = newWeighted,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HypotheticalRow(label: String, current: Float?, projected: Float?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = current?.let { "%.2f".format(it) } ?: "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            if (projected != null && current != null) {
                Text(text = "→", fontSize = 22.sp)
                Text(
                    text = "%.2f".format(projected),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                DifferenceIndicator(delta = projected - current)
            }
        }
    }
}

@Composable
private fun DifferenceIndicator(delta: Float) {
    val (bg, fg, sign) = when {
        delta > 0.005f -> Triple(Color(0xFF1FA84B).copy(alpha = 0.2f), Color(0xFF1FA84B), "+")
        delta < -0.005f -> Triple(Color(0xFFCC1F2F).copy(alpha = 0.2f), Color(0xFFCC1F2F), "")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "$sign${"%.2f".format(delta)}",
            color = fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
