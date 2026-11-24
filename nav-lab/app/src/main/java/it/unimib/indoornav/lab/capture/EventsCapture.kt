package it.unimib.indoornav.lab.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.PowerManager
import android.os.SystemClock
import it.unimib.indoornav.lab.format.StreamSpec

class EventsCapture(
    private val context: Context,
    private val handler: Handler,
    private val writer: SessionWriter,
) : StreamCapture {

    override val streamKey: String = StreamSpec.EVENTS.streamKey

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var started = false
    private var lastBatteryPercent = -1

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val kind = when (action) {
                Intent.ACTION_SCREEN_ON -> "screen_on"
                Intent.ACTION_SCREEN_OFF -> "screen_off"
                Intent.ACTION_USER_PRESENT -> "user_present"
                else -> return
            }
            logEvent(kind)
        }
    }

    private val thermalListener = PowerManager.OnThermalStatusChangedListener { status ->
        logEvent("thermal", thermalStatusName(status))
    }

    private val batteryRunnable = object : Runnable {
        override fun run() {
            if (!started) return
            emitBatteryLevel()
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun start(): Boolean {
        if (started) return true
        started = true
        writer.openStream(StreamSpec.EVENTS)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(screenReceiver, filter, null, handler)
        powerManager.addThermalStatusListener(thermalListener)
        handler.post(batteryRunnable)
        return true
    }

    override fun stop() {
        if (!started) return
        started = false
        handler.removeCallbacks(batteryRunnable)
        runCatching { context.unregisterReceiver(screenReceiver) }
        runCatching { powerManager.removeThermalStatusListener(thermalListener) }
    }

    fun logEvent(kind: String, detail: String = "") {
        val rxNs = SystemClock.elapsedRealtimeNanos()
        writer.writeRow(StreamSpec.EVENTS, rxNs, rxNs, rxNs, kind, detail)
    }

    private fun emitBatteryLevel() {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        if (level >= 0 && scale > 0) {
            val pct = (level * 100) / scale
            lastBatteryPercent = pct
            logEvent("battery", pct.toString())
        }
    }

    private fun thermalStatusName(status: Int): String = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "none"
        PowerManager.THERMAL_STATUS_LIGHT -> "light"
        PowerManager.THERMAL_STATUS_MODERATE -> "moderate"
        PowerManager.THERMAL_STATUS_SEVERE -> "severe"
        PowerManager.THERMAL_STATUS_CRITICAL -> "critical"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "emergency"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "shutdown"
        else -> "unknown_$status"
    }
}
