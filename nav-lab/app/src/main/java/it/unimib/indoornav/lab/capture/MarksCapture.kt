package it.unimib.indoornav.lab.capture

import android.os.SystemClock
import it.unimib.indoornav.lab.format.StreamSpec

class MarksCapture(
    private val writer: SessionWriter,
) : StreamCapture {

    override val streamKey: String = StreamSpec.MARKS.streamKey

    @Volatile
    private var started = false

    override fun start(): Boolean {
        if (started) return true
        started = true
        writer.openStream(StreamSpec.MARKS)
        return true
    }

    override fun stop() {
        started = false
    }

    fun recordMark(kind: String, label: String, source: String) {
        if (!started) return
        val rxNs = SystemClock.elapsedRealtimeNanos()
        writer.writeRow(StreamSpec.MARKS, rxNs, rxNs, rxNs, rxNs, kind, label, source)
    }
}
