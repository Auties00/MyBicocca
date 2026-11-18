package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec

class GyroscopeRawCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
    manifestType = "gyroscope_uncalibrated",
    targetPeriodUs = 10_000,
    spec = StreamSpec.GYRO_RAW,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val v = event.values
        val bx = if (v.size >= 6) v[3] else 0f
        val by = if (v.size >= 6) v[4] else 0f
        val bz = if (v.size >= 6) v[5] else 0f
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, v[0], v[1], v[2], bx, by, bz)
    }
}
