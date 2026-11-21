package it.unimib.indoornav.lab.capture

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.format.StreamSpec
import java.util.concurrent.atomic.AtomicLong

class WifiCapture(
    private val context: Context,
    private val handler: Handler,
    private val writer: SessionWriter,
    private val eventsCapture: EventsCapture,
    private val universitySsids: Set<String>,
    private val onRespondersUpdated: ((List<ScanResult>) -> Unit)? = null,
) : StreamCapture {

    override val streamKey: String = StreamSpec.WIFI.streamKey

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val scanCounter = AtomicLong(0L)

    @Volatile
    private var started = false

    private val nextScanRunnable = Runnable {
        if (started) requestScan()
    }

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (!started) return
            if (intent?.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return
            val updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)
            if (!updated) {
                eventsCapture.logEvent("wifi_scan_refused")
                handler.removeCallbacks(nextScanRunnable)
                handler.postDelayed(nextScanRunnable, 2_000L)
                return
            }
            processScanResults()
            handler.removeCallbacks(nextScanRunnable)
            handler.postDelayed(nextScanRunnable, 500L)
        }
    }

    override fun start(): Boolean {
        if (started) return true
        if (wifiManager == null || !hasWifiPermissions()) {
            writer.recordError("Wi-Fi unavailable or missing permissions")
            return false
        }
        started = true
        writer.openStream(StreamSpec.WIFI)
        context.registerReceiver(
            scanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            null,
            handler,
        )
        requestScan()
        return true
    }

    override fun stop() {
        if (!started) return
        started = false
        handler.removeCallbacks(nextScanRunnable)
        runCatching { context.unregisterReceiver(scanReceiver) }
    }

    @Suppress("DEPRECATION")
    private fun requestScan() {
        val wm = wifiManager ?: return
        val accepted = runCatching { wm.startScan() }.getOrDefault(false)
        if (!accepted) {
            eventsCapture.logEvent("wifi_scan_refused")
            handler.removeCallbacks(nextScanRunnable)
            handler.postDelayed(nextScanRunnable, 2_000L)
        }
    }

    @SuppressLint("MissingPermission")
    private fun processScanResults() {
        val wm = wifiManager ?: return
        val results = runCatching { wm.scanResults }.getOrElse { emptyList() }
        val rxNs = SystemClock.elapsedRealtimeNanos()
        val scanId = scanCounter.incrementAndGet()
        var otherCount = 0
        val responders = mutableListOf<ScanResult>()

        for (sr in results) {
            if (sr.is80211mcResponder) {
                responders.add(sr)
            }
            val ssid = extractSsid(sr)
            if (ssid in universitySsids) {
                val tNs = sr.timestamp * 1_000L
                val bssid = sr.BSSID ?: ""
                val laBit = if (isLocallyAdministered(bssid)) 1 else 0
                val rttFlag = if (sr.is80211mcResponder) 1 else 0
                val azFlag = if (isAzResponder(sr)) 1 else 0
                val width = channelWidthLabel(sr.channelWidth)
                writer.writeRow(
                    StreamSpec.WIFI,
                    tNs,
                    rxNs,
                    tNs,
                    rxNs,
                    scanId,
                    bssid,
                    ssid,
                    sr.level,
                    sr.frequency,
                    width,
                    laBit,
                    rttFlag,
                    azFlag,
                )
            } else {
                otherCount++
            }
        }

        eventsCapture.logEvent("wifi_scan", "scan_id=$scanId other=$otherCount")
        onRespondersUpdated?.invoke(responders)
    }

    private fun hasWifiPermissions(): Boolean {
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

    companion object {
        @Suppress("DEPRECATION")
        fun extractSsid(sr: ScanResult): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sr.wifiSsid?.toString()?.removeSurrounding("\"") ?: ""
            } else {
                sr.SSID ?: ""
            }
        }

        /** 802.11az (next-generation ranging) responder; Android only reports it from API 35. */
        fun isAzResponder(sr: ScanResult): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && sr.is80211azNtbResponder

        fun isLocallyAdministered(bssid: String): Boolean {
            val firstOctet = bssid.split(':').firstOrNull()?.toIntOrNull(16) ?: return false
            return (firstOctet and 0x02) != 0
        }

        fun channelWidthLabel(width: Int): String = when (width) {
            ScanResult.CHANNEL_WIDTH_20MHZ -> "20"
            ScanResult.CHANNEL_WIDTH_40MHZ -> "40"
            ScanResult.CHANNEL_WIDTH_80MHZ -> "80"
            ScanResult.CHANNEL_WIDTH_160MHZ -> "160"
            ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> "80+80"
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && width == ScanResult.CHANNEL_WIDTH_320MHZ) {
                "320"
            } else {
                width.toString()
            }
        }
    }
}
