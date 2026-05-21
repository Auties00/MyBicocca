package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDashboardTile
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTileStatus
import it.attendance100.mybicocca.ui.screen.registry.state.colors
import it.attendance100.mybicocca.ui.screen.registry.state.materialShape

@Composable
fun RegistryGridTile(
    tile: RegistryDashboardTile,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = tile.category.colors(scheme)
    val shape = tile.shape.materialShape()

    val container = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.errorContainer
        RegistryTileStatus.Important -> scheme.primaryContainer
        RegistryTileStatus.Normal -> scheme.surfaceContainerHigh
    }
    val titleColor = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.error
        RegistryTileStatus.Important -> scheme.onPrimaryContainer
        RegistryTileStatus.Normal -> scheme.onSurface
    }
    val shapeFill = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.error
        RegistryTileStatus.Important -> scheme.onPrimaryContainer
        RegistryTileStatus.Normal -> palette.accent
    }
    val shapeIconTint = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.onError
        RegistryTileStatus.Important -> scheme.primaryContainer
        RegistryTileStatus.Normal -> palette.onAccent
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>(),
        label = "gridPressScale",
    )

    val tileShape = RoundedCornerShape(
        topStart = 26.dp,
        topEnd = 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 26.dp,
    )

    Surface(
        onClick = tile.onClick,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        color = container,
        contentColor = titleColor,
        shape = tileShape,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(46.dp)
                    .graphicsLayer { rotationZ = 14f }
                    .clip(shape)
                    .background(shapeFill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    tint = shapeIconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tile.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (tile.status == RegistryTileStatus.Warning) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            tint = scheme.error,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                if (tile.subtitle != null) {
                    Text(
                        text = tile.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = titleColor.copy(alpha = 0.70f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
