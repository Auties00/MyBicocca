package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags

/** Lets the user pick the allergens to flag in the menus. The selection never leaves the device. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MensaAllergensPage(
    allergens: Loadable<List<MensaAllergen>>,
    status: SyncStatus,
    preferences: MensaAllergenPreferences,
    onToggleAllergen: (id: Int, selected: Boolean) -> Unit,
    onHideMatchingChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val list = allergens.valueOrNull().orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .testTag(MensaTestTags.ALLERGENS_PAGE),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                Text(
                    text = stringResource(R.string.mensa_allergens_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        when {
            list.isEmpty() && status is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_allergens_load_failed),
                body = stringResource(status.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetry) },
            )

            allergens is Loadable.NotYetLoaded -> Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_allergens_loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    list.forEach { allergen ->
                        val selected = allergen.id in preferences.allergenIds
                        FilterChip(
                            selected = selected,
                            onClick = { haptic.tap(); onToggleAllergen(allergen.id, !selected) },
                            label = { Text(allergen.shortName.ifBlank { allergen.name }) },
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.mensa_allergens_hide_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.mensa_allergens_hide_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = preferences.hideMatchingDishes,
                            onCheckedChange = { haptic.tap(); onHideMatchingChange(it) },
                            enabled = preferences.isActive,
                        )
                    }
                }

                if (preferences.isActive) {
                    TextButton(onClick = { haptic.tap(); onClear() }, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.mensa_allergens_clear))
                    }
                }

                Text(
                    text = stringResource(R.string.mensa_allergens_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Allergens", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun AllergensPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaAllergensPage(
            allergens = Loadable.Loaded(MensaPreviewData.allergens),
            status = SyncStatus.Idle,
            preferences = MensaAllergenPreferences(allergenIds = setOf(1, 7)),
            onToggleAllergen = { _, _ -> },
            onHideMatchingChange = {},
            onClear = {},
            onRetry = {},
        )
    }
}
