package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplication
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationResult
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationStage
import java.time.format.DateTimeFormatter

private val DayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

// The hub hero: brand-red card stating where the student is in the graduation timeline.
// Shows the final result once it exists, otherwise the application / window state.
@Composable
fun GraduationStatusHeader(
    stage: GraduationStage,
    application: GraduationApplication?,
    result: GraduationResult?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (result != null) Icons.Default.WorkspacePremium else Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Conseguimento titolo",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = headline(stage, result),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            val detail = detailLine(stage, application, result)
            if (detail != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }

            if (result != null) {
                Spacer(Modifier.height(14.dp))
                GradeBadge(result)
            }
        }
    }
}

@Composable
private fun GradeBadge(result: GraduationResult) {
    val grade = result.finalGrade
    val text = when {
        grade != null && result.cumLaude -> "$grade e lode"
        grade != null -> grade.toString()
        else -> "—"
    }
    Surface(
        color = Color.White,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            result.judgmentDescription?.let {
                Spacer(Modifier.width(10.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun headline(stage: GraduationStage, result: GraduationResult?): String = when {
    result != null -> "Titolo conseguito"
    stage == GraduationStage.NotOpen -> "Non ancora aperta"
    stage == GraduationStage.Open -> "Puoi presentare domanda"
    stage == GraduationStage.InProgress -> "Domanda in corso"
    stage == GraduationStage.Confirmed -> "Domanda confermata"
    stage == GraduationStage.Cancelled -> "Domanda annullata"
    stage == GraduationStage.Completed -> "Titolo conseguito"
    else -> "Conseguimento titolo"
}

private fun detailLine(
    stage: GraduationStage,
    application: GraduationApplication?,
    result: GraduationResult?,
): String? = when {
    result?.date != null -> "Discussa il ${result.date.format(DayFormat)}"
    stage == GraduationStage.NotOpen ->
        "Non sei ancora nella finestra di conseguimento titolo. Quando un appello di laurea sarà aperto potrai presentare la domanda da qui."
    stage == GraduationStage.Open ->
        "Seleziona un appello di laurea e segui i passaggi: domanda, tesi, relatori, allegati e consultazione."
    application?.sessionStartDate != null ->
        "Sessione dal ${application.sessionStartDate.format(DayFormat)}" +
            (application.sessionEndDate?.let { " al ${it.format(DayFormat)}" } ?: "")
    application?.callDescription != null -> application.callDescription
    else -> null
}
