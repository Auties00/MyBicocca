package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec
import kotlin.math.sqrt

class AccelerometerCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
    private val onSample: ((tNs: Long, x: Float, y: Float, z: Float, norm: Double) -> Unit)? = null,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_ACCELEROMETER,
    manifestType = "accelerometer",
    targetPeriodUs = 10_000,
    spec = StreamSpec.ACC,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, x, y, z)
        if (onSample != null) {
            val norm = sqrt((x * x + y * y + z * z).toDouble())
            onSample.invoke(rxNs, x, y, z, norm)
        }
    }
}
