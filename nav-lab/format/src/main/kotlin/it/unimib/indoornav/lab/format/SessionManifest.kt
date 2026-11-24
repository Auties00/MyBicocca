package it.unimib.indoornav.lab.format

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PhoneInfo(
    val label: String,
    val model: String,
    val os: String,
    val manufacturer: String,
)

@Serializable
data class SensorManifestEntry(
    val type: String,
    val name: String,
    val vendor: String,
    val min_delay_us: Int,
    val resolution: Float,
    val requested_period_us: Int,
)

@Serializable
data class CapabilitiesInfo(
    val barometer: Boolean,
    val gyroscope: Boolean,
    val wifi_rtt: Boolean,
    val arcore: Boolean,
)

@Serializable
data class SetupManifestInfo(
    val operator: String,
    val marker: String,
    val building: String,
    val floor: Int,
    val test_id: String,
    val carry_mode: String,
    val recording_mode: String,
    val template_fields: Map<String, String> = emptyMap(),
    val notes: String = "",
)

@Serializable
data class ClockInfo(
    val wall_ms: Long,
    val elapsed_ns: Long,
)

@Serializable
data class StreamStats(
    val rows: Long,
    val first_t_ns: Long,
    val last_t_ns: Long,
)

@Serializable
data class SessionManifest(
    val format_version: Int = ProjectConstants.FORMAT_VERSION,
    val app_version: String,
    val platform: String = "android",
    val phone: PhoneInfo,
    val sensors: List<SensorManifestEntry> = emptyList(),
    val capabilities: CapabilitiesInfo,
    val setup: SetupManifestInfo,
    val clock: ClockInfo,
    val university_ssids: List<String> = ProjectConstants.DEFAULT_UNIVERSITY_SSIDS,
    val streams: Map<String, StreamStats> = emptyMap(),
    val errors: List<String> = emptyList(),
    val stopped_cleanly: Boolean = false,
) {
    fun toJsonString(): String = ManifestJson.encodeToString(this)

    companion object {
        val ManifestJson = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun fromJsonString(raw: String): SessionManifest = ManifestJson.decodeFromString(raw)
    }
}
