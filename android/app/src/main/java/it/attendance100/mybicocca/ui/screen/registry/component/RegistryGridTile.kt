package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Euro
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryAccentShape
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryCategory
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDashboardTile
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTileStatus
import it.attendance100.mybicocca.ui.screen.registry.state.colors
import it.attendance100.mybicocca.ui.screen.registry.state.materialShape
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

@Composable
fun RegistryGridTile(
    tile: RegistryDashboardTile,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
    wide: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = tile.category.colors(scheme)
    val shape = tile.shape.materialShape()

    val container = when {
        tile.status == RegistryTileStatus.Warning -> scheme.errorContainer
        tile.status == RegistryTileStatus.Important -> scheme.primaryContainer
        inverted -> palette.accent
        else -> scheme.surfaceContainerHigh
    }
    val titleColor = when {
        tile.status == RegistryTileStatus.Warning -> scheme.error
        tile.status == RegistryTileStatus.Important -> scheme.onPrimaryContainer
        inverted -> palette.onAccent
        else -> scheme.onSurface
    }
    val shapeFill = when {
        tile.status == RegistryTileStatus.Warning -> scheme.error
        tile.status == RegistryTileStatus.Important -> scheme.onPrimaryContainer
        inverted -> palette.onAccent
        else -> palette.accent
    }
    val shapeIconTint = when {
        tile.status == RegistryTileStatus.Warning -> scheme.onError
        tile.status == RegistryTileStatus.Important -> scheme.primaryContainer
        inverted -> palette.accent
        else -> palette.onAccent
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "gridPressScale",
    )

    val tileShape = if (!tile.mirrored) RoundedCornerShape(
        topStart = 26.dp,
        topEnd = 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 26.dp,
    ) else RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 26.dp,
        bottomStart = 26.dp,
        bottomEnd = 12.dp,
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
        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer { rotationZ = 14f }
                        .clip(shape)
                        .background(shapeFill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = null,
                        tint = shapeIconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tile.title,
                            style = MaterialTheme.typography.titleMedium,
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
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = tile.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = titleColor.copy(alpha = 0.70f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
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
}

@Preview(showBackground = true)
@Composable
private fun RegistryGridTilePreview() {
    BicoccaTheme(dark = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RegistryGridTile(
                    tile = RegistryDashboardTile(
                        id = "study_plan",
                        title = "Piano di Studi",
                        subtitle = null,
                        status = RegistryTileStatus.Normal,
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        shape = RegistryAccentShape.Clover4,
                        category = RegistryCategory.Teaching,
                        onClick = {},
                        mirrored = false,
                    ),
                    modifier = Modifier.size(160.dp, 124.dp)
                )
                RegistryGridTile(
                    tile = RegistryDashboardTile(
                        id = "study_plan_inverted",
                        title = "Piano di Studi",
                        subtitle = null,
                        status = RegistryTileStatus.Normal,
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        shape = RegistryAccentShape.Clover4,
                        category = RegistryCategory.Teaching,
                        onClick = {},
                        mirrored = true,
                    ),
                    inverted = true,
                    modifier = Modifier.size(160.dp, 124.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RegistryGridTile(
                    tile = RegistryDashboardTile(
                        id = "taxes_warning",
                        title = "Tasse",
                        subtitle = "1 pagamento in ritardo",
                        status = RegistryTileStatus.Warning,
                        icon = Icons.Outlined.Euro,
                        shape = RegistryAccentShape.Cookie6,
                        category = RegistryCategory.Taxes,
                        onClick = {},
                        mirrored = false,
                    ),
                    modifier = Modifier.size(160.dp, 124.dp)
                )
                RegistryGridTile(
                    tile = RegistryDashboardTile(
                        id = "important",
                        title = "Avviso Importante",
                        subtitle = null,
                        status = RegistryTileStatus.Important,
                        icon = Icons.Default.Warning,
                        shape = RegistryAccentShape.Burst,
                        category = RegistryCategory.Exams,
                        onClick = {},
                        mirrored = true,
                    ),
                    modifier = Modifier.size(160.dp, 124.dp)
                )
            }
            RegistryGridTile(
                tile = RegistryDashboardTile(
                    id = "wide",
                    title = "Autocertificazioni",
                    subtitle = "Apri e consulta i documenti",
                    status = RegistryTileStatus.Normal,
                    icon = Icons.Outlined.Euro,
                    shape = RegistryAccentShape.Cookie9,
                    category = RegistryCategory.Documents,
                    onClick = {},
                    mirrored = false,
                ),
                inverted = true,
                wide = true,
                modifier = Modifier.size(328.dp, 124.dp)
            )
        }
    }
}
