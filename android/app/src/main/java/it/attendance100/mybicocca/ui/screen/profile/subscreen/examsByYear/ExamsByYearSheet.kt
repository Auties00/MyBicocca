package it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

enum class ExamValueMode { Grade, Credits }

private val PassedGreen = Color(0xFF1FA84B)

@Composable
fun ExamsByYearSheet(
    rows: List<TranscriptRow>,
    initialMode: ExamValueMode,
    onDismiss: () -> Unit,
) {
    var mode by remember { mutableStateOf(initialMode) }

    // Grouped by year of the study plan (anno di corso), passed exams first within each year.
    val byYear = rows
        .sortedWith(
            compareBy(
                { it.courseYear },
                { it.state != TranscriptRowState.Passed },
                { it.activityName })
        )
        .groupBy { it.courseYear }
        .toSortedMap()

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = if (mode == ExamValueMode.Grade) "Esami Sostenuti" else "CFU Acquisiti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    byYear.forEach { (year, exams) ->
                        item(key = "year-$year") {
                            Text(
                                text = if (year <= 0) "Prerequisiti" else "Anno $year",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(exams, key = { it.id }) { exam ->
                            ExamRow(exam = exam, mode = mode)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(100)
                    )
            ) {
                SegmentedSwitch(
                    options = listOf(ExamValueMode.Grade, ExamValueMode.Credits),
                    selected = mode,
                    onSelected = { mode = it },
                    label = { if (it == ExamValueMode.Grade) "Voti" else "Crediti" },
                    borderColor = Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun ExamRow(exam: TranscriptRow, mode: ExamValueMode) {
    val passed = exam.state == TranscriptRowState.Passed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Outlined.Schedule,
            contentDescription = if (passed) "Superato" else "In sospeso",
            tint = if (passed) PassedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = exam.activityName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valueLabel(exam, mode),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (passed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun valueLabel(exam: TranscriptRow, mode: ExamValueMode): String = when (mode) {
    ExamValueMode.Grade -> when {
        exam.grade == null -> "—"
        exam.cumLaude -> "30L"
        else -> exam.grade.toString()
    }

    ExamValueMode.Credits -> "${formatCredits(exam.credits)} CFU"
}

private fun formatCredits(value: Float): String {
    val asInt = value.toInt()
    return if (value == asInt.toFloat()) asInt.toString() else "%.1f".format(value)
}
