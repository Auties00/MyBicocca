package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec

class GyroscopeCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_GYROSCOPE,
    manifestType = "gyroscope",
    targetPeriodUs = 10_000,
    spec = StreamSpec.GYRO,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val v = event.values
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, v[0], v[1], v[2])
    }
}
