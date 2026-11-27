package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.SystemClock
import it.unimib.indoornav.lab.data.DeviceCapabilities
import it.unimib.indoornav.lab.format.StreamSpec

abstract class BaseSensorCapture(
    protected val sensorManager: SensorManager,
    protected val sensorHandler: Handler,
    protected val writer: SessionWriter,
    private val accuracyCapture: SensorAccuracyCapture?,
    private val androidSensorType: Int,
    private val manifestType: String,
    private val targetPeriodUs: Int,
    protected val spec: StreamSpec,
) : StreamCapture, SensorEventListener {

    override val streamKey: String = spec.streamKey

    private var sensor: Sensor? = null
    private var registered = false

    val isAvailable: Boolean
        get() = sensorManager.getDefaultSensor(androidSensorType) != null

    override fun start(): Boolean {
        if (registered) return true
        val s = sensorManager.getDefaultSensor(androidSensorType) ?: return false
        sensor = s
        writer.openStream(spec)
        val periodUs = DeviceCapabilities.effectivePeriodUs(targetPeriodUs, s)
        // Never use SENSOR_DELAY_FASTEST; explicit period >= 5,000 us avoids HIGH_SAMPLING_RATE_SENSORS.
        registered = sensorManager.registerListener(this, s, periodUs, 0, sensorHandler)
        if (!registered) {
            writer.recordError("Failed to register sensor $manifestType")
        }
        return registered
    }

    override fun stop() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        accuracyCapture?.onSensorAccuracy(manifestType, accuracy)
    }

    protected fun rxNowNs(): Long = SystemClock.elapsedRealtimeNanos()
}
