package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.component.directory.SegmentedHeaderTile
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadge
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone

/**
 * A directory group rendered as a connected segmented card via the shared SegmentedTile
 * primitives: a header tile, then one tile per service with an accent icon chip leading
 * and an optional status badge plus a chevron (or a link icon for external entries)
 * trailing.
 *
 * [tileTag] supplies an optional `testTag` per service tile so UI tests can address an
 * individual row by its stable id; the shared [SegmentedTile] primitive itself carries no
 * tag, so the marker lands here at the call site.
 */
@Composable
fun RegistryServiceSection(
    group: RegistryServiceGroup,
    accentContainer: Color,
    accentOnContainer: Color,
    modifier: Modifier = Modifier,
    tileTag: ((RegistryService) -> String)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SegmentedHeaderTile(title = group.name, subtitle = group.caption)
        group.services.forEachIndexed { index, item ->
            val tileModifier = tileTag?.let { Modifier.testTag(it(item)) } ?: Modifier
            SegmentedTile(
                isFirst = false,
                isLast = index == group.services.lastIndex,
                modifier = tileModifier,
                title = item.title,
                subtitle = item.subtitle,
                onClick = item.onClick,
                leading = { SegmentedIconChip(item.icon, accentContainer, accentOnContainer) },
                trailing = {
                    val badge = item.badge
                    if (badge != null) {
                        Spacer(Modifier.width(10.dp))
                        StatusBadge(badge = badge)
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (item.external) Icons.Rounded.Link else Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun StatusBadge(badge: RegistryBadge) {
    val tone = registryBadgeTone(badge.tone)
    Box(
        modifier = Modifier
            .background(tone.container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = badge.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone.onContainer,
            maxLines = 1,
        )
    }
}
