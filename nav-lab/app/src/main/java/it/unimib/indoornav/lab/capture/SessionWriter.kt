package it.unimib.indoornav.lab.capture

import android.os.SystemClock
import it.unimib.indoornav.lab.format.CsvCodec
import it.unimib.indoornav.lab.format.StreamSpec
import it.unimib.indoornav.lab.format.StreamStats
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class LiveStreamCounter(
    val streamKey: String,
    val rows: Long,
    val rowsPerSec: Double,
    val silentMs: Long,
    val active: Boolean,
)

class SessionWriter(private val sessionDir: File) {
    @Volatile
    private var writerThread: Thread? = null

    private val ioExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "LabCsvWriter").apply {
            priority = Thread.NORM_PRIORITY
            writerThread = this
        }
    }

    private class StreamHandle(
        val spec: StreamSpec,
        val writer: BufferedWriter,
        initialRxNs: Long,
    ) {
        @Volatile
        var rows: Long = 0L

        @Volatile
        var firstTNs: Long = 0L

        @Volatile
        var lastTNs: Long = 0L

        @Volatile
        var lastRxNs: Long = initialRxNs

        var prevSnapshotRows: Long = 0L

        @Volatile
        var lastRate: Double = 0.0
    }

    private val handles = ConcurrentHashMap<String, StreamHandle>()
    private val errors = mutableListOf<String>()
    private var flushTask: ScheduledFuture<*>? = null

    private val closed = AtomicBoolean(false)
    private var handlesClosed = false

    init {
        sessionDir.mkdirs()
        flushTask = ioExecutor.scheduleAtFixedRate({
            flushAndUpdateRates()
        }, 1, 1, TimeUnit.SECONDS)
    }

    fun openStream(spec: StreamSpec) {
        if (closed.get()) return
        val openedRxNs = SystemClock.elapsedRealtimeNanos()
        runCatching {
            ioExecutor.execute {
                if (handlesClosed || handles.containsKey(spec.streamKey)) return@execute
                try {
                    val file = File(sessionDir, spec.fileName)
                    val bw = BufferedWriter(FileWriter(file, false), 32 * 1024)
                    bw.write(spec.headerLine)
                    bw.newLine()
                    handles[spec.streamKey] = StreamHandle(spec, bw, openedRxNs)
                } catch (t: Throwable) {
                    recordError("open ${spec.fileName}: ${t.message}")
                }
            }
        }
    }

    fun writeRow(spec: StreamSpec, tNs: Long, rxNs: Long, vararg fields: Any?) {
        if (closed.get()) return
        val line = CsvCodec.formatRow(fields.asList())
        runCatching {
            ioExecutor.execute {
                if (handlesClosed) return@execute
                val handle = handles[spec.streamKey] ?: return@execute
                try {
                    handle.writer.write(line)
                    handle.writer.newLine()
                    if (handle.rows == 0L) {
                        handle.firstTNs = tNs
                    }
                    handle.rows++
                    handle.lastTNs = tNs
                    handle.lastRxNs = rxNs
                } catch (t: Throwable) {
                    recordError("write ${spec.fileName}: ${t.message}")
                }
            }
        }
    }

    fun emergencyFlush() {
        if (handlesClosed) return
        if (Thread.currentThread() === writerThread) {
            for (h in handles.values) {
                runCatching { h.writer.flush() }
            }
            return
        }
        val flushedOnExecutor = runCatching {
            ioExecutor.submit {
                for (h in handles.values) {
                    runCatching { h.writer.flush() }
                }
            }.get(1, TimeUnit.SECONDS)
            true
        }.getOrDefault(false)
        if (!flushedOnExecutor) {
            for (h in handles.values) {
                runCatching { h.writer.flush() }
            }
        }
    }

    fun snapshotCounters(activeKeys: Set<String>): List<LiveStreamCounter> {
        val nowNs = SystemClock.elapsedRealtimeNanos()
        return StreamSpec.entries.mapNotNull { spec ->
            val h = handles[spec.streamKey]
            val active = activeKeys.contains(spec.streamKey)
            if (h == null && !active) return@mapNotNull null
            val rows = h?.rows ?: 0L
            val rate = h?.lastRate ?: 0.0
            val lastRx = h?.lastRxNs ?: 0L
            val silentMs = if (lastRx > 0L) maxOf(0L, (nowNs - lastRx) / 1_000_000L) else 0L
            LiveStreamCounter(
                streamKey = spec.streamKey,
                rows = rows,
                rowsPerSec = rate,
                silentMs = silentMs,
                active = active,
            )
        }
    }

    fun buildStreamStats(): Map<String, StreamStats> {
        val result = linkedMapOf<String, StreamStats>()
        for ((key, h) in handles) {
            result[key] = StreamStats(
                rows = h.rows,
                first_t_ns = h.firstTNs,
                last_t_ns = h.lastTNs,
            )
        }
        return result
    }

    fun recordError(message: String) {
        synchronized(errors) {
            if (errors.size < 200) {
                errors.add(message)
            }
        }
    }

    fun getErrors(): List<String> = synchronized(errors) { errors.toList() }

    private fun flushAndUpdateRates() {
        for (h in handles.values) {
            val delta = h.rows - h.prevSnapshotRows
            h.prevSnapshotRows = h.rows
            h.lastRate = delta.toDouble()
            try {
                h.writer.flush()
            } catch (t: Throwable) {
                recordError("flush ${h.spec.fileName}: ${t.message}")
            }
        }
    }

    fun close(): Map<String, StreamStats> {
        if (!closed.compareAndSet(false, true)) return buildStreamStats()
        flushTask?.cancel(false)
        val future = runCatching {
            ioExecutor.submit<Map<String, StreamStats>> {
                handlesClosed = true
                val stats = buildStreamStats()
                for (h in handles.values) {
                    runCatching {
                        h.writer.flush()
                        h.writer.close()
                    }
                }
                stats
            }
        }.getOrNull()
        val stats = runCatching { future?.get(5, TimeUnit.SECONDS) }.getOrNull() ?: buildStreamStats()
        ioExecutor.shutdown()
        return stats
    }
}
