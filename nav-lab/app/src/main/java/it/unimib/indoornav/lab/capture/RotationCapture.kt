package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec
import kotlin.math.sqrt

class RotationCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_ROTATION_VECTOR,
    manifestType = "rotation_vector",
    targetPeriodUs = 20_000,
    spec = StreamSpec.ROT,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val v = event.values
        val qx = v[0]
        val qy = v[1]
        val qz = v[2]
        val qw = if (v.size >= 4) v[3] else computeQw(qx, qy, qz)
        val headingAcc = if (v.size >= 5) v[4] else -1f
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, qx, qy, qz, qw, headingAcc)
    }

    private fun computeQw(x: Float, y: Float, z: Float): Float {
        val t = 1.0f - (x * x + y * y + z * z)
        return if (t > 0f) sqrt(t) else 0f
    }
}
