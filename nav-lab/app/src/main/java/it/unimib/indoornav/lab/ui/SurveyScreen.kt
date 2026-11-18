package it.unimib.indoornav.lab.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.survey.BeaconInventoryItem
import it.unimib.indoornav.lab.survey.SurveyManager
import it.unimib.indoornav.lab.survey.SurveyUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SurveyScreen(
    surveyState: SurveyUiState,
    config: SetupConfig,
    surveyManager: SurveyManager,
    onUpdateConfig: ((SetupConfig) -> SetupConfig) -> Unit,
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
            0 -> BeaconInventoryTab(surveyState, surveyManager)
            1 -> WifiSurveyTab(surveyState, config, surveyManager, onUpdateConfig)
            2 -> RttSurveyTab(surveyState, surveyManager)
            3 -> ConnectedApTab(surveyState, surveyManager)
        }
    }
}

@Composable
private fun BeaconInventoryTab(
    state: SurveyUiState,
    manager: SurveyManager,
) {
    var selectedBeacon by remember { mutableStateOf<BeaconInventoryItem?>(null) }
    var showSnapshotDialog by remember { mutableStateOf(false) }

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
                Text(if (state.beaconScanning) "Stop Scan" else "Start Inventory")
            }
            OutlinedButton(
                onClick = { showSnapshotDialog = true },
                enabled = state.beacons.isNotEmpty(),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Snapshot")
            }
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Min RSSI: ${state.minRssiFilter} dBm", style = MaterialTheme.typography.labelLarge)
                Text(
                    "Beacons: ${state.beacons.size} | Other BLE/s: ${state.otherBlePerSec}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Slider(
                value = state.minRssiFilter.toFloat(),
                onValueChange = { manager.setMinRssiFilter(it.toInt()) },
                valueRange = -100f..-30f,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.beacons, key = { it.key }) { item ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedBeacon = item },
                ) {
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
        }
    }

    selectedBeacon?.let { b ->
        AlertDialog(
            onDismissRequest = { selectedBeacon = null },
            title = { Text(b.projectLabel ?: b.frameType.wireName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Frame: ${b.frameType.wireName}")
                    Text("Canonical ID: ${b.canonicalId.ifEmpty { "(TLM without prior UID)" }}")
                    Text("Address: ${b.addr}")
                    Text("RSSI: ${b.currentRssi} dBm (median ${b.medianRssi} dBm)")
                    Text("Rate: %.2f packets/s".format(Locale.US, b.packetsPerSec))
                    Text("Tx Power: ${b.txPowerAdv ?: "n/a"}")
                    Text("TLM Battery: ${b.tlmBattMv?.let { "$it mV" } ?: "n/a"}")
                    Text("TLM Temp: ${b.tlmTempC?.let { "%.2f °C".format(Locale.US, it) } ?: "n/a"}")
                    Text("Adv hex: ${b.advHex}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBeacon = null }) { Text("Close") }
            },
        )
    }

    if (showSnapshotDialog) {
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            title = { Text("Save Beacon Snapshot") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Location note (e.g. U14 F1 CP3)") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    manager.saveBeaconSnapshot(note.trim())
                    showSnapshotDialog = false
                }) { Text("Save CSV") }
            },
            dismissButton = {
                TextButton(onClick = { showSnapshotDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun WifiSurveyTab(
    state: SurveyUiState,
    config: SetupConfig,
    manager: SurveyManager,
    onUpdateConfig: ((SetupConfig) -> SetupConfig) -> Unit,
) {
    var showSnapshotDialog by remember { mutableStateOf(false) }
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
            OutlinedButton(onClick = { showSnapshotDialog = true }, enabled = state.wifiAps.isNotEmpty()) {
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
                color = if (state.wifiRttSupported) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
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
            items(state.wifiAps, key = { "${it.bssid}_${it.freqMhz}" }) { ap ->
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
                            if (ap.laBit) AssistChip(onClick = {}, label = { Text("LA bit") })
                        }
                    }
                }
            }
        }
    }

    if (showSnapshotDialog) {
        var cp by remember { mutableStateOf("CP1") }
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            title = { Text("Save Wi-Fi Snapshot") },
            text = {
                OutlinedTextField(
                    value = cp,
                    onValueChange = { cp = it },
                    label = { Text("Checkpoint label (e.g. CP3)") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    manager.saveWifiSnapshot(cp.trim().ifEmpty { "CP" })
                    showSnapshotDialog = false
                }) { Text("Append CSV") }
            },
            dismissButton = {
                TextButton(onClick = { showSnapshotDialog = false }) { Text("Cancel") }
            },
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Phone Wi-Fi RTT: ${if (state.wifiRttSupported) "Supported" else "Not supported"} • Responders in scan: $responderCount",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

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
