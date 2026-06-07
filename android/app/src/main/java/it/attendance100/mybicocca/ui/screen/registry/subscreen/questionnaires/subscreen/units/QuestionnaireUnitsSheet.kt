package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.units

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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.displayName

// Bottom sheet listing the compilable questionnaires of one activity, one row per
// teaching unit / lecturer / turno. The unit choice is what targets the compilation.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionnaireUnitsSheet(
    activity: QuestionnaireActivity,
    detail: Loadable<ActivityQuestionnaires>,
    detailStatus: SyncStatus,
    onCompileUnit: (ActivityQuestionnaires, QuestionnaireUnit) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        ) {
            Text(
                text = activity.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            val questionnaireName = (detail as? Loadable.Loaded)?.value?.questionnaireName
            if (questionnaireName != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = questionnaireName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(20.dp))

            when (detail) {
                Loadable.NotYetLoaded -> when (val status = detailStatus) {
                    is SyncStatus.Failed -> SheetError(onRetry = onRetry)

                    else -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(56.dp))
                    }
                }

                is Loadable.Loaded -> SheetUnits(
                    detail = detail.value,
                    onCompileUnit = onCompileUnit,
                )
            }
        }
    }
}

@Composable
private fun SheetUnits(
    detail: ActivityQuestionnaires,
    onCompileUnit: (ActivityQuestionnaires, QuestionnaireUnit) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val compilable = detail.questionnaireId != null && detail.questionnaireConfigId != null

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        detail.units.forEachIndexed { index, unit ->
            val first = index == 0
            val last = index == detail.units.lastIndex
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (first) 20.dp else 6.dp,
                    topEnd = if (first) 20.dp else 6.dp,
                    bottomStart = if (last) 20.dp else 6.dp,
                    bottomEnd = if (last) 20.dp else 6.dp,
                ),
                color = scheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (unit.completed) scheme.surfaceContainerHighest
                                else scheme.secondaryContainer,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = if (unit.completed) scheme.onSurfaceVariant
                            else scheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = unit.lecturerName ?: unit.teachingUnitName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        unit.partitionName?.let { partition ->
                            Text(
                                text = partition,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    when {
                        unit.completed -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Compilato",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.primary,
                            )
                        }

                        compilable -> Button(onClick = { onCompileUnit(detail, unit) }) {
                            Text("Compila")
                        }

                        else -> Text(
                            text = "Non disponibile",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (detail.anonymous) {
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Le risposte sono anonime: nessun legame viene salvato tra te e il questionario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SheetError(onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Impossibile caricare i questionari di questo insegnamento.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) { Text("Riprova") }
    }
}
