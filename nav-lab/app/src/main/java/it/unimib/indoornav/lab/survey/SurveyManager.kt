package it.unimib.indoornav.lab.survey

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.capture.BleCapture
import it.unimib.indoornav.lab.capture.WifiCapture
import it.unimib.indoornav.lab.data.LabPreferences
import it.unimib.indoornav.lab.data.SessionRepository
import it.unimib.indoornav.lab.data.WifiThrottleCheckState
import it.unimib.indoornav.lab.format.BeaconFrameParser
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.format.CsvCodec
import it.unimib.indoornav.lab.format.ProjectConstants
import it.unimib.indoornav.lab.format.StreamSpec
import it.unimib.indoornav.lab.format.SurveyCsvHeaders
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import android.net.wifi.ScanResult as WifiScanResult

data class BeaconInventoryItem(
    val key: String,
    val frameType: BeaconFrameType,
    val canonicalId: String,
    val projectLabel: String?,
    val addr: String,
    val currentRssi: Int,
    val medianRssi: Int,
    val packetsPerSec: Double,
    val txPowerAdv: Int?,
    val tlmBattMv: Int?,
    val tlmTempC: Double?,
    val advHex: String,
    val lastSeenMs: Long,
)

data class WifiSurveyAp(
    val bssid: String,
    val ssid: String,
    val rssi: Int,
    val freqMhz: Int,
    val channelWidth: String,
    val laBit: Boolean,
    val rttResponder: Boolean,
    val azResponder: Boolean = false,
    val isConnected: Boolean,
    val rawScanResult: WifiScanResult,
)

data class RttSurveyEntry(
    val bssid: String,
    val ssid: String,
    val status: Int,
    val distMeters: Double?,
    val stdMeters: Double?,
    val rssi: Int?,
    val attempted: Int,
    val successful: Int,
    val timestampMs: Long,
)

data class ConnectedApEntry(
    val timestampMs: Long,
    val elapsedNs: Long,
    val bssid: String,
    val ssid: String,
    val rssi: Int,
    val freqMhz: Int,
)

data class SurveyUiState(
    val beaconScanning: Boolean = false,
    val minRssiFilter: Int = -70,
    val beacons: List<BeaconInventoryItem> = emptyList(),
    val otherBlePerSec: Int = 0,
    val wifiScanning: Boolean = false,
    val wifiRttSupported: Boolean = false,
    val wifiAps: List<WifiSurveyAp> = emptyList(),
    val connectedBssid: String = "",
    val connectedSsid: String = "",
    val universityStrongCount: Int = 0,
    val rttRangingActive: Boolean = false,
    val rttLogging: Boolean = false,
    val rttResults: List<RttSurveyEntry> = emptyList(),
    val connectedApLogging: Boolean = false,
    val connectedApHistory: List<ConnectedApEntry> = emptyList(),
    val statusMessage: String? = null,
)

