package it.attendance100.mybicocca.ui.screen.registry.component

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadge
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryService
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone

// A directory group rendered as a connected segmented card: a header tile (name +
// caption) followed by one tile per service, separated by 2.dp gaps with the outer
// corners rounded. `accentContainer`/`accentOnContainer` colour the per-service icon chip.
@Composable
fun RegistryServiceSection(
    group: RegistryServiceGroup,
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
        CategoryHeaderTile(
            title = group.name,
            subtitle = group.caption,
            shape = RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small),
        )
        group.services.forEachIndexed { index, item ->
            val isLast = index == group.services.lastIndex
            SegmentedTile(
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
private fun CategoryHeaderTile(
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
private fun SegmentedTile(
    item: RegistryService,
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
            IconBadge(
                icon = item.icon,
                background = accentContainer,
                tint = accentOnContainer,
                size = 44.dp,
                cornerRadius = 14.dp,
                iconSize = 22.dp,
            )
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
            if (item.badge != null) {
                Spacer(Modifier.width(10.dp))
                StatusBadge(badge = item.badge)
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = if (item.external) Icons.Rounded.Link else Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
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

@Composable
private fun IconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp,
    cornerRadius: Dp,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}
