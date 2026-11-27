package it.unimib.indoornav.lab.ui.beacons

import it.unimib.indoornav.lab.inventory.BeaconPhotos
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.theme.LabTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.input.KeyboardType
import it.unimib.indoornav.lab.inventory.BeaconBattery
import it.unimib.indoornav.lab.inventory.BeaconSlot
import it.unimib.indoornav.lab.ui.components.BatteryBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    onSetPositionOnMap: ((String) -> Unit)? = null,
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
            onSetPositionOnMap = onSetPositionOnMap,
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
    onSetPositionOnMap: ((String) -> Unit)? = null,
    onCancel: () -> Unit,
) {
    val existing = tile.savedEntry
    val initialFloor = existing?.floor?.takeIf { it.isNotBlank() } ?: "Floor $defaultFloor"
    val initialBuilding = existing?.building?.takeIf { it.isNotBlank() } ?: defaultBuilding

    // An unsaved tile's display label is its beacon ID (e.g. 01A08C); its last 4 characters are the code.
    val suggestedCode = tile.displayLabel.filter { it.isLetterOrDigit() }.takeLast(4).uppercase()
    var label by remember(tile.tileKey) { mutableStateOf(existing?.label ?: "") }
    var code by remember(tile.tileKey) { mutableStateOf(existing?.alphanumericCode ?: suggestedCode) }
    var building by remember(tile.tileKey) { mutableStateOf(initialBuilding) }
    var floor by remember(tile.tileKey) { mutableStateOf(initialFloor) }
    var position by remember(tile.tileKey) { mutableStateOf(existing?.positionDescription ?: "") }
    var confirmRemove by remember { mutableStateOf(false) }
    var intervalText by remember(tile.tileKey) {
        mutableStateOf((existing?.advIntervalMs ?: BeaconBattery.DEFAULT_INTERVAL_MS).toString())
    }
    var slots by remember(tile.tileKey) {
        mutableStateOf(
            existing?.let { BeaconBattery.slotsOf(it.copy(seenFrameTypes = it.seenFrameTypes + tile.frameTypes)) }
                ?: BeaconBattery.slotsFromFrames(tile.frameTypes),
        )
    }
    var batteryResetAt by remember(tile.tileKey) { mutableStateOf(existing?.batteryResetAtMs) }
    var photoFile by remember(tile.tileKey) { mutableStateOf(existing?.photoFile) }
    val context = LocalContext.current
    val entryId = remember(tile.tileKey) { existing?.id ?: UUID.randomUUID().toString() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching { BeaconPhotos.importPhoto(context, uri, entryId) }.onSuccess { photoFile = it }
        }
    }

    val focusManager = LocalFocusManager.current
    val closeWith: (() -> Unit) -> Unit = { action ->
        focusManager.clearFocus()
        action()
    }

    val buildEntry: () -> SavedBeaconEntry = {
        val live = tile.liveBeacon
        val linked = buildList {
            existing?.linkedKeys?.let { addAll(it) }
            live?.canonicalKeys?.let { addAll(it) }
            if (tile.canonicalKey.isNotBlank()) add(tile.canonicalKey)
        }.filter { it.isNotBlank() }.distinct()
        val now = System.currentTimeMillis()
        SavedBeaconEntry(
            id = entryId,
            canonicalKey = live?.identityKey ?: existing?.canonicalKey ?: tile.canonicalKey,
            macAddress = live?.macAddress?.ifBlank { existing?.macAddress ?: "" }
                ?: existing?.macAddress
                ?: tile.macAddress,
            linkedKeys = linked,
            label = label.trim().ifBlank { code.trim().uppercase() },
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
            batteryResetAtMs = batteryResetAt,
            advIntervalMs = intervalText.toIntOrNull()?.coerceIn(20, 10_000)
                ?: BeaconBattery.DEFAULT_INTERVAL_MS,
            activeSlots = slots,
            x = existing?.x,
            y = existing?.y,
            photoFile = photoFile,
        )
    }
    val saveAnd: (afterSave: (SavedBeaconEntry) -> Unit) -> Unit = { after ->
        val entry = buildEntry()
        if (existing?.photoFile != photoFile) BeaconPhotos.delete(context, existing?.photoFile)
        closeWith {
            onSave(entry)
            after(entry)
        }
    }

    val previewLabel = label.trim().ifBlank { code.trim() }
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
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center,
            ) {
                OrganicBeaconHalo(
                    online = true,
                    shape = OrganicShapes.cardShapeFor(tile.tileKey),
                    modifier = Modifier.fillMaxSize(0.86f),
                )
                BeaconModelView(
                    label = previewLabel,
                    subLabel = previewSubLabel,
                    progress = 0f,
                    online = true,
                    modifier = Modifier.fillMaxSize(),
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
                placeholder = { Text("B1") },
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

        if (onSetPositionOnMap != null) {
            OutlinedButton(
                onClick = { saveAnd { onSetPositionOnMap(it.id) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (existing?.x != null) "Move on map" else "Set position on map")
            }
        }

        PhotoSection(
            photoFile = photoFile,
            onPick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemove = { photoFile = null },
        )

        BatterySection(
            intervalText = intervalText,
            onIntervalChange = { intervalText = it.filter(Char::isDigit).take(5) },
            slots = slots,
            onSlotsChange = { slots = it },
            batteryResetAt = batteryResetAt,
            onReset = { batteryResetAt = System.currentTimeMillis() },
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
                onClick = { saveAnd {} },
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

@Composable
private fun PhotoSection(photoFile: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(photoFile) {
        photoFile?.let { runCatching { BitmapFactory.decodeFile(BeaconPhotos.file(context, it).path)?.asImageBitmap() }.getOrNull() }
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Reference photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Reference photo", fontWeight = FontWeight.Bold)
                Text(
                    if (bitmap != null) "Shown in the beacon's detail screen" else "A photo of where it is mounted",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (bitmap != null) TextButton(onClick = onRemove) { Text("Remove") }
            OutlinedButton(onClick = onPick) { Text(if (bitmap != null) "Replace" else "Add") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BatterySection(
    intervalText: String,
    onIntervalChange: (String) -> Unit,
    slots: Set<BeaconSlot>,
    onSlotsChange: (Set<BeaconSlot>) -> Unit,
    batteryResetAt: Long?,
    onReset: () -> Unit,
) {
    val interval = intervalText.toIntOrNull()?.coerceIn(20, 10_000) ?: BeaconBattery.DEFAULT_INTERVAL_MS
    val lifeDays = BeaconBattery.lifeDays(interval, slots)
    val estimate = batteryResetAt?.let {
        BeaconBattery.estimate(
            SavedBeaconEntry(
                id = "", canonicalKey = "", macAddress = "", label = "", building = "", floor = "",
                batteryResetAtMs = it, advIntervalMs = interval, activeSlots = slots,
            ),
            System.currentTimeMillis(),
        )
    }
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Battery (CR2477)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                estimate?.let { BatteryBadge(it.remainingFraction) }
            }
            Text(
                "Slots switched on in Runestone and their interval set the expected life.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BeaconSlot.entries.forEach { slot ->
                    FilterChip(
                        selected = slot in slots,
                        onClick = { onSlotsChange(if (slot in slots) slots - slot else slots + slot) },
                        label = { Text(slot.label) },
                    )
                }
            }
            OutlinedTextField(
                value = intervalText,
                onValueChange = onIntervalChange,
                label = { Text("Advertising interval (ms)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Expected life: about ${formatLife(lifeDays)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when {
                    batteryResetAt == null -> "Not tracked yet. Reset when you fit a new battery."
                    estimate == null -> ""
                    else -> "Battery reset on ${dateFormat.format(Date(batteryResetAt))}, " +
                        "replace by about ${dateFormat.format(Date(estimate.replaceByMs))}"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("Reset battery expectancy (new battery fitted)")
            }
        }
    }
}

internal fun formatLife(days: Double): String = when {
    days >= 365.25 -> "%.1f years".format(Locale.US, days / 365.25)
    days >= 60 -> "%.0f months".format(Locale.US, days / 30.44)
    else -> "%.0f days".format(Locale.US, days)
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
