package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.survey.BeaconInventoryItem
import kotlinx.serialization.Serializable

@Serializable
data class SavedBeaconEntry(
    val id: String,
    val canonicalKey: String,
    val macAddress: String,
    val linkedKeys: List<String> = emptyList(),
    val label: String,
    val alphanumericCode: String = "",
    val building: String,
    val floor: String,
    val positionDescription: String = "",
    val seenFrameTypes: Set<BeaconFrameType> = emptySet(),
    val lastRssi: Int? = null,
    val lastMedianRssi: Int? = null,
    val lastTxPowerAdv: Int? = null,
    val lastBattMv: Int? = null,
    val lastTempC: Double? = null,
    val lastSeenMs: Long = 0L,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
)

data class GroupedLiveBeacon(
    val identityKey: String,
    val macAddress: String,
    val canonicalKeys: Set<String>,
    val frameTypes: List<BeaconFrameType>,
    val framesByType: Map<BeaconFrameType, BeaconInventoryItem>,
    val primaryFrame: BeaconInventoryItem,
    val projectLabel: String?,
    val currentRssi: Int,
    val medianRssi: Int,
    val packetsPerSec: Double,
    val txPowerAdv: Int?,
    val tlmBattMv: Int?,
    val tlmTempC: Double?,
    val tlmAdvCount: Long?,
    val tlmUptimeSec: Double?,
    val estimatedIntervalByFrame: Map<BeaconFrameType, Int>,
    val lastSeenMs: Long,
    val rawAdvHexByFrame: Map<BeaconFrameType, String>,
)

data class ResolvedBeaconTile(
    val tileKey: String,
    val savedEntry: SavedBeaconEntry?,
    val liveBeacon: GroupedLiveBeacon?,
    val isOnline: Boolean,
    /** Still online, but nothing has been heard from it for [STALE_AFTER_MS]. */
    val isStale: Boolean = false,
    val displayLabel: String,
    val alphanumericCode: String,
    val building: String,
    val floor: String,
    val positionDescription: String,
    val macAddress: String,
    val canonicalKey: String,
    val frameTypes: List<BeaconFrameType>,
    val currentRssi: Int?,
    val medianRssi: Int?,
    val packetsPerSec: Double?,
    val txPowerAdv: Int?,
    val tlmBattMv: Int?,
    val tlmTempC: Double?,
    val tlmAdvCount: Long?,
    val tlmUptimeSec: Double?,
    val estimatedIntervalByFrame: Map<BeaconFrameType, Int>,
    val rawAdvHexByFrame: Map<BeaconFrameType, String>,
    val lastSeenMs: Long,
) {
    val isSaved: Boolean
        get() = savedEntry != null
}

enum class InventorySortOption(val label: String) {
    AVAILABILITY_THEN_LABEL("On first · Label"),
    AVAILABILITY_THEN_RSSI("On first · RSSI ↓"),
    LABEL_ASC("Label A–Z"),
    BUILDING_FLOOR_LABEL("Building · Floor · Label"),
    CODE_ASC("Code A–Z"),
    LAST_SEEN_DESC("Last Seen ↓"),
}

enum class BeaconViewMode(val label: String) {
    LIST("List View"),
    GRID("Grid View"),
}
