package it.unimib.indoornav.lab.ui

import it.unimib.indoornav.lab.ui.components.BeaconFilterField
import it.unimib.indoornav.lab.inventory.BeaconFilter
import androidx.compose.runtime.saveable.rememberSaveable
import android.annotation.SuppressLint
import android.net.wifi.ScanResult as WifiScanResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.inventory.BeaconGrouping
import it.unimib.indoornav.lab.inventory.BeaconInventoryRepository
import it.unimib.indoornav.lab.inventory.BeaconViewMode
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.survey.BeaconInventoryItem
import it.unimib.indoornav.lab.survey.SurveyManager
import it.unimib.indoornav.lab.survey.SurveyUiState
import it.unimib.indoornav.lab.survey.WifiSurveyAp
import it.unimib.indoornav.lab.ui.beacons.BeaconCardGrid
import it.unimib.indoornav.lab.ui.beacons.BeaconCardList
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailHost
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailOverlay
import it.unimib.indoornav.lab.ui.beacons.SaveBeaconBottomSheet
import it.unimib.indoornav.lab.ui.components.SegmentedSwitch
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import it.unimib.indoornav.lab.ui.theme.LabTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SurveyScreen(
    surveyState: SurveyUiState,
    config: SetupConfig,
    surveyManager: SurveyManager,
    inventoryRepository: BeaconInventoryRepository,
    onUpdateConfig: ((SetupConfig) -> SetupConfig) -> Unit,
    onShowOnMap: (String) -> Unit = {},
    onPlaceOnMap: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Beacons", "Wi-Fi", "RTT", "Conn. AP")

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title) },
                )
            }
        }

        surveyState.statusMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(msg, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
        }

        when (selectedTab) {
            0 -> BeaconInventoryTab(surveyState, config, surveyManager, inventoryRepository, onShowOnMap, onPlaceOnMap)
            1 -> WifiSurveyTab(surveyState, config, surveyManager, onUpdateConfig)
            2 -> RttSurveyTab(surveyState, surveyManager)
            3 -> ConnectedApTab(surveyState, surveyManager)
        }
    }
}

