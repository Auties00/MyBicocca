package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryOccupancyLevel
import it.attendance100.mybicocca.domain.model.library.occupancyLevelOf
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.crowdingArmchairsDescription
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.libraryBuildingLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.occupancyAccentColor
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

private val LibraryCardShape = RoundedCornerShape(24.dp)

/**
 * One bookable library card on the Libraries screen: 2:1 photo header (desaturated and dimmed when
 * closed), status line opposite three qualitative crowding armchairs, bold name, building/floor
 * line and square tonal chevron.
 */
@Composable
internal fun LibraryCard(
    library: Library,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val status = library.liveStatus
    val isOpen = status?.isOpen == true
    val isClosed = status != null && !status.isOpen
    val crowdingLevel = if (isOpen) status.occupancyPercentage?.let(::occupancyLevelOf) else null
    val buildingLine = libraryBuildingLine(library)

    Surface(
        onClick = { haptic.tap(); onClick() },
        modifier = modifier.fillMaxWidth(),
        shape = LibraryCardShape,
        color = scheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column {
            LibraryPhoto(
                libraryId = library.id,
                pictureUrl = library.pictureUrl,
                overlayClipShape = LibraryCardShape,
                dimmedAndDesaturated = isClosed,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f),
            )

            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val statusTint = if (isOpen) registryAccent(RegistryAccent.Success) else scheme.onSurfaceVariant
                    val statusLabel = status?.statusText?.takeIf { it.isNotBlank() }
                        ?: when {
                            isOpen -> stringResource(R.string.library_open)
                            isClosed -> stringResource(R.string.library_closed)
                            else -> null
                        }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (statusLabel != null) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = statusTint,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = statusTint,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    CrowdingArmchairs(level = crowdingLevel)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (buildingLine != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = if (isClosed) scheme.onSurfaceVariant else scheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = buildingLine,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(scheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = scheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Qualitative three-armchair crowding indicator: 1 green / 2 amber / 3 red filled icons when live
 * occupancy is known while open, or three muted outlines when closed or unknown.
 */
@Composable
internal fun CrowdingArmchairs(
    level: LibraryOccupancyLevel?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val description = crowdingArmchairsDescription(level)
    val filledCount = when (level) {
        LibraryOccupancyLevel.Low -> 1
        LibraryOccupancyLevel.Moderate -> 2
        LibraryOccupancyLevel.High -> 3
        null -> 0
    }
    val activeTint = level?.let { occupancyAccentColor(it) }
    val outlineTint = activeTint?.copy(alpha = 0.45f) ?: scheme.onSurfaceVariant.copy(alpha = 0.55f)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val filled = index < filledCount && activeTint != null
            Icon(
                imageVector = if (filled) Icons.Rounded.Chair else Icons.Outlined.Chair,
                contentDescription = null,
                tint = if (filled) activeTint else outlineTint,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Preview(name = "LibraryCard Open · Light", showBackground = true)
@Composable
private fun LibraryCardOpenLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "LibraryCard Open · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryCardOpenDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "LibraryCard Closed · Light", showBackground = true)
@Composable
private fun LibraryCardClosedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCard(
            library = LibraryPreviewDefaults.Libraries.last(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "LibraryCard Closed · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryCardClosedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCard(
            library = LibraryPreviewDefaults.Libraries.last(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "LibraryCard Unknown Occupancy · Light", showBackground = true)
@Composable
private fun LibraryCardUnknownOccupancyLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCard(
            library = LibraryPreviewDefaults.LibraryUnknownOccupancy,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "LibraryCard Unknown Occupancy · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryCardUnknownOccupancyDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCard(
            library = LibraryPreviewDefaults.LibraryUnknownOccupancy,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
