package it.unimib.indoornav.lab.capture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import it.unimib.indoornav.lab.format.StreamSpec

class BarometerCapture(
    sensorManager: SensorManager,
    sensorHandler: Handler,
    writer: SessionWriter,
    accuracyCapture: SensorAccuracyCapture?,
) : BaseSensorCapture(
    sensorManager = sensorManager,
    sensorHandler = sensorHandler,
    writer = writer,
    accuracyCapture = accuracyCapture,
    androidSensorType = Sensor.TYPE_PRESSURE,
    manifestType = "pressure",
    targetPeriodUs = 40_000,
    spec = StreamSpec.BARO,
) {
    override fun onSensorChanged(event: SensorEvent) {
        val rxNs = rxNowNs()
        writer.writeRow(spec, event.timestamp, rxNs, event.timestamp, rxNs, event.values[0])
    }
}
