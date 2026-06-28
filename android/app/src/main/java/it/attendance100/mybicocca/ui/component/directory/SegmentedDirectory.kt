package it.attendance100.mybicocca.ui.component.directory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * The corner rule of the connected-segmented-card language shared by every directory-style list
 * in the app (Settings, the Registry "servizi" directory, the About/Language sheets, …): outer
 * corners of the group are rounded (20dp), inner corners between rows are tight (4dp). The
 * companion primitives — [SegmentedHeaderTile], [SegmentedTile], [SegmentedIconChip] — all
 * build on this shape.
 */
fun segmentedShape(isFirst: Boolean, isLast: Boolean): RoundedCornerShape {
    val large = 20.dp
    val small = 4.dp
    return RoundedCornerShape(
        topStart = if (isFirst) large else small,
        topEnd = if (isFirst) large else small,
        bottomEnd = if (isLast) large else small,
        bottomStart = if (isLast) large else small,
    )
}

/** The heading row of a segmented group: bold name + caption, with the group's top corners rounded. */
@Composable
fun SegmentedHeaderTile(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentedShape(isFirst = true, isLast = false),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 12.dp
            )
        ) {
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

/** The leading rounded-square icon chip (tinted container + glyph) used by directory tiles. */
@Composable
fun SegmentedIconChip(
    icon: ImageVector,
    container: Color,
    onContainer: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 14.dp,
    iconSize: Dp = 22.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(container, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = onContainer,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * One connected-card row: an optional [leading] slot (icon chip / avatar / flag …), the title
 * and an optional subtitle, and an optional [trailing] slot (chevron / switch / knob / badge …).
 * A non-null [onClick] makes the row tappable; [isFirst]/[isLast] place the row in the group's
 * corner rule via [segmentedShape]. The trailing slot owns its own leading spacing so each
 * caller can reproduce its exact gap.
 */
@Composable
fun SegmentedTile(
    isFirst: Boolean,
    isLast: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val shape = segmentedShape(isFirst, isLast)
    val body: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
    }
    if (onClick != null) {
        Surface(
            onClick = {
                haptic.tap()
                onClick()
            },
            modifier = modifier.fillMaxWidth(),
            color = scheme.surfaceContainer,
            contentColor = scheme.onSurface,
            shape = shape,
        ) { body() }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = scheme.surfaceContainer,
            contentColor = scheme.onSurface,
            shape = shape,
        ) { body() }
    }
}
