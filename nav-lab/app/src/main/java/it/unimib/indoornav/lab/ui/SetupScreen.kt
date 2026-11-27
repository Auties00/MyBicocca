package it.unimib.indoornav.lab.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.data.DeviceCapabilities
import it.unimib.indoornav.lab.data.DeviceCapabilitySnapshot
import it.unimib.indoornav.lab.data.ReadinessStatus
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.data.WifiThrottleCheckState
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.TestIdCodec
import it.unimib.indoornav.lab.ui.theme.LabStatusColors
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    config: SetupConfig,
    readiness: ReadinessStatus,
    capabilities: DeviceCapabilitySnapshot,
    onUpdateConfig: ((SetupConfig) -> SetupConfig) -> Unit,
    onRequestPermissions: () -> Unit,
    onCheckWifiThrottle: () -> Unit,
    onRefreshReadiness: () -> Unit,
    onStartRecording: () -> Unit,
) {
    val context = LocalContext.current
    val template = ExperimentTemplate.fromCode(config.templateCode) ?: ExperimentTemplate.E5
    val generatedTestId = remember(template, config.templateFields) {
        TestIdCodec.build(template, config.templateFields)
    }

    var showStoreAllWarning by remember { mutableStateOf(false) }
    var showCapabilities by remember { mutableStateOf(false) }
    var showSsidEditor by remember { mutableStateOf(false) }

    var floorText by remember { mutableStateOf(config.floor.toString()) }
    LaunchedEffect(config.floor) {
        if (floorText.toIntOrNull() != config.floor) {
            floorText = config.floor.toString()
        }
    }
    var buildingCodeText by remember { mutableStateOf(config.buildingCode.toString()) }
    LaunchedEffect(config.buildingCode) {
        if (buildingCodeText.toIntOrNull() != config.buildingCode) {
            buildingCodeText = config.buildingCode.toString()
        }
    }

    LaunchedEffect(Unit) {
        onRefreshReadiness()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Recording Setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Operator & Phone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.operator,
                        onValueChange = { v ->
                            onUpdateConfig { old ->
                                val tmpl = ExperimentTemplate.fromCode(old.templateCode)
                                val nextFields = if (tmpl?.fields?.any { it.key == "walker" } == true) {
                                    old.templateFields.toMutableMap().apply { put("walker", v) }
                                } else {
                                    old.templateFields
                                }
                                old.copy(operator = v, templateFields = nextFields)
                            }
                        },
                        label = { Text("Walker") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = config.marker,
                        onValueChange = { v -> onUpdateConfig { it.copy(marker = v) } },
                        label = { Text("Marker") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.phoneLabel,
                        onValueChange = { v -> onUpdateConfig { it.copy(phoneLabel = v) } },
                        label = { Text("Phone ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = config.building,
                        onValueChange = { v -> onUpdateConfig { it.copy(building = v) } },
                        label = { Text("Building") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { v ->
                            floorText = v
                            v.toIntOrNull()?.let { f -> onUpdateConfig { it.copy(floor = f) } }
                        },
                        label = { Text("Floor") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(0.8f),
                    )
                }
                OutlinedTextField(
                    value = buildingCodeText,
                    onValueChange = { v ->
                        buildingCodeText = v
                        v.toIntOrNull()?.let { code -> onUpdateConfig { it.copy(buildingCode = code) } }
                    },
                    label = { Text("Building numeric code (beacon scheme, U14 = 14)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Mode & Experiment Template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Text("Recording mode", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    template.allowedRecordingModes.forEach { mode ->
                        FilterChip(
                            selected = config.recordingMode == mode,
                            onClick = { onUpdateConfig { it.copy(recordingMode = mode) } },
                            label = { Text(mode) },
                        )
                    }
                }

                if (!template.hasModeField()) {
                    Text("Carry mode", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExperimentTemplate.CARRY_MODES.forEach { carry ->
                            FilterChip(
                                selected = config.carryMode == carry,
                                onClick = {
                                    onUpdateConfig { it.copy(carryMode = carry) }
                                },
                                label = { Text(carry) },
                            )
                        }
                    }
                }

                Text("Template", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ExperimentTemplate.entries.forEach { tmpl ->
                        FilterChip(
                            selected = tmpl.code == template.code,
                            onClick = {
                                onUpdateConfig { old ->
                                    val defaults = tmpl.fields
                                        .filter { it.defaultValue.isNotEmpty() }
                                        .associate { it.key to it.defaultValue }
                                        .toMutableMap()
                                    if (old.operator.isNotBlank() && tmpl.fields.any { it.key == "walker" }) {
                                        defaults["walker"] = old.operator
                                    }
                                    val nextCarry = defaults["mode"]?.takeIf { it.isNotBlank() }
                                        ?: tmpl.defaultCarryMode
                                    old.copy(
                                        templateCode = tmpl.code,
                                        carryMode = nextCarry,
                                        recordingMode = tmpl.defaultRecordingMode,
                                        templateFields = defaults,
                                    )
                                }
                            },
                            label = { Text(tmpl.code) },
                        )
                    }
                }

                Text(template.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                val isE10Rtt = template == ExperimentTemplate.E10 &&
                    (config.templateFields["cp"] ?: "").equals("rtt", ignoreCase = true)

                template.fields.forEach { field ->
                    val skipField = template == ExperimentTemplate.E10 &&
                        ((isE10Rtt && field.key == "face") || (!isE10Rtt && field.key == "d"))
                    if (!skipField) {
                        val currentVal = config.templateFields[field.key] ?: ""
                        fun applyFieldValue(v: String, fromSuggestionChip: Boolean = false) {
                            onUpdateConfig { old ->
                                val tmpl = ExperimentTemplate.fromCode(old.templateCode) ?: template
                                val nextMap = old.templateFields.toMutableMap()
                                nextMap[field.key] = v
                                if (tmpl == ExperimentTemplate.E10 && field.key == "cp") {
                                    if (v.equals("rtt", ignoreCase = true)) {
                                        nextMap["face"] = ""
                                        if (nextMap["d"].isNullOrBlank()) nextMap["d"] = "5"
                                    } else {
                                        nextMap["d"] = ""
                                        if (nextMap["face"].isNullOrBlank()) nextMap["face"] = "pos"
                                    }
                                }
                                if (fromSuggestionChip && field.key == "route") {
                                    if (tmpl.fields.any { it.key == "dir" }) {
                                        nextMap["dir"] = if (v.endsWith("r")) "back" else "out"
                                    }
                                    if (tmpl == ExperimentTemplate.E8) {
                                        nextMap["mode"] = when {
                                            v.startsWith("S") -> "stairs"
                                            v.startsWith("L") -> "lift"
                                            v.equals("still", ignoreCase = true) -> "still"
                                            else -> nextMap["mode"] ?: tmpl.defaultCarryMode
                                        }
                                    }
                                }
                                val nextOperator = if (field.key == "walker") v else old.operator
                                val nextCarry = if (tmpl.hasModeField()) {
                                    nextMap["mode"]?.trim()?.takeIf { it.isNotEmpty() } ?: tmpl.defaultCarryMode
                                } else {
                                    old.carryMode
                                }
                                old.copy(
                                    operator = nextOperator,
                                    carryMode = nextCarry,
                                    templateFields = nextMap,
                                )
                            }
                        }
                        OutlinedTextField(
                            value = currentVal,
                            onValueChange = { v -> applyFieldValue(v, fromSuggestionChip = false) },
                            label = {
                                Text(if (field.afterWalk) "${field.label} (asked after walk)" else field.label)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (field.suggestions.isNotEmpty()) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                field.suggestions.forEach { s ->
                                    FilterChip(
                                        selected = currentVal == s,
                                        onClick = { applyFieldValue(s, fromSuggestionChip = true) },
                                        label = { Text(s) },
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Generated test ID", style = MaterialTheme.typography.labelSmall)
                        Text(
                            generatedTestId,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                OutlinedTextField(
                    value = config.notes,
                    onValueChange = { v -> onUpdateConfig { it.copy(notes = v) } },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow screen off (E4b)", style = MaterialTheme.typography.bodyMedium)
                        Text("Releases keep-screen-on & dimming", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = config.allowScreenOff,
                        onCheckedChange = { checked -> onUpdateConfig { it.copy(allowScreenOff = checked) } },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BLE inventory mode (unfiltered)", style = MaterialTheme.typography.bodyMedium)
                        Text("Only with screen on; counts other devices", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = config.bleInventoryMode,
                        onCheckedChange = { checked ->
                            onUpdateConfig {
                                it.copy(
                                    bleInventoryMode = checked,
                                    bleStoreEverything = if (!checked) false else it.bleStoreEverything,
                                )
                            }
                        },
                    )
                }

                if (config.bleInventoryMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Store everything (diagnostic)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            Text("Stores non-beacon devices too", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = config.bleStoreEverything,
                            onCheckedChange = { checked ->
                                if (checked) showStoreAllWarning = true
                                else onUpdateConfig { it.copy(bleStoreEverything = false) }
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "University SSIDs: ${config.universitySsids.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { showSsidEditor = true }) {
                        Text("Edit SSIDs")
                    }
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Readiness Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onRefreshReadiness) { Text("Refresh") }
                }

                ReadinessRow(
                    ok = readiness.permissionsGranted,
                    label = if (readiness.permissionsGranted) "All runtime permissions granted"
                    else "${readiness.missingPermissions.size} permission(s) missing",
                    actionLabel = if (!readiness.permissionsGranted) "Grant" else null,
                    onAction = onRequestPermissions,
                )
                ReadinessRow(
                    ok = readiness.bluetoothOn,
                    label = if (readiness.bluetoothOn) "Bluetooth ON" else "Bluetooth OFF",
                    actionLabel = if (!readiness.bluetoothOn) "Enable" else null,
                    onAction = {
                        runCatching { context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
                    },
                )
                ReadinessRow(
                    ok = readiness.locationOn,
                    label = if (readiness.locationOn) "Location ON" else "Location OFF",
                    actionLabel = if (!readiness.locationOn) "Settings" else null,
                    onAction = {
                        runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    },
                )
                ReadinessRow(
                    ok = readiness.wifiThrottleState == WifiThrottleCheckState.OFF,
                    isWarning = readiness.wifiThrottleState == WifiThrottleCheckState.UNTESTED,
                    label = "Wi-Fi throttling: ${readiness.wifiThrottleDetail}",
                    actionLabel = "Test",
                    onAction = onCheckWifiThrottle,
                )
                ReadinessRow(
                    ok = readiness.batteryOptimizationExempt,
                    label = if (readiness.batteryOptimizationExempt) "Battery optimization exempt"
                    else "Battery optimization active",
                    actionLabel = if (!readiness.batteryOptimizationExempt) "Exempt" else null,
                    onAction = {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}"),
                        )
                        runCatching { context.startActivity(intent) }
                    },
                )
                ReadinessRow(
                    ok = readiness.batteryPercent >= 30,
                    label = "Battery: ${readiness.batteryPercent}% (need ≥ 30%)",
                )
                ReadinessRow(
                    ok = readiness.freeStorageGb >= 1.0,
                    label = "Free storage: %.2f GB (need ≥ 1.0 GB)".format(Locale.US, readiness.freeStorageGb),
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Phone Capabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showCapabilities = !showCapabilities }) {
                        Text(if (showCapabilities) "Hide" else "Show")
                        Icon(
                            if (showCapabilities) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    }
                }
                Text(
                    "Baro: ${capabilities.capabilities.barometer} • Gyro: ${capabilities.capabilities.gyroscope} • " +
                        "RTT: ${capabilities.capabilities.wifi_rtt} • ARCore: ${capabilities.capabilities.arcore}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (showCapabilities) {
                    Spacer(Modifier.height(8.dp))
                    capabilities.sensors.forEach { s ->
                        val rateText = if (s.present && s.requestedPeriodUs > 0) {
                            "${s.requestedPeriodUs} µs (%.0f Hz), min ${s.minDelayUs} µs".format(Locale.US, s.maxEffectiveHz)
                        } else if (s.present) {
                            "on-event"
                        } else {
                            "absent"
                        }
                        Text(
                            "${s.manifestType}: ${if (s.present) s.name else "ABSENT"} ($rateText)",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!readiness.permissionsGranted) {
                    onRequestPermissions()
                } else {
                    onStartRecording()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (readiness.permissionsGranted) "START RECORDING (${config.recordingMode.uppercase()})"
                else "GRANT PERMISSIONS FIRST",
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showStoreAllWarning) {
        AlertDialog(
            onDismissRequest = { showStoreAllWarning = false },
            title = { Text("Privacy Warning") },
            text = {
                Text("Enabling 'Store everything' logs raw BLE advertisements from nearby personal devices (phones, headphones, trackers). Use only for E0 troubleshooting and delete afterwards.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showStoreAllWarning = false
                    onUpdateConfig { it.copy(bleStoreEverything = true) }
                }) { Text("Enable") }
            },
            dismissButton = {
                TextButton(onClick = { showStoreAllWarning = false }) { Text("Cancel") }
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
private fun ReadinessRow(
    ok: Boolean,
    label: String,
    isWarning: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = when {
                ok -> Icons.Default.CheckCircle
                isWarning -> Icons.Default.Warning
                else -> Icons.Default.Error
            },
            contentDescription = null,
            tint = when {
                ok -> LabStatusColors.Green
                isWarning -> LabStatusColors.Amber
                else -> LabStatusColors.Red
            },
        )
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
