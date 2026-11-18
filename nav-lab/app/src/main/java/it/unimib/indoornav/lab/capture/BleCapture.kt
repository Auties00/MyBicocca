package it.unimib.indoornav.lab.capture

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.format.BeaconFrameParser
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.format.StreamSpec
import it.unimib.indoornav.lab.format.toHexLower
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class BleCapture(
    private val context: Context,
    private val handler: Handler,
    private val writer: SessionWriter,
    private val eventsCapture: EventsCapture,
    private val inventoryMode: Boolean,
    private val storeEverything: Boolean,
) : StreamCapture {

    override val streamKey: String = StreamSpec.BLE.streamKey

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private var scanner: BluetoothLeScanner? = null
    private val lastUidByAddr = ConcurrentHashMap<String, String>()
    private val otherPerSecond = AtomicInteger(0)

    @Volatile
    private var started = false

    private val restartRunnable = object : Runnable {
        override fun run() {
            if (!started) return
            restartScan()
            handler.postDelayed(this, SCAN_RESTART_INTERVAL_MS)
        }
    }

    private val otherCountRunnable = object : Runnable {
        override fun run() {
            if (!started) return
            val count = otherPerSecond.getAndSet(0)
            eventsCapture.logEvent("ble_other_count", count.toString())
            handler.postDelayed(this, 1_000L)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                handleResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            writer.recordError("BLE scan failed: code=$errorCode")
            eventsCapture.logEvent("error", "ble_scan_failed_$errorCode")
        }
    }

    override fun start(): Boolean {
        if (started) return true
        if (!hasScanPermission()) {
            writer.recordError("Missing BLE scan permission")
            eventsCapture.logEvent("error", "ble_missing_permission")
            return false
        }
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            writer.recordError("Bluetooth adapter off or unavailable")
            eventsCapture.logEvent("error", "ble_adapter_off")
            return false
        }
        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            writer.recordError("BluetoothLeScanner unavailable")
            eventsCapture.logEvent("error", "ble_scanner_null")
            return false
        }

        started = true
        writer.openStream(StreamSpec.BLE)
        startInternal()
        handler.postDelayed(restartRunnable, SCAN_RESTART_INTERVAL_MS)
        if (inventoryMode) {
            handler.postDelayed(otherCountRunnable, 1_000L)
        }
        return true
    }

    override fun stop() {
        if (!started) return
        started = false
        handler.removeCallbacks(restartRunnable)
        handler.removeCallbacks(otherCountRunnable)
        stopInternal()
    }

    @SuppressLint("MissingPermission")
    private fun startInternal() {
        val sc = scanner ?: return
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        val filters = if (inventoryMode) emptyList() else defaultNormalFilters()
        runCatching {
            sc.startScan(filters, settings, scanCallback)
        }.onFailure { t ->
            writer.recordError("BLE startScan exception: ${t.message}")
            eventsCapture.logEvent("error", "ble_start_exception")
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopInternal() {
        val sc = scanner ?: return
        runCatching { sc.stopScan(scanCallback) }
    }

    private fun restartScan() {
        stopInternal()
        eventsCapture.logEvent("ble_scan_restart")
        handler.postDelayed({
            if (started) startInternal()
        }, 150L)
    }

    private fun handleResult(result: ScanResult) {
        val rxNs = SystemClock.elapsedRealtimeNanos()
        val tNs = result.timestampNanos
        val addr = result.device?.address ?: ""
        val rssi = result.rssi
        val rawBytes = result.scanRecord?.bytes ?: ByteArray(0)
        val parsed = BeaconFrameParser.parseRawAdvertisement(rawBytes)

        if (parsed == null) {
            if (inventoryMode) {
                otherPerSecond.incrementAndGet()
                if (storeEverything) {
                    val activeLen = BeaconFrameParser.activeAdvertisementLength(rawBytes)
                    val hex = rawBytes.copyOfRange(0, activeLen).toHexLower()
                    writer.writeRow(
                        StreamSpec.BLE,
                        tNs,
                        rxNs,
                        tNs,
                        rxNs,
                        addr,
                        rssi,
                        BeaconFrameType.VENDOR.wireName,
                        "",
                        "",
                        "",
                        "",
                        hex,
                    )
                }
            }
            return
        }

        val resolvedBeaconId = when (parsed.frameType) {
            BeaconFrameType.ED_UID -> {
                val id = parsed.beaconId ?: ""
                if (addr.isNotEmpty() && id.isNotEmpty()) {
                    lastUidByAddr[addr] = id
                }
                id
            }
            BeaconFrameType.ED_TLM -> lastUidByAddr[addr] ?: ""
            else -> parsed.beaconId ?: ""
        }

        val txPowerStr = parsed.txPowerAdv?.toString() ?: ""
        val battStr = parsed.tlmBattMv?.toString() ?: ""
        val tempStr = parsed.tlmTempC?.let { "%.2f".format(java.util.Locale.US, it) } ?: ""

        writer.writeRow(
            StreamSpec.BLE,
            tNs,
            rxNs,
            tNs,
            rxNs,
            addr,
            rssi,
            parsed.frameType.wireName,
            resolvedBeaconId,
            txPowerStr,
            battStr,
            tempStr,
            parsed.advHex,
        )
    }

    private fun hasScanPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            fine && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            fine
        }
    }

    companion object {
        const val SCAN_RESTART_INTERVAL_MS = 20L * 60_000L
        private val EDDYSTONE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")

        fun defaultNormalFilters(): List<ScanFilter> = listOf(
            ScanFilter.Builder().setServiceUuid(EDDYSTONE_UUID).build(),
            ScanFilter.Builder()
                .setManufacturerData(
                    0x004C,
                    byteArrayOf(0x02, 0x15),
                    byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
                )
                .build(),
        )
    }
}
