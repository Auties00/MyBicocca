package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntry
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntryGroup

// A settings group rendered as a connected segmented card, matching the Registry tab's
// directory style: a header tile (name + caption) followed by one tile per entry,
// separated by 2.dp gaps with the outer corners rounded. `accentContainer`/
// `accentOnContainer` colour the per-entry icon chip.
@Composable
fun SettingsEntrySection(
    group: SettingsEntryGroup,
    accentContainer: Color,
    accentOnContainer: Color,
    modifier: Modifier = Modifier,
) {
    val large = 20.dp
    val small = 4.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GroupHeaderTile(
            title = group.name,
            subtitle = group.caption,
            shape = RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small),
        )
        group.entries.forEachIndexed { index, item ->
            val isLast = index == group.entries.lastIndex
            SegmentedEntryTile(
                item = item,
                accentContainer = accentContainer,
                accentOnContainer = accentOnContainer,
                shape = RoundedCornerShape(
                    topStart = small,
                    topEnd = small,
                    bottomStart = if (isLast) large else small,
                    bottomEnd = if (isLast) large else small,
                ),
            )
        }
    }
}

@Composable
private fun GroupHeaderTile(
    title: String,
    subtitle: String,
    shape: Shape,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SegmentedEntryTile(
    item: SettingsEntry,
    accentContainer: Color,
    accentOnContainer: Color,
    shape: Shape,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = accentOnContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(6.dp))
            if (item.switch != null) {
                Switch(checked = item.switch, onCheckedChange = null)
            } else {
                Icon(
                    imageVector = if (item.external) Icons.Rounded.Link else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
