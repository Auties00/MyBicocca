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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

enum class RegistryHeroLayout { Portrait, Banner }

@Composable
fun RegistryHeroTile(
    tile: RegistryDashboardTile,
    layout: RegistryHeroLayout,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = tile.category.colors(scheme)
    val shape = tile.shape.materialShape()

    val container = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.errorContainer
        else -> palette.bigContainer
    }
    val onContainer = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.onErrorContainer
        else -> palette.bigOnContainer
    }
    val shapeFill = scheme.surface
    val shapeIconTint = when (tile.status) {
        RegistryTileStatus.Warning -> scheme.error
        else -> palette.bigContainer
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "heroPressScale",
    )

    val tileShape = if (!tile.mirrored) RoundedCornerShape(
        topStart = 36.dp,
        topEnd = 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 36.dp,
    ) else RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 36.dp,
        bottomStart = 36.dp,
        bottomEnd = 18.dp,
    )

    Surface(
        onClick = tile.onClick,
        interactionSource = interaction,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        color = container,
        contentColor = onContainer,
        shape = tileShape,
    ) {
        when (layout) {
            RegistryHeroLayout.Portrait -> PortraitContent(
                tile = tile,
                shape = shape,
                onContainer = onContainer,
                shapeFill = shapeFill,
                shapeIconTint = shapeIconTint,
                errorTint = scheme.error,
            )

            RegistryHeroLayout.Banner -> BannerContent(
                tile = tile,
                shape = shape,
                onContainer = onContainer,
                shapeFill = shapeFill,
                shapeIconTint = shapeIconTint,
                errorTint = scheme.error,
            )
        }
    }
}

@Composable
private fun PortraitContent(
    tile: RegistryDashboardTile,
    shape: Shape,
    onContainer: Color,
    shapeFill: Color,
    shapeIconTint: Color,
    errorTint: Color,
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 24.dp, y = (-24).dp)
                .size(150.dp)
                .graphicsLayer { rotationZ = -10f }
                .clip(shape)
                .background(onContainer.copy(alpha = 0.16f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .size(72.dp)
                .graphicsLayer { rotationZ = 14f }
                .clip(shape)
                .background(shapeFill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                tint = shapeIconTint,
                modifier = Modifier.size(36.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (tile.status == RegistryTileStatus.Warning) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = errorTint,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (tile.subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            OpenChip(onContainer = onContainer)
        }
    }
}

@Composable
private fun BannerContent(
    tile: RegistryDashboardTile,
    shape: Shape,
    onContainer: Color,
    shapeFill: Color,
    shapeIconTint: Color,
    errorTint: Color,
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .size(180.dp)
                .graphicsLayer { rotationZ = -8f }
                .clip(shape)
                .background(onContainer.copy(alpha = 0.14f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(98.dp)
                .graphicsLayer { rotationZ = 12f }
                .clip(shape)
                .background(shapeFill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                tint = shapeIconTint,
                modifier = Modifier.size(48.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 22.dp, end = 140.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tile.status == RegistryTileStatus.Warning) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = errorTint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (tile.subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.80f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            OpenChip(onContainer = onContainer)
        }
    }
}

@Composable
private fun OpenChip(onContainer: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(onContainer.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = "Apri",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = onContainer,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.ArrowOutward,
            contentDescription = null,
            tint = onContainer,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistryHeroTilePortraitPreview() {
    BicoccaTheme(dark = false) {
        RegistryHeroTile(
            tile = RegistryDashboardTile(
                id = "exams",
                title = "Esami e Libretto",
                subtitle = "Vedi i tuoi voti e prenota esami",
                icon = Icons.Rounded.School,
                shape = RegistryAccentShape.Sunny,
                category = RegistryCategory.Exams,
                onClick = {}
            ),
            layout = RegistryHeroLayout.Portrait,
            modifier = Modifier
                .padding(16.dp)
                .size(width = 160.dp, height = 220.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistryHeroTileBannerPreview() {
    BicoccaTheme(dark = false) {
        RegistryHeroTile(
            tile = RegistryDashboardTile(
                id = "exams",
                title = "Esami e Libretto",
                subtitle = "Vedi i tuoi voti e prenota esami",
                icon = Icons.Rounded.School,
                shape = RegistryAccentShape.Sunny,
                category = RegistryCategory.Exams,
                onClick = {}
            ),
            layout = RegistryHeroLayout.Banner,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistryHeroTileWarningPreview() {
    BicoccaTheme(dark = false) {
        RegistryHeroTile(
            tile = RegistryDashboardTile(
                id = "taxes",
                title = "Tasse e Pagamenti",
                subtitle = "Hai pagamenti in sospeso",
                status = RegistryTileStatus.Warning,
                icon = Icons.Rounded.School,
                shape = RegistryAccentShape.SoftBurst,
                category = RegistryCategory.Taxes,
                onClick = {}
            ),
            layout = RegistryHeroLayout.Banner,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}
