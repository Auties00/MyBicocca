package it.unimib.indoornav.lab.ui.beacons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.ui.components.BeaconModelView
import it.unimib.indoornav.lab.ui.components.BeaconPose
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.components.SignalTypeBadge
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import it.unimib.indoornav.lab.ui.theme.LabTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeaconCardGrid(
    tile: ResolvedBeaconTile,
    index: Int,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haloShape = if (index % 2 == 0) OrganicShapes.SmoothCookie6 else OrganicShapes.SmoothCookie8
    val subLabel = listOf(tile.building, tile.floor).filter { it.isNotBlank() }.joinToString(" - ")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    tile.frameTypes.forEach { ft ->
                        SignalTypeBadge(frameType = ft, compact = true)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (tile.isOnline) LabStatusColors.Green else MaterialTheme.colorScheme.outline,
                            ),
                    )
                    Icon(
                        imageVector = if (tile.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (tile.isSaved) "Edit saved beacon" else "Save beacon",
                        tint = if (tile.isSaved) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBookmarkClick),
                    )
                }
            }

            Box(
                modifier = Modifier.size(width = 78.dp, height = 92.dp),
                contentAlignment = Alignment.Center,
            ) {
                OrganicBeaconHalo(
                    online = tile.isOnline,
                    shape = haloShape,
                    modifier = Modifier.size(74.dp),
                )
                BeaconModelView(
                    label = tile.displayLabel,
                    subLabel = subLabel,
                    pose = BeaconPose.CARD,
                    online = tile.isOnline,
                    modifier = Modifier.size(width = 44.dp, height = 88.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tile.alphanumericCode.ifBlank { "----" },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = if (tile.isOnline && tile.currentRssi != null) "${tile.currentRssi}" else "Offline",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tile.isOnline) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BeaconCardGridPreview() {
    LabTheme {
        Surface {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BeaconCardGrid(
                    tile = ResolvedBeaconTile(
                        tileKey = "1",
                        savedEntry = null,
                        liveBeacon = null,
                        isOnline = true,
                        displayLabel = "B07",
                        alphanumericCode = "K4M2",
                        building = "U14",
                        floor = "Floor 1",
                        positionDescription = "",
                        macAddress = "D9:84:CE:34:5C:5C",
                        canonicalKey = "ed:515b86185cea0ec01b22:0000000e0007",
                        frameTypes = listOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM),
                        currentRssi = -54,
                        medianRssi = -55,
                        packetsPerSec = 3.0,
                        txPowerAdv = -40,
                        tlmBattMv = 3100,
                        tlmTempC = 22.0,
                        tlmAdvCount = null,
                        tlmUptimeSec = null,
                        estimatedIntervalByFrame = emptyMap(),
                        rawAdvHexByFrame = emptyMap(),
                        lastSeenMs = 0L,
                    ),
                    index = 0,
                    onClick = {},
                    onBookmarkClick = {},
                    modifier = Modifier.weight(1f),
                )
                BeaconCardGrid(
                    tile = ResolvedBeaconTile(
                        tileKey = "2",
                        savedEntry = null,
                        liveBeacon = null,
                        isOnline = false,
                        displayLabel = "B12",
                        alphanumericCode = "A9P3",
                        building = "U14",
                        floor = "Floor 2",
                        positionDescription = "",
                        macAddress = "AA:BB:CC:DD:EE:FF",
                        canonicalKey = "ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:12",
                        frameTypes = listOf(BeaconFrameType.IBEACON),
                        currentRssi = null,
                        medianRssi = -71,
                        packetsPerSec = null,
                        txPowerAdv = -59,
                        tlmBattMv = null,
                        tlmTempC = null,
                        tlmAdvCount = null,
                        tlmUptimeSec = null,
                        estimatedIntervalByFrame = emptyMap(),
                        rawAdvHexByFrame = emptyMap(),
                        lastSeenMs = 0L,
                    ),
                    index = 1,
                    onClick = {},
                    onBookmarkClick = {},
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
