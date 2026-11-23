package it.unimib.indoornav.lab.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.data.SessionRepository
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.inventory.BeaconGrouping
import it.unimib.indoornav.lab.inventory.BeaconInventoryRepository
import it.unimib.indoornav.lab.inventory.BeaconViewMode
import it.unimib.indoornav.lab.inventory.InventorySortOption
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.inventory.SavedBeaconEntry
import it.unimib.indoornav.lab.survey.SurveyManager
import it.unimib.indoornav.lab.survey.SurveyUiState
import it.unimib.indoornav.lab.ui.beacons.BeaconCardGrid
import it.unimib.indoornav.lab.ui.beacons.BeaconCardList
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailHost
import it.unimib.indoornav.lab.ui.beacons.BeaconDetailOverlay
import it.unimib.indoornav.lab.ui.beacons.SaveBeaconBottomSheet
import it.unimib.indoornav.lab.ui.components.SegmentedSwitch
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen(
    surveyState: SurveyUiState,
    config: SetupConfig,
    isRecording: Boolean,
    surveyManager: SurveyManager,
    inventoryRepository: BeaconInventoryRepository,
    sessionRepository: SessionRepository,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedEntries by inventoryRepository.entries.collectAsState()
    val latestIsRecording by rememberUpdatedState(isRecording)

    LaunchedEffect(isRecording) {
        surveyManager.setInventoryTabActive(!isRecording, isRecording)
    }

    DisposableEffect(Unit) {
        onDispose {
            surveyManager.setInventoryTabActive(false, latestIsRecording)
        }
    }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000L)
            nowMs = System.currentTimeMillis()
        }
    }

    val liveGrouped = remember(surveyState.allBeacons) {
        BeaconGrouping.groupByPhysicalBeacon(surveyState.allBeacons)
    }
    val resolvedTiles = remember(savedEntries, liveGrouped, nowMs) {
        BeaconGrouping.resolveInventoryTiles(savedEntries, liveGrouped, nowMs)
    }

    var viewMode by remember { mutableStateOf(BeaconViewMode.LIST) }
    var sortOption by remember { mutableStateOf(InventorySortOption.AVAILABILITY_THEN_LABEL) }
    var detailTileKey by remember { mutableStateOf<String?>(null) }
    var editingTile by remember { mutableStateOf<ResolvedBeaconTile?>(null) }

    val sortedTiles = remember(resolvedTiles, sortOption) {
        BeaconGrouping.sortInventoryTiles(resolvedTiles, sortOption)
    }
    // Keeps the detail drawable while it animates out after its beacon was removed.
    val lastKnownTiles = remember { mutableMapOf<String, ResolvedBeaconTile>() }
    remember(sortedTiles) { sortedTiles.forEach { lastKnownTiles[it.tileKey] = it } }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    BeaconDetailHost(
        detailKey = detailTileKey,
        onClose = { detailTileKey = null },
        list = {
            InventoryContent(
                tiles = sortedTiles,
                viewMode = viewMode,
                sortOption = sortOption,
                isRecording = isRecording,
                scanPending = surveyState.beaconScanPending,
                statusMessage = surveyState.statusMessage,
                onViewModeChange = { viewMode = it },
                onSortOptionChange = { sortOption = it },
                onExportCsv = {
                    scope.launch {
                        val file = inventoryRepository.exportCatalogCsv(sessionRepository.surveyDir())
                        Toast.makeText(context, "Exported ${file.name}", Toast.LENGTH_SHORT).show()
                    }
                },
                onSelectTile = { detailTileKey = it.tileKey },
                listState = listState,
                gridState = gridState,
            )
        },
        detail = { key ->
            val tile = sortedTiles.firstOrNull { it.tileKey == key } ?: lastKnownTiles[key]
            if (tile != null) {
                BeaconDetailOverlay(
                    tile = tile,
                    onClose = { detailTileKey = null },
                    onEdit = { editingTile = tile },
                    onRemove = {
                        detailTileKey = null
                        tile.savedEntry?.let { inventoryRepository.remove(it.id) }
                    },
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
                detailTileKey = null
                inventoryRepository.remove(id)
                editingTile = null
            },
            onDismiss = { editingTile = null },
        )
    }
}

