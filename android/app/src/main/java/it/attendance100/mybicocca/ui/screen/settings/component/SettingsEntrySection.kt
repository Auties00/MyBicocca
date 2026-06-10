package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.component.directory.SegmentedHeaderTile
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntryGroup

/**
 * A settings group rendered as a connected segmented card, matching the Registry tab's
 * directory style: a header tile (name + caption) followed by one tile per entry. The shared
 * [SegmentedTile] primitives carry the layout; this only maps [SettingsEntryGroup] entries to
 * tiles and supplies the trailing element (a switch, or a chevron/link glyph).
 */
@Composable
fun SettingsEntrySection(
    group: SettingsEntryGroup,
    accentContainer: Color,
    accentOnContainer: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SegmentedHeaderTile(title = group.name, subtitle = group.caption)
        group.entries.forEachIndexed { index, item ->
            SegmentedTile(
                isFirst = false,
                isLast = index == group.entries.lastIndex,
                title = item.title,
                subtitle = item.subtitle,
                onClick = item.onClick,
                leading = { SegmentedIconChip(item.icon, accentContainer, accentOnContainer) },
                trailing = {
                    Spacer(Modifier.width(6.dp))
                    val switch = item.switch
                    if (switch != null) {
                        Switch(checked = switch, onCheckedChange = null)
                    } else {
                        Icon(
                            imageVector = if (item.external) Icons.Rounded.Link else Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
            )
        }
    }
}
