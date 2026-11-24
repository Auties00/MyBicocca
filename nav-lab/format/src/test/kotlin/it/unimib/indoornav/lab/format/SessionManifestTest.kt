package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SessionManifestTest {
    @Test
    fun `manifest serializes and deserializes with recording_mode and sensor types`() {
        val manifest = SessionManifest(
            app_version = "0.1.0",
            phone = PhoneInfo(label = "A1", model = "Pixel 8", os = "15", manufacturer = "Google"),
            sensors = listOf(
                SensorManifestEntry(
                    type = "accelerometer",
                    name = "LSM6DSR",
                    vendor = "STMicro",
                    min_delay_us = 2500,
                    resolution = 0.0012f,
                    requested_period_us = 10000,
                ),
            ),
            capabilities = CapabilitiesInfo(
                barometer = true,
                gyroscope = true,
                wifi_rtt = true,
                arcore = false,
            ),
            setup = SetupManifestInfo(
                operator = "Marco",
                marker = "Elena",
                building = "U14",
                floor = 1,
                test_id = "E5-route=R1-dir=out-pace=normal-layout=3",
                carry_mode = "pocket",
                recording_mode = "guided",
                template_fields = mapOf("route" to "R1", "dir" to "out"),
                notes = "",
            ),
            clock = ClockInfo(wall_ms = 1763300000000L, elapsed_ns = 123456789000L),
            university_ssids = listOf("eduroam"),
            streams = mapOf("acc" to StreamStats(rows = 1200, first_t_ns = 100L, last_t_ns = 200L)),
            stopped_cleanly = true,
        )

        val json = manifest.toJsonString()
        assertTrue(json.contains("\"recording_mode\": \"guided\""))
        assertTrue(json.contains("\"type\": \"accelerometer\""))

        val decoded = SessionManifest.fromJsonString(json)
        assertEquals(manifest, decoded)
    }
}