class SurveyManager(
    private val context: Context,
    private val preferences: LabPreferences,
    private val sessionRepository: SessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mainExecutor = Executor { r -> mainHandler.post(r) }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val rttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _state = MutableStateFlow(
        SurveyUiState(
            wifiRttSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT),
        ),
    )
    val state: StateFlow<SurveyUiState> = _state.asStateFlow()

    private class MutableBeaconTracker(
        val key: String,
        var frameType: BeaconFrameType,
        var canonicalId: String,
        var addr: String,
        var txPowerAdv: Int?,
        var tlmBattMv: Int?,
        var tlmTempC: Double?,
        var advHex: String,
    ) {
        var currentRssi: Int = -100
        var lastSeenMs: Long = 0L
        val rssiSamples = ArrayDeque<Int>()
        val packetTimestampsMs = ArrayDeque<Long>()

        fun addRssi(rssi: Int, nowMs: Long) {
            lastSeenMs = nowMs
            // The packet still counts towards the rate; its RSSI of 127 means "not available".
            if (rssi != BleCapture.RSSI_UNAVAILABLE) {
                currentRssi = rssi
                rssiSamples.addLast(rssi)
                if (rssiSamples.size > 31) rssiSamples.removeFirst()
            }
            packetTimestampsMs.addLast(nowMs)
            trimWindow(nowMs)
        }

        fun trimWindow(nowMs: Long) {
            while (packetTimestampsMs.isNotEmpty() && nowMs - packetTimestampsMs.first() > 3_000L) {
                packetTimestampsMs.removeFirst()
            }
        }

        fun medianRssi(): Int {
            if (rssiSamples.isEmpty()) return currentRssi
            val sorted = rssiSamples.sorted()
            return sorted[sorted.size / 2]
        }

        fun pps(nowMs: Long): Double {
            trimWindow(nowMs)
            return packetTimestampsMs.size / 3.0
        }
    }

    private val beaconTrackers = ConcurrentHashMap<String, MutableBeaconTracker>()
    private val lastUidByAddr = ConcurrentHashMap<String, String>()
    private val tlmByAddr = ConcurrentHashMap<String, Pair<Int?, Double?>>()
    private var otherBleCounter = 0

    private var bleScanner: BluetoothLeScanner? = null
    private var beaconRefreshJob: Job? = null
    private var wifiAutoJob: Job? = null
    private var rttJob: Job? = null
    private var connectedApJob: Job? = null
    private var throttleJob: Job? = null

    private val surveyIoExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "LabSurveyCsvIo")
    }
    private var rttLogWriter: BufferedWriter? = null
    private var connectedApWriter: BufferedWriter? = null
    private var wifiReceiverRegistered = false

    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            mainHandler.post {
                if (_state.value.beaconScanning) processBleResult(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            val snapshot = results.toList()
            mainHandler.post {
                if (_state.value.beaconScanning) {
                    for (r in snapshot) processBleResult(r)
                }
            }
        }
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                refreshWifiResults()
            }
        }
    }

    fun setMinRssiFilter(rssi: Int) {
        _state.value = _state.value.copy(minRssiFilter = rssi)
        publishBeaconSnapshot()
    }

    @SuppressLint("MissingPermission")
    fun startBeaconInventory() {
        if (_state.value.beaconScanning) return
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !adapter.isEnabled || !hasBlePermission()) {
            setStatus("Bluetooth off or permission missing")
            return
        }
        val scanner = adapter.bluetoothLeScanner ?: run {
            setStatus("BLE scanner unavailable")
            return
        }
        bleScanner = scanner
        beaconTrackers.clear()
        lastUidByAddr.clear()
        tlmByAddr.clear()
        otherBleCounter = 0

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        runCatching {
            scanner.startScan(emptyList(), settings, bleScanCallback)
            _state.value = _state.value.copy(beaconScanning = true, statusMessage = null)
        }.onFailure {
            setStatus("Failed to start BLE inventory: ${it.message}")
            return
        }

        beaconRefreshJob?.cancel()
        beaconRefreshJob = scope.launch {
            while (_state.value.beaconScanning) {
                publishBeaconSnapshot()
                otherBleCounter = 0
                delay(1_000L)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBeaconInventory() {
        if (!_state.value.beaconScanning) return
        beaconRefreshJob?.cancel()
        beaconRefreshJob = null
        runCatching { bleScanner?.stopScan(bleScanCallback) }
        _state.value = _state.value.copy(beaconScanning = false)
    }

    private fun processBleResult(result: ScanResult) {
        val rawBytes = result.scanRecord?.bytes ?: ByteArray(0)
        val parsed = BeaconFrameParser.parseRawAdvertisement(rawBytes)
        if (parsed == null) {
            otherBleCounter++
            return
        }

        val addr = result.device?.address ?: ""
        val nowMs = System.currentTimeMillis()
        when (parsed.frameType) {
            BeaconFrameType.ED_UID -> {
                val id = parsed.beaconId ?: return
                if (addr.isNotEmpty()) {
                    lastUidByAddr[addr] = id
                    beaconTrackers["${BeaconFrameType.ED_TLM.wireName}:$addr"]?.canonicalId = id
                }
                val tlm = tlmByAddr[addr]
                val key = "${parsed.frameType.wireName}:$id:$addr"
                val tracker = beaconTrackers.getOrPut(key) {
                    MutableBeaconTracker(
                        key = key,
                        frameType = parsed.frameType,
                        canonicalId = id,
                        addr = addr,
                        txPowerAdv = parsed.txPowerAdv,
                        tlmBattMv = tlm?.first,
                        tlmTempC = tlm?.second,
                        advHex = parsed.advHex,
                    )
                }
                tracker.txPowerAdv = parsed.txPowerAdv
                tracker.advHex = parsed.advHex
                if (tlm != null) {
                    tracker.tlmBattMv = tlm.first
                    tracker.tlmTempC = tlm.second
                }
                tracker.addRssi(result.rssi, nowMs)
            }
            BeaconFrameType.ED_TLM -> {
                if (addr.isNotEmpty()) {
                    tlmByAddr[addr] = parsed.tlmBattMv to parsed.tlmTempC
                }
                val id = lastUidByAddr[addr] ?: ""
                val key = "${parsed.frameType.wireName}:$addr"
                val tracker = beaconTrackers.getOrPut(key) {
                    MutableBeaconTracker(
                        key = key,
                        frameType = parsed.frameType,
                        canonicalId = id,
                        addr = addr,
                        txPowerAdv = null,
                        tlmBattMv = parsed.tlmBattMv,
                        tlmTempC = parsed.tlmTempC,
                        advHex = parsed.advHex,
                    )
                }
                tracker.canonicalId = id
                tracker.tlmBattMv = parsed.tlmBattMv
                tracker.tlmTempC = parsed.tlmTempC
                tracker.advHex = parsed.advHex
                tracker.addRssi(result.rssi, nowMs)

                beaconTrackers.values.filter { it.addr == addr && it.frameType == BeaconFrameType.ED_UID }.forEach {
                    it.tlmBattMv = parsed.tlmBattMv
                    it.tlmTempC = parsed.tlmTempC
                }
            }
            BeaconFrameType.IBEACON, BeaconFrameType.ALTBEACON, BeaconFrameType.VENDOR -> {
                val id = parsed.beaconId ?: ""
                val key = "${parsed.frameType.wireName}:$id:$addr"
                val tracker = beaconTrackers.getOrPut(key) {
                    MutableBeaconTracker(
                        key = key,
                        frameType = parsed.frameType,
                        canonicalId = id,
                        addr = addr,
                        txPowerAdv = parsed.txPowerAdv,
                        tlmBattMv = null,
                        tlmTempC = null,
                        advHex = parsed.advHex,
                    )
                }
                tracker.txPowerAdv = parsed.txPowerAdv
                tracker.advHex = parsed.advHex
                tracker.addRssi(result.rssi, nowMs)
            }
        }
    }

    private fun publishBeaconSnapshot() {
        val nowMs = System.currentTimeMillis()
        val minRssi = _state.value.minRssiFilter
        val buildingCode = preferences.config.value.buildingCode

        val list = beaconTrackers.values
            .filter { nowMs - it.lastSeenMs <= 30_000L }
            .filter { it.currentRssi >= minRssi }
            .map { t ->
                val label = if (t.canonicalId.isNotEmpty()) {
                    ProjectConstants.matchProjectLabel(t.canonicalId, buildingCode)
                } else {
                    null
                }
                BeaconInventoryItem(
                    key = t.key,
                    frameType = t.frameType,
                    canonicalId = t.canonicalId,
                    projectLabel = label,
                    addr = t.addr,
                    currentRssi = t.currentRssi,
                    medianRssi = t.medianRssi(),
                    packetsPerSec = t.pps(nowMs),
                    txPowerAdv = t.txPowerAdv,
                    tlmBattMv = t.tlmBattMv,
                    tlmTempC = t.tlmTempC,
                    advHex = t.advHex,
                    lastSeenMs = t.lastSeenMs,
                )
            }
            .sortedByDescending { it.currentRssi }

        _state.value = _state.value.copy(
            beacons = list,
            otherBlePerSec = otherBleCounter,
        )
    }

    /** Saves [beacons] as they were at [ts], which is when the user pressed Snapshot, not when they confirmed. */
    fun saveBeaconSnapshot(
        locationNote: String,
        beacons: List<BeaconInventoryItem> = _state.value.beacons,
        ts: Long = System.currentTimeMillis(),
    ) {
        scope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
            val file = File(sessionRepository.surveyDir(), "ble_inventory_${sdf.format(Date(ts))}.csv")
            val result = runCatching {
                file.bufferedWriter().use { bw ->
                    bw.write(SurveyCsvHeaders.BEACON_INVENTORY.joinToString(","))
                    bw.newLine()
                    for (b in beacons) {
                        bw.write(
                            CsvCodec.formatRow(
                                ts,
                                locationNote,
                                b.frameType.wireName,
                                b.canonicalId,
                                b.projectLabel ?: "",
                                b.addr,
                                b.currentRssi,
                                b.medianRssi,
                                "%.2f".format(Locale.US, b.packetsPerSec),
                                b.txPowerAdv ?: "",
                                b.tlmBattMv ?: "",
                                b.tlmTempC?.let { "%.2f".format(Locale.US, it) } ?: "",
                            ),
                        )
                        bw.newLine()
                    }
                }
                "Saved ${beacons.size} beacons to ${file.name}"
            }.getOrElse { "Save failed: ${it.message}" }
            withContext(Dispatchers.Main) { setStatus(result) }
        }
    }

    @Suppress("DEPRECATION")
    fun triggerWifiScanOnce() {
        if (!hasWifiPermission()) {
            setStatus("Missing Wi-Fi/Location permission")
            return
        }
        ensureWifiReceiver()
        refreshWifiResults()
        val ok = runCatching { wifiManager?.startScan() == true }.getOrDefault(false)
        if (!ok) {
            setStatus("Wi-Fi startScan() refused (check throttling toggle)")
        }
    }

    fun toggleWifiContinuousScan(enabled: Boolean) {
        if (enabled) {
            if (!hasWifiPermission()) {
                setStatus("Missing Wi-Fi/Location permission")
                return
            }
            ensureWifiReceiver()
            _state.value = _state.value.copy(wifiScanning = true)
            wifiAutoJob?.cancel()
            wifiAutoJob = scope.launch {
                while (_state.value.wifiScanning) {
                    triggerWifiScanOnce()
                    delay(2_000L)
                }
            }
        } else {
            wifiAutoJob?.cancel()
            wifiAutoJob = null
            _state.value = _state.value.copy(wifiScanning = false)
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshWifiResults() {
        val wm = wifiManager ?: return
        if (!hasWifiPermission()) return
        val results = runCatching { wm.scanResults }.getOrElse { emptyList() }
        val connected = readConnectedWifiInfo()
        val univSsids = preferences.config.value.universitySsids.toSet()

        val aps = results.map { sr ->
            val bssid = sr.BSSID ?: ""
            val ssid = WifiCapture.extractSsid(sr)
            WifiSurveyAp(
                bssid = bssid,
                ssid = ssid,
                rssi = sr.level,
                freqMhz = sr.frequency,
                channelWidth = WifiCapture.channelWidthLabel(sr.channelWidth),
                laBit = WifiCapture.isLocallyAdministered(bssid),
                rttResponder = sr.is80211mcResponder,
                azResponder = WifiCapture.isAzResponder(sr),
                isConnected = connected != null && connected.first.equals(bssid, ignoreCase = true),
                rawScanResult = sr,
            )
        }.sortedWith(compareByDescending<WifiSurveyAp> { it.isConnected }.thenByDescending { it.rssi })

        val strongUniv = aps.count { it.ssid in univSsids && it.rssi > -75 }

        _state.value = _state.value.copy(
            wifiAps = aps,
            connectedBssid = connected?.first ?: "",
            connectedSsid = connected?.second ?: "",
            universityStrongCount = strongUniv,
        )
    }

    fun saveWifiSnapshot(checkpointLabel: String) {
        // Only university networks are saved, as in the recording's Wi-Fi stream; the rest stay on screen only.
        val univSsids = preferences.config.value.universitySsids.toSet()
        val allAps = _state.value.wifiAps
        val aps = allAps.filter { it.ssid in univSsids }
        val skipped = allAps.size - aps.size
        val ts = System.currentTimeMillis()
        scope.launch(Dispatchers.IO) {
            val file = File(sessionRepository.surveyDir(), "wifi_survey.csv")
            val msg = runCatching {
                val needsHeader = !file.exists() || file.length() == 0L
                FileWriter(file, true).buffered().use { bw ->
                    if (needsHeader) {
                        bw.write(SurveyCsvHeaders.WIFI_SURVEY.joinToString(","))
                        bw.newLine()
                    }
                    for (ap in aps) {
                        bw.write(
                            CsvCodec.formatRow(
                                ts,
                                checkpointLabel,
                                ap.bssid,
                                ap.ssid,
                                ap.rssi,
                                ap.freqMhz,
                                ap.channelWidth,
                                if (ap.laBit) 1 else 0,
                                if (ap.rttResponder) 1 else 0,
                                if (ap.azResponder) 1 else 0,
                                if (ap.isConnected) 1 else 0,
                            ),
                        )
                        bw.newLine()
                    }
                }
                "Appended ${aps.size} university APs at $checkpointLabel to ${file.name} ($skipped other networks not saved)"
            }.getOrElse { "Save failed: ${it.message}" }
            withContext(Dispatchers.Main) { setStatus(msg) }
        }
    }

    fun toggleRttRanging(enabled: Boolean, logToCsv: Boolean) {
        if (!enabled) {
            rttJob?.cancel()
            rttJob = null
            closeRttWriter()
            _state.value = _state.value.copy(rttRangingActive = false, rttLogging = false)
            return
        }
        if (!_state.value.wifiRttSupported || rttManager == null) {
            setStatus("Wi-Fi RTT not supported on this phone")
            return
        }
        if (!hasWifiPermission()) {
            setStatus("Missing Wi-Fi/Location permission")
            return
        }
        if (_state.value.wifiAps.isEmpty()) refreshWifiResults()
        val aps = _state.value.wifiAps
        if (aps.none { it.rttResponder }) {
            // Ranging needs the access point's cooperation; without a responder there is nothing to range to.
            val az = aps.count { it.azResponder }
            setStatus(
                "No 802.11mc ranging responders among the ${aps.size} access points in the last scan" +
                    (if (az > 0) " ($az support 802.11az, which Lab does not range to yet)" else "") +
                    ". Press Scan APs to rescan.",
            )
            return
        }

        if (logToCsv) {
            openRttWriter()
        }
        _state.value = _state.value.copy(rttRangingActive = true, rttLogging = logToCsv)
        rttJob?.cancel()
        rttJob = scope.launch {
            while (_state.value.rttRangingActive) {
                if (_state.value.wifiAps.isEmpty()) {
                    refreshWifiResults()
                }
                performRttOnce()
                delay(1_000L)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun performRttOnce() {
        val mgr = rttManager ?: return
        if (!mgr.isAvailable) return
        val responders = _state.value.wifiAps.filter { it.rttResponder }
        if (responders.isEmpty()) return

        val maxPeers = minOf(10, RangingRequest.getMaxPeers())
        val targets = responders.take(maxPeers)
        val ssidByBssid = targets.associate { it.bssid.lowercase() to it.ssid }
        val request = RangingRequest.Builder()
            .addAccessPoints(targets.map { it.rawScanResult })
            .build()

        runCatching {
            mgr.startRanging(request, mainExecutor, object : RangingResultCallback() {
                override fun onRangingFailure(code: Int) {
                    setStatus("RTT ranging failed: $code")
                }

                override fun onRangingResults(results: MutableList<RangingResult>) {
                    val rxNs = SystemClock.elapsedRealtimeNanos()
                    val nowMs = System.currentTimeMillis()
                    val csvLines = if (_state.value.rttLogging) ArrayList<String>(results.size) else null
                    val entries = results.map { r ->
                        val bssid = r.macAddress?.toString() ?: ""
                        val ok = r.status == RangingResult.STATUS_SUCCESS
                        val tNs = if (ok) r.rangingTimestampMillis * 1_000_000L else rxNs
                        if (csvLines != null) {
                            val line = if (ok) {
                                CsvCodec.formatRow(
                                    tNs,
                                    rxNs,
                                    bssid,
                                    r.status,
                                    r.distanceMm,
                                    r.distanceStdDevMm,
                                    r.rssi,
                                    r.numAttemptedMeasurements,
                                    r.numSuccessfulMeasurements,
                                )
                            } else {
                                CsvCodec.formatRow(tNs, rxNs, bssid, r.status, "", "", "", "", "")
                            }
                            csvLines.add(line)
                        }
                        RttSurveyEntry(
                            bssid = bssid,
                            ssid = ssidByBssid[bssid.lowercase()] ?: "",
                            status = r.status,
                            distMeters = if (ok) r.distanceMm / 1000.0 else null,
                            stdMeters = if (ok) r.distanceStdDevMm / 1000.0 else null,
                            rssi = if (ok) r.rssi else null,
                            attempted = if (ok) r.numAttemptedMeasurements else 0,
                            successful = if (ok) r.numSuccessfulMeasurements else 0,
                            timestampMs = nowMs,
                        )
                    }
                    if (!csvLines.isNullOrEmpty()) {
                        surveyIoExecutor.execute {
                            val w = rttLogWriter ?: return@execute
                            runCatching {
                                for (line in csvLines) {
                                    w.write(line)
                                    w.newLine()
                                }
                                w.flush()
                            }
                        }
                    }
                    _state.value = _state.value.copy(rttResults = entries)
                }
            })
        }
    }

    fun toggleConnectedApLogging(enabled: Boolean) {
        if (!enabled) {
            connectedApJob?.cancel()
            connectedApJob = null
            surveyIoExecutor.execute {
                runCatching {
                    connectedApWriter?.flush()
                    connectedApWriter?.close()
                }
                connectedApWriter = null
            }
            _state.value = _state.value.copy(connectedApLogging = false)
            return
        }
        if (!hasWifiPermission()) {
            setStatus("Missing Wi-Fi/Location permission")
            return
        }

        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val fileName = "connected_ap_${sdf.format(Date())}.csv"
        surveyIoExecutor.execute {
            runCatching {
                connectedApWriter?.flush()
                connectedApWriter?.close()
            }
            val file = File(sessionRepository.surveyDir(), fileName)
            val err = runCatching {
                connectedApWriter = BufferedWriter(FileWriter(file, false)).apply {
                    write(SurveyCsvHeaders.CONNECTED_AP.joinToString(","))
                    newLine()
                    flush()
                }
                null
            }.exceptionOrNull()
            if (err != null) {
                connectedApWriter = null
                mainHandler.post {
                    _state.value = _state.value.copy(
                        connectedApLogging = false,
                        statusMessage = "Cannot open connected AP log: ${err.message}",
                    )
                }
            }
        }

        _state.value = _state.value.copy(connectedApLogging = true, statusMessage = "Logging to $fileName")
        connectedApJob?.cancel()
        connectedApJob = scope.launch {
            while (_state.value.connectedApLogging) {
                val info = readConnectedWifiFull()
                val nowMs = System.currentTimeMillis()
                val elapsedNs = SystemClock.elapsedRealtimeNanos()
                if (info != null) {
                    val entry = ConnectedApEntry(
                        timestampMs = nowMs,
                        elapsedNs = elapsedNs,
                        bssid = info.bssid,
                        ssid = info.ssid,
                        rssi = info.rssi,
                        freqMhz = info.freqMhz,
                    )
                    val line = CsvCodec.formatRow(nowMs, elapsedNs, info.bssid, info.ssid, info.rssi, info.freqMhz)
                    // A non-university connection, such as a home network, is shown but never written.
                    val isUniversity = info.ssid in preferences.config.value.universitySsids
                    if (isUniversity) surveyIoExecutor.execute {
                        val w = connectedApWriter ?: return@execute
                        runCatching {
                            w.write(line)
                            w.newLine()
                            w.flush()
                        }
                    }
                    val updated = (listOf(entry) + _state.value.connectedApHistory).take(30)
                    _state.value = _state.value.copy(
                        connectedBssid = info.bssid,
                        connectedSsid = info.ssid,
                        connectedApHistory = updated,
                    )
                }
                delay(5_000L)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun checkWifiThrottling(onResult: (WifiThrottleCheckState, String) -> Unit) {
        val wm = wifiManager
        if (wm == null || !hasWifiPermission()) {
            onResult(WifiThrottleCheckState.FAILED, "Wi-Fi or permission unavailable")
            return
        }
        if (!wm.isWifiEnabled) {
            onResult(WifiThrottleCheckState.FAILED, "Wi-Fi is disabled")
            return
        }
        throttleJob?.cancel()
        onResult(WifiThrottleCheckState.TESTING, "Running 5-scan throttling test...")
        throttleJob = scope.launch {
            var pendingScan: CompletableDeferred<Boolean>? = null
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (intent?.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return
                    val updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)
                    pendingScan?.complete(updated)
                }
            }
            context.registerReceiver(
                receiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                null,
                mainHandler,
            )
            try {
                for (scanIdx in 1..5) {
                    onResult(WifiThrottleCheckState.TESTING, "Running Wi-Fi scan $scanIdx/5...")
                    val deferred = CompletableDeferred<Boolean>()
                    pendingScan = deferred
                    val accepted = runCatching { wm.startScan() }.getOrDefault(false)
                    if (!accepted) {
                        onResult(
                            WifiThrottleCheckState.ON,
                            "Scan $scanIdx/5 refused — disable Wi-Fi scan throttling in Developer options",
                        )
                        return@launch
                    }
                    val updated = withTimeoutOrNull(10_000L) { deferred.await() }
                    pendingScan = null
                    if (updated == null) {
                        onResult(
                            WifiThrottleCheckState.FAILED,
                            "Scan $scanIdx/5 timed out waiting for scan results",
                        )
                        return@launch
                    }
                    if (!updated) {
                        onResult(
                            WifiThrottleCheckState.ON,
                            "Scan $scanIdx/5 throttled (stale results) — disable Wi-Fi scan throttling in Developer options",
                        )
                        return@launch
                    }
                    if (scanIdx < 5) {
                        delay(200L)
                    }
                }
                onResult(WifiThrottleCheckState.OFF, "5/5 consecutive scans accepted (throttling off)")
            } finally {
                pendingScan = null
                runCatching { context.unregisterReceiver(receiver) }
            }
        }
    }

    fun release() {
        throttleJob?.cancel()
        throttleJob = null
        stopBeaconInventory()
        toggleWifiContinuousScan(false)
        toggleRttRanging(false, false)
        toggleConnectedApLogging(false)
        if (wifiReceiverRegistered) {
            runCatching { context.unregisterReceiver(wifiReceiver) }
            wifiReceiverRegistered = false
        }
    }

    private fun openRttWriter() {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val fileName = "rtt_survey_${sdf.format(Date())}.csv"
        surveyIoExecutor.execute {
            runCatching {
                rttLogWriter?.flush()
                rttLogWriter?.close()
            }
            rttLogWriter = null
            val file = File(sessionRepository.surveyDir(), fileName)
            runCatching {
                rttLogWriter = BufferedWriter(FileWriter(file, false)).apply {
                    write(StreamSpec.RTT.headerLine)
                    newLine()
                    flush()
                }
            }
        }
    }

    private fun closeRttWriter() {
        surveyIoExecutor.execute {
            runCatching {
                rttLogWriter?.flush()
                rttLogWriter?.close()
            }
            rttLogWriter = null
        }
    }

    private fun ensureWifiReceiver() {
        if (wifiReceiverRegistered) return
        context.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiReceiverRegistered = true
    }

    private data class FullWifiConn(val bssid: String, val ssid: String, val rssi: Int, val freqMhz: Int)

    @Suppress("DEPRECATION")
    private fun readConnectedWifiInfo(): Pair<String, String>? {
        val full = readConnectedWifiFull() ?: return null
        return full.bssid to full.ssid
    }

    @Suppress("DEPRECATION")
    private fun readConnectedWifiFull(): FullWifiConn? {
        val activeInfo: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cm = connectivityManager
            val network = runCatching { cm?.activeNetwork }.getOrNull()
            val caps = if (network != null) runCatching { cm?.getNetworkCapabilities(network) }.getOrNull() else null
            val ti = caps?.transportInfo as? WifiInfo
            if (ti != null && !ti.bssid.isNullOrBlank() && ti.bssid != "02:00:00:00:00:00") {
                ti
            } else {
                wifiManager?.connectionInfo
            }
        } else {
            wifiManager?.connectionInfo
        }
        val bssid = activeInfo?.bssid ?: return null
        if (bssid.isBlank() || bssid == "02:00:00:00:00:00") return null
        val ssid = activeInfo.ssid?.removeSurrounding("\"") ?: ""
        return FullWifiConn(bssid, ssid, activeInfo.rssi, activeInfo.frequency)
    }

    private fun hasBlePermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            fine && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            fine
        }
    }

    private fun hasWifiPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val nearby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.NEARBY_WIFI_DEVICES) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return fine && nearby
    }

    private fun setStatus(msg: String) {
        _state.value = _state.value.copy(statusMessage = msg)
    }
}
