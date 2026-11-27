package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec

class MagnetometerCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_MAGNETIC_FIELD,
    manifestType = "magnetic_field",
    targetPeriodUs = 20_000,
    spec = StreamSpec.MAG,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        val v = event.values
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, v[0], v[1], v[2])
    }
}
