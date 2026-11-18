package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec
import kotlin.math.sqrt

class GameRotationCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_GAME_ROTATION_VECTOR,
    manifestType = "game_rotation_vector",
    targetPeriodUs = 20_000,
    spec = StreamSpec.GAME_ROT,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val v = event.values
        val qx = v[0]
        val qy = v[1]
        val qz = v[2]
        val qw = if (v.size >= 4) v[3] else computeQw(qx, qy, qz)
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, qx, qy, qz, qw)
    }

    private fun computeQw(x: Float, y: Float, z: Float): Float {
        val t = 1.0f - (x * x + y * y + z * z)
        return if (t > 0f) sqrt(t) else 0f
    }
}
