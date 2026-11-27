package it.unimib.indoornav.lab.capture

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.format.StreamSpec
import java.util.concurrent.Executor

class WifiRttCapture(
    private val context: Context,
    private val handler: Handler,
    private val writer: SessionWriter,
    private val periodMs: Long = 500L,
) : StreamCapture {

    override val streamKey: String = StreamSpec.RTT.streamKey

    private val rttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
    private val handlerExecutor = Executor { runnable -> handler.post(runnable) }

    @Volatile
    private var started = false

    @Volatile
    private var latestResponders: List<ScanResult> = emptyList()

    private val rangeRunnable = object : Runnable {
        override fun run() {
            if (!started) return
            triggerRanging()
            handler.postDelayed(this, periodMs)
        }
    }

    fun updateResponders(responders: List<ScanResult>) {
        latestResponders = responders.sortedByDescending { it.level }
    }

    override fun start(): Boolean {
        if (started) return true
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) || rttManager == null) {
            return false
        }
        if (!hasRttPermissions()) {
            writer.recordError("Missing Wi-Fi RTT permission")
            return false
        }
        started = true
        writer.openStream(StreamSpec.RTT)
        handler.postDelayed(rangeRunnable, periodMs)
        return true
    }

    override fun stop() {
        if (!started) return
        started = false
        handler.removeCallbacks(rangeRunnable)
    }

    @SuppressLint("MissingPermission")
    private fun triggerRanging() {
        val manager = rttManager ?: return
        if (!manager.isAvailable) return
        val maxPeers = minOf(10, RangingRequest.getMaxPeers())
        val targets = latestResponders.take(maxPeers)
        if (targets.isEmpty()) return

        val request = RangingRequest.Builder()
            .addAccessPoints(targets)
            .build()

        runCatching {
            manager.startRanging(request, handlerExecutor, object : RangingResultCallback() {
                override fun onRangingFailure(code: Int) {
                    writer.recordError("RTT ranging failure: code=$code")
                }

                override fun onRangingResults(results: MutableList<RangingResult>) {
                    val rxNs = SystemClock.elapsedRealtimeNanos()
                    for (res in results) {
                        val bssid = res.macAddress?.toString() ?: ""
                        if (res.status == RangingResult.STATUS_SUCCESS) {
                            val tNs = res.rangingTimestampMillis * 1_000_000L
                            writer.writeRow(
                                StreamSpec.RTT,
                                tNs,
                                rxNs,
                                tNs,
                                rxNs,
                                bssid,
                                res.status,
                                res.distanceMm,
                                res.distanceStdDevMm,
                                res.rssi,
                                res.numAttemptedMeasurements,
                                res.numSuccessfulMeasurements,
                            )
                        } else {
                            writer.writeRow(
                                StreamSpec.RTT,
                                rxNs,
                                rxNs,
                                rxNs,
                                rxNs,
                                bssid,
                                res.status,
                                "",
                                "",
                                "",
                                "",
                                "",
                            )
                        }
                    }
                }
            })
        }.onFailure { t ->
            writer.recordError("RTT exception: ${t.message}")
        }
    }

    private fun hasRttPermissions(): Boolean {
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
}
