package it.unimib.indoornav.lab.ui.beacons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.inventory.SavedBeaconEntry
import it.unimib.indoornav.lab.ui.components.BeaconModelView
import it.unimib.indoornav.lab.ui.components.BeaconPose
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.theme.LabTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveBeaconBottomSheet(
    tile: ResolvedBeaconTile,
    defaultBuilding: String,
    defaultFloor: Int,
    onSave: (SavedBeaconEntry) -> Unit,
    onRemove: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        SaveBeaconSheetContent(
            tile = tile,
            defaultBuilding = defaultBuilding,
            defaultFloor = defaultFloor,
            onSave = onSave,
            onRemove = onRemove,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun SaveBeaconSheetContent(
    tile: ResolvedBeaconTile,
    defaultBuilding: String,
    defaultFloor: Int,
    onSave: (SavedBeaconEntry) -> Unit,
    onRemove: ((String) -> Unit)?,
    onCancel: () -> Unit,
) {
    val existing = tile.savedEntry
    val initialFloor = existing?.floor?.takeIf { it.isNotBlank() } ?: "Floor $defaultFloor"
    val initialBuilding = existing?.building?.takeIf { it.isNotBlank() } ?: defaultBuilding

    var label by remember(tile.tileKey) { mutableStateOf(existing?.label ?: tile.displayLabel) }
    var code by remember(tile.tileKey) { mutableStateOf(existing?.alphanumericCode ?: "") }
    var building by remember(tile.tileKey) { mutableStateOf(initialBuilding) }
    var floor by remember(tile.tileKey) { mutableStateOf(initialFloor) }
    var position by remember(tile.tileKey) { mutableStateOf(existing?.positionDescription ?: "") }
    var confirmRemove by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val closeWith: (() -> Unit) -> Unit = { action ->
        focusManager.clearFocus()
        action()
    }

    val previewLabel = label.trim().ifBlank { tile.displayLabel }
    val previewSubLabel = listOf(building.trim(), floor.trim()).filter { it.isNotBlank() }.joinToString(" - ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(width = 58.dp, height = 74.dp),
                contentAlignment = Alignment.Center,
            ) {
                OrganicBeaconHalo(
                    online = true,
                    shape = OrganicShapes.SmoothCookie6,
                    modifier = Modifier.size(66.dp),
                )
                BeaconModelView(
                    label = previewLabel,
                    subLabel = previewSubLabel,
                    pose = BeaconPose.CARD,
                    online = true,
                    modifier = Modifier.size(width = 36.dp, height = 70.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (existing != null) "Edit Saved Beacon" else "Save Beacon to Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = tile.macAddress.ifBlank { tile.canonicalKey },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                placeholder = { Text("B07") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(8) },
                label = { Text("Code (4 chars)") },
                placeholder = { Text("K4M2") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = building,
                onValueChange = { building = it },
                label = { Text("Building") },
                placeholder = { Text("U14") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = floor,
                onValueChange = { floor = it },
                label = { Text("Floor") },
                placeholder = { Text("Floor 1") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = position,
            onValueChange = { position = it },
            label = { Text("Position / Description") },
            placeholder = { Text("Near Lab 102 door (2.2 m pillar)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (existing != null && onRemove != null) {
                TextButton(onClick = { confirmRemove = true }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { closeWith(onCancel) }) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val live = tile.liveBeacon
                    val linked = buildList {
                        existing?.linkedKeys?.let { addAll(it) }
                        live?.canonicalKeys?.let { addAll(it) }
                        if (tile.canonicalKey.isNotBlank()) add(tile.canonicalKey)
                    }.filter { it.isNotBlank() }.distinct()
                    val now = System.currentTimeMillis()
                    val entry = SavedBeaconEntry(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        canonicalKey = live?.identityKey ?: existing?.canonicalKey ?: tile.canonicalKey,
                        macAddress = live?.macAddress?.ifBlank { existing?.macAddress ?: "" }
                            ?: existing?.macAddress
                            ?: tile.macAddress,
                        linkedKeys = linked,
                        label = label.trim().ifBlank { tile.displayLabel },
                        alphanumericCode = code.trim().uppercase(),
                        building = building.trim().ifBlank { defaultBuilding },
                        floor = floor.trim().ifBlank { initialFloor },
                        positionDescription = position.trim(),
                        seenFrameTypes = (existing?.seenFrameTypes ?: emptySet()) + tile.frameTypes,
                        lastRssi = tile.currentRssi ?: existing?.lastRssi,
                        lastMedianRssi = tile.medianRssi ?: existing?.lastMedianRssi,
                        lastTxPowerAdv = tile.txPowerAdv ?: existing?.lastTxPowerAdv,
                        lastBattMv = tile.tlmBattMv ?: existing?.lastBattMv,
                        lastTempC = tile.tlmTempC ?: existing?.lastTempC,
                        lastSeenMs = maxOf(tile.lastSeenMs, existing?.lastSeenMs ?: 0L),
                        createdAtMs = existing?.createdAtMs ?: now,
                        updatedAtMs = now,
                    )
                    closeWith { onSave(entry) }
                },
            ) {
                Text(if (existing != null) "Update" else "Save")
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (confirmRemove && existing != null && onRemove != null) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove saved beacon?") },
            text = { Text("Remove ${existing.label} from your persistent inventory?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemove = false
                        closeWith { onRemove(existing.id) }
                    },
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaveBeaconSheetContentPreview() {
    LabTheme {
        Surface {
            SaveBeaconSheetContent(
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
                defaultBuilding = "U14",
                defaultFloor = 1,
                onSave = {},
                onRemove = null,
                onCancel = {},
            )
        }
    }
}