@Composable
private fun InventoryContent(
    tiles: List<ResolvedBeaconTile>,
    viewMode: BeaconViewMode,
    sortOption: InventorySortOption,
    isRecording: Boolean,
    scanPending: Boolean,
    statusMessage: String?,
    onViewModeChange: (BeaconViewMode) -> Unit,
    onSortOptionChange: (InventorySortOption) -> Unit,
    onExportCsv: () -> Unit,
    onSelectTile: (ResolvedBeaconTile) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val onlineCount = tiles.count { it.isOnline }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Beacon Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$onlineCount online · ${tiles.size} saved",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = onExportCsv,
                enabled = tiles.isNotEmpty(),
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export CSV", fontSize = 12.sp)
            }
        }

        if (isRecording) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "BLE inventory scan paused while recording is running.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        } else if (scanPending) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Waiting a few seconds before restarting BLE scan...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        } else if (!statusMessage.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        SegmentedSwitch(
            options = BeaconViewMode.entries,
            selected = viewMode,
            onSelected = onViewModeChange,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                AssistChip(
                    onClick = { sortMenuExpanded = true },
                    label = { Text("Sort: ${sortOption.label}", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    InventorySortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = {
                                sortMenuExpanded = false
                                onSortOptionChange(option)
                            },
                        )
                    }
                }
            }
            Text(
                text = "Offline after 20 s",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (tiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "No saved beacons yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Open Survey → Beacons, start scanning, open a beacon and choose Save to inventory in its ⋮ menu.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else if (viewMode == BeaconViewMode.LIST) {
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
                        onClick = { onSelectTile(tile) },
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
                        onClick = { onSelectTile(tile) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun InventoryContentPreview() {
    val sampleEntry = SavedBeaconEntry(
        id = "1",
        canonicalKey = "ed:515b86185cea0ec01b22:0000000e0007",
        macAddress = "D9:84:CE:34:5C:5C",
        label = "B07",
        alphanumericCode = "K4M2",
        building = "U14",
        floor = "Floor 1",
        positionDescription = "Near Lab 102 door",
        seenFrameTypes = setOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM),
        lastRssi = -54,
        lastMedianRssi = -56,
        lastTxPowerAdv = -40,
        lastSeenMs = 1_795_220_000_000L,
    )
    val sampleTiles = listOf(
        ResolvedBeaconTile(
            tileKey = "1",
            savedEntry = sampleEntry,
            liveBeacon = null,
            isOnline = true,
            displayLabel = "B07",
            alphanumericCode = "K4M2",
            building = "U14",
            floor = "Floor 1",
            positionDescription = "Near Lab 102 door",
            macAddress = "D9:84:CE:34:5C:5C",
            canonicalKey = sampleEntry.canonicalKey,
            frameTypes = listOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM),
            currentRssi = -54,
            medianRssi = -56,
            packetsPerSec = 4.2,
            txPowerAdv = -40,
            tlmBattMv = 3120,
            tlmTempC = 22.4,
            tlmAdvCount = null,
            tlmUptimeSec = null,
            estimatedIntervalByFrame = emptyMap(),
            rawAdvHexByFrame = emptyMap(),
            lastSeenMs = 1_795_220_000_000L,
        ),
        ResolvedBeaconTile(
            tileKey = "2",
            savedEntry = sampleEntry.copy(id = "2", label = "B12", alphanumericCode = "A9P3"),
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
    )
    LabTheme {
        Surface {
            InventoryContent(
                tiles = sampleTiles,
                viewMode = BeaconViewMode.LIST,
                sortOption = InventorySortOption.AVAILABILITY_THEN_LABEL,
                isRecording = false,
                scanPending = false,
                statusMessage = null,
                onViewModeChange = {},
                onSortOptionChange = {},
                onExportCsv = {},
                onSelectTile = {},
                listState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
            )
        }
    }
}
