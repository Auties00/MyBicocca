package it.unimib.indoornav.lab.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.SystemClock
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.format.StreamSpec

class StepsCapture(
    private val context: Context,
    private val sensorManager: SensorManager,
    private val sensorHandler: Handler,
    private val writer: SessionWriter,
    private val accuracyCapture: SensorAccuracyCapture?,
) : StreamCapture, SensorEventListener {

    override val streamKey: String = StreamSpec.STEPS.streamKey

    private val startLock = Any()
    private var registeredDetector = false
    private var registeredCounter = false

    override fun start(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            writer.recordError("Missing ACTIVITY_RECOGNITION permission")
            return false
        }
        val detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (detector == null && counter == null) {
            return false
        }
        synchronized(startLock) {
            if (detector != null && !registeredDetector) {
                registeredDetector = sensorManager.registerListener(
                    this,
                    detector,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    0,
                    sensorHandler,
                )
            }
            if (counter != null && !registeredCounter) {
                registeredCounter = sensorManager.registerListener(
                    this,
                    counter,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    0,
                    sensorHandler,
                )
            }
            if (registeredDetector || registeredCounter) {
                writer.openStream(StreamSpec.STEPS)
                return true
            }
            writer.recordError("Failed to register step sensors")
            return false
        }
    }

    override fun stop() {
        synchronized(startLock) {
            if (registeredDetector || registeredCounter) {
                sensorManager.unregisterListener(this)
                registeredDetector = false
                registeredCounter = false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        synchronized(startLock) {
            if (!registeredDetector && !registeredCounter) return
        }
        val rxNs = SystemClock.elapsedRealtimeNanos()
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                writer.writeRow(StreamSpec.STEPS, event.timestamp, rxNs, event.timestamp, rxNs, "detector", 1)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val count = event.values[0].toLong()
                writer.writeRow(StreamSpec.STEPS, event.timestamp, rxNs, event.timestamp, rxNs, "counter", count)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val type = when (sensor?.type) {
            Sensor.TYPE_STEP_DETECTOR -> "step_detector"
            Sensor.TYPE_STEP_COUNTER -> "step_counter"
            else -> return
        }
        accuracyCapture?.onSensorAccuracy(type, accuracy)
    }
}
