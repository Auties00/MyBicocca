package it.attendance100.mybicocca.ui.screen.settings.component.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Suppress("AssignedValueIsNeverRead")
@Composable
fun <T> OpenDialogTile(
    title: String,
    value: T,
    entries: Map<T, String>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    var isDialogShown by remember { mutableStateOf(false) }

    StandardSettingTile(
        modifier = modifier,
        title = title,
        iconAsset = icon,
        trailingAction = {
            Text(
                text = entries[value].orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        onItemClick = {
            isDialogShown = true
        },
    )

    if (isDialogShown) {
        AlertDialog(
            title = {
                Text(text = title)
            },
            onDismissRequest = {
                isDialogShown = false
            },
            text = {
                Box {
                    val state = rememberLazyListState()

                    LazyColumn(state = state) {
                        entries.forEach { entry ->
                            item {
                                DialogRow(
                                    label = entry.value,
                                    isSelected = value == entry.key,
                                    onSelected = {
                                        isDialogShown = false
                                        onValueChange(entry.key)
                                    },
                                )
                            }
                        }
                    }

                    if (state.canScrollBackward) HorizontalDivider(
                        modifier = Modifier.align(
                            Alignment.TopCenter
                        )
                    )
                    if (state.canScrollForward) HorizontalDivider(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { isDialogShown = false }) {
                    Text(text = "Annulla")
                }
            },
        )
    }
}

@Composable
private fun DialogRow(
    label: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = isSelected,
                onClick = {
                    if (!isSelected) onSelected()
                },
            )
            .fillMaxWidth()
            .minimumInteractiveComponentSize(),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.merge(),
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}
