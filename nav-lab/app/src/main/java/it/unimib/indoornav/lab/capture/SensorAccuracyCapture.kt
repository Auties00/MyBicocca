package it.unimib.indoornav.lab.capture

import android.os.SystemClock
import it.unimib.indoornav.lab.format.StreamSpec

class SensorAccuracyCapture(
    private val writer: SessionWriter,
) : StreamCapture {
    override val streamKey: String = StreamSpec.SENSOR_ACCURACY.streamKey

    @Volatile
    private var started = false

    override fun start(): Boolean {
        if (started) return true
        started = true
        writer.openStream(StreamSpec.SENSOR_ACCURACY)
        return true
    }

    override fun stop() {
        started = false
    }

    fun onSensorAccuracy(sensorType: String, accuracy: Int) {
        if (!started) return
        val rxNs = SystemClock.elapsedRealtimeNanos()
        writer.writeRow(StreamSpec.SENSOR_ACCURACY, rxNs, rxNs, rxNs, sensorType, accuracy)
    }
}
