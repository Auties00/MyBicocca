package it.unimib.indoornav.lab.format

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QuickLookStatus {
    GREEN,
    AMBER,
    RED,
}

data class SessionHealthReport(
    val status: QuickLookStatus,
    val durationSec: Double,
    val sizeBytes: Long,
    val streamRows: Map<String, Long>,
    val findings: List<String>,
)

object SessionNaming {
    private val INVALID_PATH_CHARS = Regex("[^A-Za-z0-9._=-]")

    fun buildFolderName(
        wallMs: Long,
        phoneLabel: String,
        testId: String,
    ): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val ts = sdf.format(Date(wallMs))
        val cleanPhone = sanitizeSegment(phoneLabel.ifBlank { "A0" })
        val cleanTestId = sanitizeSegment(testId.ifBlank { "E0" })
        return "${ts}_${cleanPhone}_${cleanTestId}"
    }

    fun sanitizeSegment(segment: String): String =
        segment.trim().replace(INVALID_PATH_CHARS, "_")
}

object SessionEvaluator {
    private const val MAX_SAMPLE_DIFFS = 4000
    private const val SPREAD_THRESHOLD_NS = 50_000_000L
    private const val OFFSET_THRESHOLD_NS = 1_000_000_000L
    private const val GAP_THRESHOLD_NS = 2_000_000_000L
    private const val MIN_WIFI_SCANS_PER_MIN = 10.0
    private const val MIN_DURATION_FOR_WIFI_RATE_SEC = 10.0