@Composable
private fun BeaconInventoryTab(
    state: SurveyUiState,
    config: SetupConfig,
    manager: SurveyManager,
    inventoryRepository: BeaconInventoryRepository,
    onShowOnMap: (String) -> Unit,
    onPlaceOnMap: (String) -> Unit,
) {
    val savedEntries by inventoryRepository.entries.collectAsState()
    var viewMode by remember { mutableStateOf(BeaconViewMode.LIST) }
    var detailTileKey by remember { mutableStateOf<String?>(null) }
    var editingTile by remember { mutableStateOf<ResolvedBeaconTile?>(null) }
    // The list as it was when Snapshot was pressed; the live list keeps changing underneath.
    var frozenSnapshot by remember { mutableStateOf<BeaconSnapshot?>(null) }

    val grouped = remember(state.beacons) {
        BeaconGrouping.groupByPhysicalBeacon(state.beacons)
    }
    var filterQuery by rememberSaveable { mutableStateOf("") }
    var filterRegex by rememberSaveable { mutableStateOf(false) }
    val allTiles = remember(grouped, savedEntries) {
        BeaconGrouping.resolveSurveyTiles(grouped, savedEntries, System.currentTimeMillis())
    }
    val filteredTiles = remember(allTiles, filterQuery, filterRegex) {
        BeaconFilter.apply(allTiles, filterQuery, filterRegex)
    }
    val tiles = filteredTiles ?: emptyList()
    // Keeps the detail drawable while it animates out after its beacon stopped transmitting.
    val lastKnownTiles = remember { mutableMapOf<String, ResolvedBeaconTile>() }
    remember(allTiles) { allTiles.forEach { lastKnownTiles[it.tileKey] = it } }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    BeaconDetailHost(
        detailKey = detailTileKey,
        onClose = { detailTileKey = null },
        list = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            if (state.beaconScanning) manager.stopBeaconInventory()
                            else manager.startBeaconInventory()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            if (state.beaconScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            when {
                                state.beaconScanning -> "Stop Scan"
                                state.beaconScanPending -> "Starting..."
                                else -> "Start Inventory"
                            },
                        )
                    }
                    OutlinedButton(
                        onClick = { frozenSnapshot = BeaconSnapshot(state.beacons, System.currentTimeMillis()) },
                        enabled = state.beacons.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Snapshot")
                    }
                }

                SegmentedSwitch(
                    options = BeaconViewMode.entries,
                    selected = viewMode,
                    onSelected = { viewMode = it },
                    label = { it.label },
                    leadingIcon = { mode, tint ->
                        Icon(
                            imageVector = if (mode == BeaconViewMode.LIST) {
                                Icons.AutoMirrored.Filled.FormatListBulleted
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Min RSSI: ${state.minRssiFilter} dBm", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Beacons: ${tiles.size} (${state.beacons.size} frames) | Other/s: ${state.otherBlePerSec}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Slider(
                        value = state.minRssiFilter.toFloat(),
                        onValueChange = { manager.setMinRssiFilter(it.toInt()) },
                        valueRange = -100f..-30f,
                    )
                }

                BeaconFilterField(
                    query = filterQuery,
                    onQueryChange = { filterQuery = it },
                    regex = filterRegex,
                    onRegexChange = { filterRegex = it },
                    invalid = filteredTiles == null,
                )

                if (viewMode == BeaconViewMode.LIST) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        itemsIndexed(tiles, key = { _, tile -> tile.tileKey }) { idx, tile ->
                            BeaconCardList(
                                tile = tile,
                                index = idx,
                                onClick = { detailTileKey = tile.tileKey },
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        itemsIndexed(tiles, key = { _, tile -> tile.tileKey }) { idx, tile ->
                            BeaconCardGrid(
                                tile = tile,
                                index = idx,
                                onClick = { detailTileKey = tile.tileKey },
                            )
                        }
                    }
                }
            }
        },
        detail = { key ->
            val tile = allTiles.firstOrNull { it.tileKey == key } ?: lastKnownTiles[key]
            if (tile != null) {
                BeaconDetailOverlay(
                    tile = tile,
                    onClose = { detailTileKey = null },
                    onEdit = { editingTile = tile },
                    onShowOnMap = tile.savedEntry?.let { entry -> { onShowOnMap(entry.id) } },
                    onRemove = { tile.savedEntry?.let { inventoryRepository.remove(it.id) } },
                )
            }
        },
    )

    editingTile?.let { tile ->
        SaveBeaconBottomSheet(
            tile = tile,
            defaultBuilding = config.building,
            defaultFloor = config.floor,
            onSave = { entry ->
                inventoryRepository.upsert(entry)
                editingTile = null
            },
            onRemove = { id ->
                inventoryRepository.remove(id)
                editingTile = null
            },
            onDismiss = { editingTile = null },
            onSetPositionOnMap = { id ->
                editingTile = null
                onPlaceOnMap(id)
            },
        )
    }

    frozenSnapshot?.let { snap ->
        BeaconSnapshotSheet(
            snapshot = snap,
            onSave = { note ->
                manager.saveBeaconSnapshot(note, snap.beacons, snap.takenAtMs)
                frozenSnapshot = null
            },
            onDismiss = { frozenSnapshot = null },
        )
    }
}

private data class BeaconSnapshot(val beacons: List<BeaconInventoryItem>, val takenAtMs: Long)

