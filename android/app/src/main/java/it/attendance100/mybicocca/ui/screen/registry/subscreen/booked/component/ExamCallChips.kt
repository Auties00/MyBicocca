package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.displayLabel

// Pills under the exam title: exam mode, the rare "prova parziale" flag, the desAppello
// qualifier when it actually adds something, and the activity's credits. Shared by the
// booked exam card and the detail sheet so the two read as the same object.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamCallChips(
    examType: ExamType,
    variantLabel: String?,
    partialLabel: String?,
    creditsLabel: String?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val examTypeLabel = examType.displayLabel()
    if (examTypeLabel == null && variantLabel == null && partialLabel == null && creditsLabel == null) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        examTypeLabel?.let {
            ExamTagChip(
                text = it,
                containerColor = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer,
            )
        }
        partialLabel?.let {
            ExamTagChip(
                text = it,
                containerColor = scheme.tertiaryContainer,
                contentColor = scheme.onTertiaryContainer,
            )
        }
        variantLabel?.let {
            ExamTagChip(
                text = it,
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurfaceVariant,
            )
        }
        creditsLabel?.let {
            ExamTagChip(
                text = it,
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExamTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMediumEmphasized,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