    fun evaluateDirectory(sessionDir: File, manifest: SessionManifest?): SessionHealthReport {
        val findings = mutableListOf<String>()
        var status = QuickLookStatus.GREEN

        fun escalate(target: QuickLookStatus, message: String) {
            findings.add(message)
            if (target.ordinal > status.ordinal) {
                status = target
            }
        }

        val files = sessionDir.listFiles()?.filter { it.isFile } ?: emptyList()
        val totalBytes = files.sumOf { it.length() }

        val streamRows = linkedMapOf<String, Long>()
        var minRxNs = Long.MAX_VALUE
        var maxRxNs = Long.MIN_VALUE
        var maxAccGapNs = 0L
        var maxGyroGapNs = 0L
        var maxBaroGapNs = 0L
        var maxBleGapNs = 0L
        var wifiRefusedCount = 0
        var wifiScanCount = 0

        for (spec in StreamSpec.entries) {
            val file = File(sessionDir, spec.fileName)
            if (!file.exists()) continue

            var rowCount = 0L
            var badLines = 0
            val diffs = ArrayList<Long>()
            var prevRxNs: Long? = null

            file.bufferedReader().useLines { lines ->
                val iter = lines.iterator()
                if (!iter.hasNext()) return@useLines
                iter.next() // header
                while (iter.hasNext()) {
                    val line = iter.next()
                    if (line.isBlank()) continue
                    val cols = CsvCodec.parseLine(line)
                    if (cols == null || cols.size != spec.columns.size) {
                        badLines++
                        continue
                    }
                    rowCount++

                    val rxIdx = if (spec == StreamSpec.EVENTS || spec == StreamSpec.SENSOR_ACCURACY) 0 else 1
                    val rxNs = cols[rxIdx].toLongOrNull()
                    if (rxNs != null) {
                        if (rxNs < minRxNs) minRxNs = rxNs
                        if (rxNs > maxRxNs) maxRxNs = rxNs
                        if (prevRxNs != null) {
                            val gap = rxNs - prevRxNs
                            when (spec) {
                                StreamSpec.ACC -> if (gap > maxAccGapNs) maxAccGapNs = gap
                                StreamSpec.GYRO -> if (gap > maxGyroGapNs) maxGyroGapNs = gap
                                StreamSpec.BARO -> if (gap > maxBaroGapNs) maxBaroGapNs = gap
                                StreamSpec.BLE -> if (gap > maxBleGapNs) maxBleGapNs = gap
                                else -> Unit
                            }
                        }
                        prevRxNs = rxNs
                    }

                    if (!spec.timeBaseExempt && rxNs != null && diffs.size < MAX_SAMPLE_DIFFS) {
                        val tNs = cols[0].toLongOrNull()
                        if (tNs != null) {
                            diffs.add(rxNs - tNs)
                        }
                    }

                    if (spec == StreamSpec.EVENTS && cols.size >= 2) {
                        when (cols[1]) {
                            "wifi_scan_refused" -> wifiRefusedCount++
                            "wifi_scan" -> wifiScanCount++
                        }
                    }
                }
            }

            streamRows[spec.streamKey] = rowCount
            if (badLines > 0) {
                escalate(QuickLookStatus.AMBER, "${spec.fileName}: $badLines skipped line(s)")
            }

            if (diffs.size >= 10) {
                diffs.sort()
                val p5 = diffs[(diffs.size * 0.05).toInt()]
                val p95 = diffs[((diffs.size - 1) * 0.95).toInt()]
                val median = diffs[diffs.size / 2]
                val spread = p95 - p5
                if (spread > SPREAD_THRESHOLD_NS) {
                    escalate(QuickLookStatus.AMBER, "${spec.streamKey}: rx_ns-t_ns spread ${spread / 1_000_000} ms")
                } else if (kotlin.math.abs(median) > OFFSET_THRESHOLD_NS) {
                    escalate(QuickLookStatus.AMBER, "${spec.streamKey}: clock offset ${median / 1_000_000} ms")
                }
            }
        }

        if (manifest == null) {
            escalate(QuickLookStatus.RED, "Missing or unreadable manifest.json")
        } else {
            if (!manifest.stopped_cleanly) {
                escalate(QuickLookStatus.AMBER, "Session did not stop cleanly")
            }
            if (manifest.errors.isNotEmpty()) {
                escalate(QuickLookStatus.AMBER, "${manifest.errors.size} recorded error(s)")
            }
        }

        val mode = manifest?.setup?.recording_mode
        if ((streamRows["acc"] ?: 0L) == 0L) {
            escalate(QuickLookStatus.RED, "No accelerometer rows")
        }
        if (mode != "marker_only" && (streamRows["ble"] ?: 0L) == 0L) {
            escalate(QuickLookStatus.RED, "No BLE rows")
        }
        if (mode == "guided" && (streamRows["marks"] ?: 0L) == 0L) {
            escalate(QuickLookStatus.AMBER, "Guided walk has no checkpoint marks")
        }
        if (maxAccGapNs > GAP_THRESHOLD_NS) {
            escalate(QuickLookStatus.AMBER, "acc gap ${maxAccGapNs / 1_000_000} ms (>2s)")
        }
        if (mode != "marker_only" && maxGyroGapNs > GAP_THRESHOLD_NS) {
            escalate(QuickLookStatus.AMBER, "gyro gap ${maxGyroGapNs / 1_000_000} ms (>2s)")
        }
        if (mode != "marker_only" && maxBaroGapNs > GAP_THRESHOLD_NS) {
            escalate(QuickLookStatus.AMBER, "baro gap ${maxBaroGapNs / 1_000_000} ms (>2s)")
        }
        if (mode != "marker_only" && maxBleGapNs > GAP_THRESHOLD_NS) {
            escalate(QuickLookStatus.AMBER, "ble gap ${maxBleGapNs / 1_000_000} ms (>2s)")
        }
        if (wifiRefusedCount > 0) {
            escalate(QuickLookStatus.AMBER, "$wifiRefusedCount refused Wi-Fi scan(s)")
        }

        val durationSec = if (maxRxNs > minRxNs && minRxNs != Long.MAX_VALUE) {
            (maxRxNs - minRxNs) / 1_000_000_000.0
        } else {
            0.0
        }

        if (mode != "marker_only" && durationSec >= MIN_DURATION_FOR_WIFI_RATE_SEC) {
            val scansPerMin = wifiScanCount / (durationSec / 60.0)
            if (scansPerMin < MIN_WIFI_SCANS_PER_MIN) {
                escalate(
                    QuickLookStatus.AMBER,
                    "Low Wi-Fi scan rate: %.1f/min (<10/min)".format(Locale.US, scansPerMin),
                )
            }
        }

        return SessionHealthReport(
            status = status,
            durationSec = durationSec,
            sizeBytes = totalBytes,
            streamRows = streamRows,
            findings = findings,
        )
    }
}