@Composable
private fun SnapshotBeaconCard(item: BeaconInventoryItem) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.projectLabel ?: item.frameType.wireName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.projectLabel != null) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    )
                    Text(
                        item.frameType.wireName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text(
                    "${item.currentRssi} dBm (med ${item.medianRssi})",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (item.canonicalId.isNotEmpty()) {
                Text(
                    item.canonicalId,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("addr ${item.addr}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                Text(
                    "%.1f pkt/s • tx ${item.txPowerAdv ?: "-"}".format(Locale.US, item.packetsPerSec),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (item.tlmBattMv != null || item.tlmTempC != null) {
                Text(
                    "TLM: ${item.tlmBattMv ?: "-"} mV, ${item.tlmTempC?.let { "%.1f °C".format(Locale.US, it) } ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeaconSnapshotSheet(
    snapshot: BeaconSnapshot,
    onSave: (note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        BeaconSnapshotContent(snapshot, onSave, onDismiss)
    }
}

@Composable
private fun BeaconSnapshotContent(
    snapshot: BeaconSnapshot,
    onSave: (note: String) -> Unit,
    onCancel: () -> Unit,
) {
    var note by remember { mutableStateOf("") }
    val time = remember(snapshot.takenAtMs) {
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(snapshot.takenAtMs))
    }
    // Note and buttons sit above the list, so the keyboard never hides them.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Save beacon snapshot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "${snapshot.beacons.size} beacon message(s) as at $time. This list is frozen; the scan keeps running behind it.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Location note (e.g. U14 F1 CP3)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = { onSave(note.trim()) }) { Text("Save CSV") }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(snapshot.beacons, key = { it.key }) { item -> SnapshotBeaconCard(item) }
        }
    }
}

private data class WifiSnapshot(val aps: List<WifiSurveyAp>, val takenAtMs: Long)

@Composable
private fun WifiApCard(ap: WifiSurveyAp) {
    val containerColor = if (ap.isConnected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    ap.ssid.ifEmpty { "<hidden>" },
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${ap.rssi} dBm",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "${ap.bssid} • ${ap.freqMhz} MHz • ${ap.channelWidth} MHz",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ap.isConnected) AssistChip(onClick = {}, label = { Text("CONNECTED") })
                if (ap.rttResponder) AssistChip(onClick = {}, label = { Text("802.11mc RTT") })
                if (ap.azResponder) AssistChip(onClick = {}, label = { Text("802.11az") })
                if (ap.laBit) AssistChip(onClick = {}, label = { Text("LA bit") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WifiSnapshotSheet(
    snapshot: WifiSnapshot,
    universitySsids: Set<String>,
    onSave: (checkpointLabel: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        WifiSnapshotContent(snapshot, universitySsids, onSave, onDismiss)
    }
}

@Composable
private fun WifiSnapshotContent(
    snapshot: WifiSnapshot,
    universitySsids: Set<String>,
    onSave: (checkpointLabel: String) -> Unit,
    onCancel: () -> Unit,
) {
    var label by remember { mutableStateOf("CP1") }
    val time = remember(snapshot.takenAtMs) {
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(snapshot.takenAtMs))
    }
    val saved = remember(snapshot, universitySsids) { snapshot.aps.filter { it.ssid in universitySsids } }
    val skipped = snapshot.aps.size - saved.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Save Wi-Fi snapshot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Scan as at $time. ${saved.size} university network(s) will be saved" +
                (if (skipped > 0) "; $skipped other network(s) are not saved." else "."),
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Checkpoint label (e.g. CP3)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = { onSave(label.trim().ifEmpty { "CP" }) }, enabled = saved.isNotEmpty()) {
                Text("Append CSV")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(saved, key = { "${it.bssid}_${it.freqMhz}" }) { ap -> WifiApCard(ap) }
        }
    }
}

private val previewBeacons = listOf(
    BeaconInventoryItem(
        key = "ed_uid|a08c", frameType = BeaconFrameType.ED_UID,
        canonicalId = "ed:8b9cc73c3ae747ef65bc:00000001a08c", projectLabel = null,
        addr = "00:00:5E:00:53:01", currentRssi = -58, medianRssi = -60, packetsPerSec = 0.9,
        txPowerAdv = -40, tlmBattMv = null, tlmTempC = null, advHex = "", lastSeenMs = 0L,
    ),
    BeaconInventoryItem(
        key = "ed_tlm|a08c", frameType = BeaconFrameType.ED_TLM,
        canonicalId = "ed:8b9cc73c3ae747ef65bc:00000001a08c", projectLabel = null,
        addr = "00:00:5E:00:53:01", currentRssi = -61, medianRssi = -61, packetsPerSec = 0.1,
        txPowerAdv = null, tlmBattMv = 3300, tlmTempC = 21.5, advHex = "", lastSeenMs = 0L,
    ),
    BeaconInventoryItem(
        key = "ib|b16", frameType = BeaconFrameType.IBEACON,
        canonicalId = "ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:16", projectLabel = "B16",
        addr = "00:00:5E:00:53:02", currentRssi = -72, medianRssi = -70, packetsPerSec = 3.1,
        txPowerAdv = -59, tlmBattMv = null, tlmTempC = null, advHex = "", lastSeenMs = 0L,
    ),
)

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun BeaconSnapshotContentPreview() {
    LabTheme {
        Surface {
            BeaconSnapshotContent(
                snapshot = BeaconSnapshot(previewBeacons, 1_795_220_000_000L),
                onSave = {},
                onCancel = {},
            )
        }
    }
}

@Composable
private fun WifiSurveyTab(
    state: SurveyUiState,
    config: SetupConfig,
    manager: SurveyManager,
    onUpdateConfig: ((SetupConfig) -> SetupConfig) -> Unit,
) {
    // The scan results as they were when Snapshot was pressed; the live list keeps changing underneath.
    var frozenSnapshot by remember { mutableStateOf<WifiSnapshot?>(null) }
    var showSsidEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { manager.triggerWifiScanOnce() },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Scan")
            }
            OutlinedButton(
                onClick = { frozenSnapshot = WifiSnapshot(state.wifiAps, System.currentTimeMillis()) },
                enabled = state.wifiAps.isNotEmpty(),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Snapshot")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Wi-Fi RTT: ${if (state.wifiRttSupported) "SUPPORTED" else "Not supported"}",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.wifiRttSupported) LabStatusColors.Green else MaterialTheme.colorScheme.error,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto scan", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = state.wifiScanning,
                    onCheckedChange = { manager.toggleWifiContinuousScan(it) },
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "University APs > -75 dBm: ${state.universityStrongCount}",
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "SSIDs: ${config.universitySsids.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = { showSsidEditor = true }) { Text("Edit") }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.wifiAps, key = { "${it.bssid}_${it.freqMhz}" }) { ap -> WifiApCard(ap) }
        }
    }

    frozenSnapshot?.let { snap ->
        WifiSnapshotSheet(
            snapshot = snap,
            universitySsids = config.universitySsids.toSet(),
            onSave = { label ->
                manager.saveWifiSnapshot(label, snap.aps, snap.takenAtMs)
                frozenSnapshot = null
            },
            onDismiss = { frozenSnapshot = null },
        )
    }

    if (showSsidEditor) {
        var text by remember { mutableStateOf(config.universitySsids.joinToString("\n")) }
        AlertDialog(
            onDismissRequest = { showSsidEditor = false },
            title = { Text("University SSIDs (one per line)") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val list = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                    onUpdateConfig { it.copy(universitySsids = list.ifEmpty { listOf("eduroam") }) }
                    manager.refreshWifiResults()
                    showSsidEditor = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSsidEditor = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun RttSurveyTab(
    state: SurveyUiState,
    manager: SurveyManager,
) {
    var logToCsv by remember { mutableStateOf(true) }
    val responderCount = state.wifiAps.count { it.rttResponder }
    val azCount = state.wifiAps.count { it.azResponder }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Phone Wi-Fi RTT: ${if (state.wifiRttSupported) "Supported" else "Not supported"}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Last scan: ${state.wifiAps.size} access points • 802.11mc responders: $responderCount • 802.11az: $azCount",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (state.wifiAps.isNotEmpty() && responderCount == 0) {
            Text(
                "None of these access points supports ranging, so there is nothing to range to here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Log to CSV", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
                Switch(
                    checked = logToCsv,
                    onCheckedChange = { logToCsv = it },
                    enabled = !state.rttRangingActive,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { manager.triggerWifiScanOnce() }) {
                    Text("Scan APs")
                }
                Button(
                    onClick = {
                        manager.toggleRttRanging(!state.rttRangingActive, logToCsv)
                    },
                    enabled = state.wifiRttSupported,
                ) {
                    Text(if (state.rttRangingActive) "Stop RTT" else "Start 1 Hz RTT")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.rttResults, key = { it.bssid }) { item ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                item.ssid.ifEmpty { item.bssid },
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (item.distMeters != null && item.stdMeters != null) {
                                    "%.2f ± %.2f m".format(Locale.US, item.distMeters, item.stdMeters)
                                } else {
                                    "status=${item.status}"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            "${item.bssid} • RSSI ${item.rssi ?: "-"} dBm • ${item.successful}/${item.attempted} ok",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedApTab(
    state: SurveyUiState,
    manager: SurveyManager,
) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Connected-AP 5s CSV Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (state.connectedBssid.isNotEmpty()) "${state.connectedSsid} (${state.connectedBssid})"
                        else "Not connected or waiting",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = state.connectedApLogging,
                    onCheckedChange = { manager.toggleConnectedApLogging(it) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.connectedApHistory, key = { it.elapsedNs }) { row ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("${sdf.format(Date(row.timestampMs))} • ${row.ssid}", fontWeight = FontWeight.Medium)
                            Text("${row.bssid} • ${row.freqMhz} MHz", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Text("${row.rssi} dBm", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
private val previewAps = listOf(
    WifiSurveyAp("00:00:5e:00:53:10", "eduroam", -44, 2412, "20", laBit = false, rttResponder = false,
        isConnected = true, rawScanResult = WifiScanResult()),
    WifiSurveyAp("00:00:5e:00:53:11", "unimib", -52, 5200, "20", laBit = true, rttResponder = false,
        isConnected = false, rawScanResult = WifiScanResult()),
    WifiSurveyAp("00:00:5e:00:53:20", "home-network", -60, 2437, "20", laBit = false, rttResponder = false,
        isConnected = false, rawScanResult = WifiScanResult()),
)

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun WifiSnapshotContentPreview() {
    LabTheme {
        Surface {
            WifiSnapshotContent(
                snapshot = WifiSnapshot(previewAps, 1_795_220_000_000L),
                universitySsids = setOf("eduroam", "unimib"),
                onSave = {},
                onCancel = {},
            )
        }
    }
}
