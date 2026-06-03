package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Section label above a group of setting rows. */
@Composable
fun SettingsSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/**
 * A title + optional subtitle row with a trailing [Switch]. The whole row is clickable.
 * The switch reflects [checked] and never flips optimistically: [onClick] initiates the change and
 * the caller updates [checked] once it is actually committed.
 */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingTexts(
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = { onClick() }, enabled = enabled)
    }
}

/**
 * A clickable row showing a title + optional subtitle on the left and an optional [value] on the right.
 */
@Composable
fun SettingsClickableRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SettingTexts(
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        value?.let {
            Spacer(Modifier.width(16.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingTexts(
    title: String,
    subtitle: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.graphicsLayer { alpha = if (enabled) 1f else 0.38f }) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = LocalContentColor.current,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
