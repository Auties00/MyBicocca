package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.theme.LocalIsOnline

/**
 * In-sheet page listing the compilable questionnaires of one activity, one row per
 * teaching unit / lecturer / turno; the sheet's morphing header carries the activity
 * name. The unit choice is what targets the compilation. Shows an error message with
 * retry when the detail fetch failed and a loading indicator until it lands.
 */
@Composable
fun QuestionnaireUnitsPage(
    detail: Loadable<ActivityQuestionnaires>,
    detailStatus: SyncStatus,
    onCompileUnit: (ActivityQuestionnaires, QuestionnaireUnit) -> Unit,
    onRetry: () -> Unit,
) {
    when (detail) {
        Loadable.NotYetLoaded -> when (detailStatus) {
            is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.common_error_title),
                body = stringResource(R.string.questionnaire_units_load_error),
                action = { RetryButton(onClick = onRetry) },
            )

            else -> SheetLoadingIndicator(label = stringResource(R.string.questionnaire_loading))
        }

        is Loadable.Loaded -> UnitsList(
            detail = detail.value,
            onCompileUnit = onCompileUnit,
        )
    }
}

/**
 * Connected segment rows, one per unit: a person chip, the lecturer (or unit) name with
 * the partition beneath, and the trailing action — a brand-filled "Compila" (red in
 * light, primaryContainer in dark, the shared CTA scheme, enabled only online) while
 * compilable, its outlined non-interactive same-red "Compilato" twin once done, or a
 * plain "Non disponibile" note when the questionnaire ids are missing. The filled button
 * stacks a transparent ghost of the longer "Compilato" label under "Compila" so both
 * buttons share one width and stay aligned across rows.
 */
@Composable
private fun UnitsList(
    detail: ActivityQuestionnaires,
    onCompileUnit: (ActivityQuestionnaires, QuestionnaireUnit) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val compilable = detail.questionnaireId != null && detail.questionnaireConfigId != null
    val isOnline = LocalIsOnline.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
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
                        unit.completed -> {
                            OutlinedButton(
                                onClick = {},
                                enabled = false,
                                border = BorderStroke(1.dp, scheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    disabledContentColor = scheme.primary,
                                ),
                            ) {
                                Text(stringResource(R.string.questionnaire_unit_compiled))
                            }
                        }

                        compilable -> {
                            val haptic = rememberHapticManager()
                            Button(
                                onClick = { haptic.tap(); onCompileUnit(detail, unit) },
                                enabled = isOnline,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = brandBg,
                                    contentColor = brandFg,
                                ),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        stringResource(R.string.questionnaire_unit_compiled),
                                        color = Color.Transparent
                                    )
                                    Text(stringResource(R.string.questionnaire_unit_compile))
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = stringResource(R.string.questionnaire_unit_unavailable),
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

    }
}
