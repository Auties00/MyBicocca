package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadge
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryServiceGroup
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone

// Outlined directory group: a soft container holding a header (name + caption) and a
// list of service rows separated by hairlines that begin under the row text.
@Composable
fun ServiceGroupCard(
    group: RegistryServiceGroup,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
        border = BorderStroke(1.5.dp, scheme.outlineVariant),
    ) {
        Column {
            Column(modifier = Modifier.padding(start = 16.dp, top = 15.dp, end = 16.dp, bottom = 13.dp)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = scheme.onSurface,
                )
                Text(
                    text = group.caption,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
            }
            group.services.forEach { service ->
                // Every row carries a top hairline (inset under the icon) — the first one
                // doubles as the separator between the group header and its content.
                HorizontalDivider(
                    modifier = Modifier.padding(start = 63.dp),
                    thickness = 1.5.dp,
                    color = scheme.outlineVariant.copy(alpha = 0.55f),
                )
                ServiceRow(
                    title = service.title,
                    subtitle = service.subtitle,
                    badge = service.badge,
                    onClick = service.onClick,
                    icon = { tint ->
                        Icon(
                            imageVector = service.icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(19.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ServiceRow(
    title: String,
    subtitle: String,
    badge: RegistryBadge?,
    onClick: () -> Unit,
    icon: @Composable (tint: androidx.compose.ui.graphics.Color) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(scheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            icon(scheme.onSurface)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.15).sp,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge != null) {
            RegistryBadgePill(badge)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp),
        )
    }
}

// Fixed-width status pill — every badge occupies the same 86.dp slot so the rows align
// down the column regardless of label length (matches the design's `width: 86px`).
@Composable
fun RegistryBadgePill(badge: RegistryBadge) {
    val tone = registryBadgeTone(badge.tone)
    Surface(
        modifier = Modifier.width(86.dp),
        shape = CircleShape,
        color = tone.container,
        contentColor = tone.onContainer,
    ) {
        Text(
            text = badge.label.uppercase(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 4.dp),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
