package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SessionEvaluatorTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `healthy 30s session with at least 10 wifi scans per min is green`() {
        val dir = tempDir.toFile()
        writeHealthyStreams(dir, durationSec = 30L, wifiScanCount = 6)
        val report = SessionEvaluator.evaluateDirectory(dir, manifest("guided"))
        assertEquals(QuickLookStatus.GREEN, report.status, "Findings: ${report.findings}")
    }

    @Test
    fun `flags gyro and baro gaps over 2 seconds as amber`() {
        val dir = tempDir.toFile()
        writeHealthyStreams(dir, durationSec = 30L, wifiScanCount = 6)

        // Overwrite gyro.csv with a 2.5s gap
        File(dir, StreamSpec.GYRO.fileName).writeText(
            buildString {
                appendLine(StreamSpec.GYRO.headerLine)
                appendLine("1000000000,1000000000,0.0,0.0,0.0")
                appendLine("3500000000,3500000000,0.0,0.0,0.0")
            },
        )

        // Overwrite baro.csv with a 3.0s gap
        File(dir, StreamSpec.BARO.fileName).writeText(
            buildString {
                appendLine(StreamSpec.BARO.headerLine)
                appendLine("1000000000,1000000000,1013.2")
                appendLine("4000000000,4000000000,1013.3")
            },
        )

        val report = SessionEvaluator.evaluateDirectory(dir, manifest("guided"))
        assertEquals(QuickLookStatus.AMBER, report.status)
        assertTrue(report.findings.any { it.contains("gyro gap") })
        assertTrue(report.findings.any { it.contains("baro gap") })
    }

    @Test
    fun `flags low wifi scan rate under 10 per minute as amber`() {
        val dir = tempDir.toFile()
        writeHealthyStreams(dir, durationSec = 60L, wifiScanCount = 4)

        val report = SessionEvaluator.evaluateDirectory(dir, manifest("guided"))
        assertEquals(QuickLookStatus.AMBER, report.status)
        assertTrue(report.findings.any { it.contains("Low Wi-Fi scan rate") })
    }

    @Test
    fun `marker_only mode skips wifi scan rate and gyro or baro checks`() {
        val dir = tempDir.toFile()
        File(dir, StreamSpec.ACC.fileName).writeText(
            buildString {
                appendLine(StreamSpec.ACC.headerLine)
                for (s in 0..30) {
                    val ns = 1_000_000_000L + s * 1_000_000_000L
                    appendLine("$ns,$ns,0.0,0.0,9.81")
                }
            },
        )
        File(dir, StreamSpec.EVENTS.fileName).writeText(
            buildString {
                appendLine(StreamSpec.EVENTS.headerLine)
                appendLine("1000000000,phase,shake")
            },
        )
        File(dir, StreamSpec.MARKS.fileName).writeText(
            buildString {
                appendLine(StreamSpec.MARKS.headerLine)
                appendLine("2000000000,2000000000,checkpoint,CP1,button")
            },
        )

        val report = SessionEvaluator.evaluateDirectory(dir, manifest("marker_only"))
        assertEquals(QuickLookStatus.GREEN, report.status, "Findings: ${report.findings}")
    }

    private fun writeHealthyStreams(dir: File, durationSec: Long, wifiScanCount: Int) {
        val startNs = 1_000_000_000L
        File(dir, StreamSpec.ACC.fileName).writeText(
            buildString {
                appendLine(StreamSpec.ACC.headerLine)
                for (s in 0..durationSec) {
                    val ns = startNs + s * 1_000_000_000L
                    appendLine("$ns,$ns,0.0,0.0,9.81")
                }
            },
        )
        File(dir, StreamSpec.GYRO.fileName).writeText(
            buildString {
                appendLine(StreamSpec.GYRO.headerLine)
                for (s in 0..durationSec) {
                    val ns = startNs + s * 1_000_000_000L
                    appendLine("$ns,$ns,0.0,0.0,0.0")
                }
            },
        )
        File(dir, StreamSpec.BARO.fileName).writeText(
            buildString {
                appendLine(StreamSpec.BARO.headerLine)
                for (s in 0..durationSec) {
                    val ns = startNs + s * 1_000_000_000L
                    appendLine("$ns,$ns,1013.2")
                }
            },
        )
        File(dir, StreamSpec.BLE.fileName).writeText(
            buildString {
                appendLine(StreamSpec.BLE.headerLine)
                for (s in 0..durationSec) {
                    val ns = startNs + s * 1_000_000_000L
                    appendLine("$ns,$ns,AA:BB:CC:DD:EE:FF,-60,ed_uid,ed:515b86185cea0ec01b22:0000000e0001,-20,,,020106")
                }
            },
        )
        File(dir, StreamSpec.MARKS.fileName).writeText(
            buildString {
                appendLine(StreamSpec.MARKS.headerLine)
                appendLine("${startNs + 1_000_000_000L},${startNs + 1_000_000_000L},checkpoint,CP1,button")
            },
        )
        File(dir, StreamSpec.EVENTS.fileName).writeText(
            buildString {
                appendLine(StreamSpec.EVENTS.headerLine)
                for (i in 1..wifiScanCount) {
                    val ns = startNs + i * 2_000_000_000L
                    appendLine("$ns,wifi_scan,scan_id=$i other=1")
                }
            },
        )
    }

    private fun manifest(recordingMode: String): SessionManifest = SessionManifest(
        app_version = "0.1.0",
        phone = PhoneInfo(label = "A1", model = "Pixel", os = "15", manufacturer = "Google"),
        capabilities = CapabilitiesInfo(barometer = true, gyroscope = true, wifi_rtt = true, arcore = false),
        setup = SetupManifestInfo(
            operator = "Marco",
            marker = "Elena",
            building = "U14",
            floor = 1,
            test_id = "E5-route=R1",
            carry_mode = "hand-up",
            recording_mode = recordingMode,
        ),
        clock = ClockInfo(wall_ms = 1763300000000L, elapsed_ns = 1_000_000_000L),
        stopped_cleanly = true,
    )
}
