package it.unimib.indoornav.lab.ui.beacons

import it.unimib.indoornav.lab.ui.components.SlotBadge
import it.unimib.indoornav.lab.inventory.SlotPills
import it.unimib.indoornav.lab.ui.components.BatteryBadge
import it.unimib.indoornav.lab.inventory.BeaconBattery
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
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.components.SignalTypeBadge
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import it.unimib.indoornav.lab.ui.theme.LabTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeaconCardList(
    tile: ResolvedBeaconTile,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subLabel = listOf(tile.building, tile.floor).filter { it.isNotBlank() }.joinToString(" - ")
    val locationLine = listOf(tile.building, tile.floor, tile.positionDescription)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { tile.macAddress.ifBlank { tile.canonicalKey } }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BeaconHero(
                tileKey = tile.tileKey,
                label = tile.displayLabel,
                subLabel = subLabel,
                online = tile.isOnline,
                stale = tile.isStale,
                trackerKeys = tile.trackerKeys(),
                cardShape = OrganicShapes.cardShapeFor(tile.tileKey),
                inDetail = false,
                size = 96.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tile.displayLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (tile.alphanumericCode.isNotBlank()) {
                        Text(
                            text = tile.alphanumericCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    SlotPills.of(tile).forEach { (slot, state) -> SlotBadge(slot, state) }
                }

                Text(
                    text = locationLine,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tile.savedEntry?.let { BeaconBattery.estimate(it, System.currentTimeMillis()) }?.let {
                        BatteryBadge(it.remainingFraction)
                    }
                    if (tile.isOnline && tile.currentRssi != null) {
                        val ppsText = "%.1f pkt/s".format(Locale.US, tile.packetsPerSec ?: 0.0)
                        Text(
                            text = ppsText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                        Text(
                            text = "Tx ${tile.txPowerAdv ?: "-"} · ${tile.currentRssi} dBm",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    } else {
                        Text(
                            text = "Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BeaconCardListPreview() {
    LabTheme {
        Surface {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BeaconCardList(
                    tile = ResolvedBeaconTile(
                        tileKey = "1",
                        savedEntry = null,
                        liveBeacon = null,
                        isOnline = true,
                        displayLabel = "B07",
                        alphanumericCode = "K4M2",
                        building = "U14",
                        floor = "Floor 1",
                        positionDescription = "Near Lab 102 door",
                        macAddress = "D9:84:CE:34:5C:5C",
                        canonicalKey = "ed:515b86185cea0ec01b22:0000000e0007",
                        frameTypes = listOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM),
                        currentRssi = -54,
                        medianRssi = -56,
                        packetsPerSec = 4.2,
                        txPowerAdv = -40,
                        tlmBattMv = 3120,
                        tlmTempC = 22.4,
                        tlmAdvCount = 12345L,
                        tlmUptimeSec = 3600.0,
                        estimatedIntervalByFrame = mapOf(BeaconFrameType.ED_UID to 200),
                        rawAdvHexByFrame = emptyMap(),
                        lastSeenMs = 1_795_220_000_000L,
                    ),
                    index = 0,
                    onClick = {},
                )
                BeaconCardList(
                    tile = ResolvedBeaconTile(
                        tileKey = "2",
                        savedEntry = null,
                        liveBeacon = null,
                        isOnline = false,
                        displayLabel = "B12",
                        alphanumericCode = "A9P3",
                        building = "U14",
                        floor = "Floor 2",
                        positionDescription = "Stairwell landing",
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
                        lastSeenMs = 1_795_219_900_000L,
                    ),
                    index = 1,
                    onClick = {},
                )
            }
        }
    }
}
