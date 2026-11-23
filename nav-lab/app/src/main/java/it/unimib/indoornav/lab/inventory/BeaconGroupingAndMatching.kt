package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameParser
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.survey.BeaconInventoryItem
import kotlin.math.roundToInt

const val OFFLINE_TIMEOUT_MS = 20_000L
const val STALE_AFTER_MS = 10_000L

data class EddystoneTlmExtras(
    val advCount: Long,
    val uptimeSec: Double,
)

object EddystoneTlmExtrasParser {
    private const val AD_TYPE_SERVICE_DATA_16 = 0x16
    private const val EDDYSTONE_UUID_LO = 0xAA
    private const val EDDYSTONE_UUID_HI = 0xFE

    fun parse(rawBytes: ByteArray): EddystoneTlmExtras? {
        if (rawBytes.isEmpty()) return null
        val activeLen = BeaconFrameParser.activeAdvertisementLength(rawBytes)
        var index = 0
        while (index < activeLen) {
            val length = rawBytes[index].toInt() and 0xFF
            if (length == 0) {
                index++
                continue
            }
            val payloadEnd = index + 1 + length
            if (payloadEnd > activeLen) break

            val adType = rawBytes[index + 1].toInt() and 0xFF
            val dataOffset = index + 2
            val dataLen = length - 1

            if (adType == AD_TYPE_SERVICE_DATA_16 && dataLen >= 16 &&
                (rawBytes[dataOffset].toInt() and 0xFF) == EDDYSTONE_UUID_LO &&
                (rawBytes[dataOffset + 1].toInt() and 0xFF) == EDDYSTONE_UUID_HI
            ) {
                val frameOffset = dataOffset + 2
                val frameType = rawBytes[frameOffset].toInt() and 0xFF
                val version = rawBytes[frameOffset + 1].toInt() and 0xFF
                if (frameType == 0x20 && version == 0x00) {
                    val advCount = uint32Be(rawBytes, frameOffset + 6)
                    val secCount = uint32Be(rawBytes, frameOffset + 10)
                    return EddystoneTlmExtras(
                        advCount = advCount,
                        uptimeSec = secCount / 10.0,
                    )
                }
            }
            index = payloadEnd
        }
        return null
    }

    private fun uint32Be(bytes: ByteArray, offset: Int): Long {
        return ((bytes[offset].toLong() and 0xFFL) shl 24) or
            ((bytes[offset + 1].toLong() and 0xFFL) shl 16) or
            ((bytes[offset + 2].toLong() and 0xFFL) shl 8) or
            (bytes[offset + 3].toLong() and 0xFFL)
    }
}

object AdvertisingIntervalEstimator {
    private const val RECEPTION_RATIO = 0.75
    private const val STEP_MS = 50.0

    fun estimateIntervalMs(interArrivalSamplesMs: List<Long>): Int? {
        val positive = interArrivalSamplesMs.filter { it > 0L }.sorted()
        if (positive.isEmpty()) return null
        val mid = positive.size / 2
        val median = if (positive.size % 2 == 1) {
            positive[mid].toDouble()
        } else {
            (positive[mid - 1] + positive[mid]) / 2.0
        }
        val scaled = median * RECEPTION_RATIO
        return ((scaled / STEP_MS).roundToInt() * 50).coerceAtLeast(50)
    }
}

object BeaconGrouping {
    private val framePriority = listOf(
        BeaconFrameType.ED_UID,
        BeaconFrameType.IBEACON,
        BeaconFrameType.ALTBEACON,
        BeaconFrameType.ED_TLM,
        BeaconFrameType.VENDOR,
    )

    fun groupByPhysicalBeacon(items: List<BeaconInventoryItem>): List<GroupedLiveBeacon> {
        if (items.isEmpty()) return emptyList()

        val groups = linkedMapOf<String, MutableList<BeaconInventoryItem>>()
        for (item in items) {
            val normalizedMac = item.addr.trim().uppercase()
            val groupKey = if (normalizedMac.isNotEmpty()) normalizedMac else "NO_MAC:${item.key}"
            groups.getOrPut(groupKey) { mutableListOf() }.add(item)
        }

        return groups.values.map { frames ->
            val framesByType = linkedMapOf<BeaconFrameType, BeaconInventoryItem>()
            for (ft in framePriority) {
                frames.filter { it.frameType == ft }
                    .maxByOrNull { it.lastSeenMs }
                    ?.let { framesByType[ft] = it }
            }

            val orderedFrames = framesByType.values.toList()
            val primaryFrame = orderedFrames.first()
            val mac = frames.firstNotNullOfOrNull { it.addr.trim().uppercase().takeIf(String::isNotEmpty) } ?: ""

            val uidId = framesByType[BeaconFrameType.ED_UID]?.canonicalId?.takeIf { it.isNotBlank() }
            val ibeaconId = framesByType[BeaconFrameType.IBEACON]?.canonicalId?.takeIf { it.isNotBlank() }
            val altId = framesByType[BeaconFrameType.ALTBEACON]?.canonicalId?.takeIf { it.isNotBlank() }
            val identityKey = uidId ?: ibeaconId ?: altId ?: mac.ifBlank { primaryFrame.key }

            val canonicalKeys = frames
                .map { it.canonicalId.trim() }
                .filter { it.isNotEmpty() }
                .toCollection(linkedSetOf())

            val latestValidRssiFrame = frames
                .filter { it.currentRssi != 127 }
                .maxByOrNull { it.lastSeenMs }
                ?: primaryFrame

            val txPowerAdv = framesByType[BeaconFrameType.ED_UID]?.txPowerAdv
                ?: framesByType[BeaconFrameType.IBEACON]?.txPowerAdv
                ?: orderedFrames.firstNotNullOfOrNull { it.txPowerAdv }

            val tlmFrame = framesByType[BeaconFrameType.ED_TLM]
            val tlmBattMv = tlmFrame?.tlmBattMv ?: orderedFrames.firstNotNullOfOrNull { it.tlmBattMv }
            val tlmTempC = tlmFrame?.tlmTempC ?: orderedFrames.firstNotNullOfOrNull { it.tlmTempC }
            val tlmAdvCount = tlmFrame?.tlmAdvCount ?: orderedFrames.firstNotNullOfOrNull { it.tlmAdvCount }
            val tlmUptimeSec = tlmFrame?.tlmUptimeSec ?: orderedFrames.firstNotNullOfOrNull { it.tlmUptimeSec }

            val estimatedIntervalByFrame = linkedMapOf<BeaconFrameType, Int>()
            val rawAdvHexByFrame = linkedMapOf<BeaconFrameType, String>()
            for ((ft, item) in framesByType) {
                AdvertisingIntervalEstimator.estimateIntervalMs(item.interArrivalSamplesMs)?.let {
                    estimatedIntervalByFrame[ft] = it
                }
                if (item.advHex.isNotBlank()) {
                    rawAdvHexByFrame[ft] = item.advHex
                }
            }

            GroupedLiveBeacon(
                identityKey = identityKey,
                macAddress = mac,
                canonicalKeys = canonicalKeys,
                frameTypes = orderedFrames.map { it.frameType },
                framesByType = framesByType,
                primaryFrame = primaryFrame,
                projectLabel = orderedFrames.firstNotNullOfOrNull { it.projectLabel },
                currentRssi = latestValidRssiFrame.currentRssi,
                medianRssi = latestValidRssiFrame.medianRssi,
                packetsPerSec = frames.sumOf { it.packetsPerSec },
                txPowerAdv = txPowerAdv,
                tlmBattMv = tlmBattMv,
                tlmTempC = tlmTempC,
                tlmAdvCount = tlmAdvCount,
                tlmUptimeSec = tlmUptimeSec,
                estimatedIntervalByFrame = estimatedIntervalByFrame,
                lastSeenMs = frames.maxOf { it.lastSeenMs },
                rawAdvHexByFrame = rawAdvHexByFrame,
            )
        }.sortedByDescending { it.currentRssi }
    }

    fun matchesSavedEntry(entry: SavedBeaconEntry, live: GroupedLiveBeacon): Boolean {
        if (entry.canonicalKey.isNotBlank() && entry.canonicalKey in live.canonicalKeys) return true
        if (entry.linkedKeys.any { it.isNotBlank() && it in live.canonicalKeys }) return true
        if (entry.macAddress.isNotBlank() && live.macAddress.isNotBlank() &&
            entry.macAddress.equals(live.macAddress, ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun resolveInventoryTiles(
        savedEntries: List<SavedBeaconEntry>,
        liveGrouped: List<GroupedLiveBeacon>,
        nowMs: Long,
        offlineTimeoutMs: Long = OFFLINE_TIMEOUT_MS,
    ): List<ResolvedBeaconTile> {
        return savedEntries.map { saved ->
            val live = liveGrouped.firstOrNull { matchesSavedEntry(saved, it) }
            val effectiveLastSeen = maxOf(saved.lastSeenMs, live?.lastSeenMs ?: 0L)
            val isOnline = live != null && (nowMs - live.lastSeenMs) <= offlineTimeoutMs
            val isStale = isOnline && (nowMs - live.lastSeenMs) > STALE_AFTER_MS
            val frameTypes = if (live != null) {
                (saved.seenFrameTypes + live.frameTypes).distinctByFramePriority()
            } else {
                saved.seenFrameTypes.toList().distinctByFramePriority()
            }
            ResolvedBeaconTile(
                tileKey = saved.id,
                savedEntry = saved,
                liveBeacon = live,
                isOnline = isOnline,
                isStale = isStale,
                displayLabel = saved.label,
                alphanumericCode = saved.alphanumericCode,
                building = saved.building,
                floor = saved.floor,
                positionDescription = saved.positionDescription,
                macAddress = live?.macAddress?.ifBlank { saved.macAddress } ?: saved.macAddress,
                canonicalKey = live?.identityKey?.ifBlank { saved.canonicalKey } ?: saved.canonicalKey,
                frameTypes = frameTypes,
                currentRssi = if (isOnline) live.currentRssi else null,
                medianRssi = live?.medianRssi ?: saved.lastMedianRssi,
                packetsPerSec = if (isOnline) live.packetsPerSec else null,
                txPowerAdv = live?.txPowerAdv ?: saved.lastTxPowerAdv,
                tlmBattMv = live?.tlmBattMv ?: saved.lastBattMv,
                tlmTempC = live?.tlmTempC ?: saved.lastTempC,
                tlmAdvCount = live?.tlmAdvCount,
                tlmUptimeSec = live?.tlmUptimeSec,
                estimatedIntervalByFrame = live?.estimatedIntervalByFrame ?: emptyMap(),
                rawAdvHexByFrame = live?.rawAdvHexByFrame ?: emptyMap(),
                lastSeenMs = effectiveLastSeen,
            )
        }
    }

    fun resolveSurveyTiles(
        liveGrouped: List<GroupedLiveBeacon>,
        savedEntries: List<SavedBeaconEntry>,
        nowMs: Long,
    ): List<ResolvedBeaconTile> {
        return liveGrouped.map { live ->
            val silentMs = nowMs - live.lastSeenMs
            val saved = savedEntries.firstOrNull { matchesSavedEntry(it, live) }
            val fallbackLabel = live.projectLabel
                ?: shortIdentityLabel(live)
            ResolvedBeaconTile(
                tileKey = live.macAddress.ifBlank { live.identityKey },
                savedEntry = saved,
                liveBeacon = live,
                isOnline = silentMs <= OFFLINE_TIMEOUT_MS,
                isStale = silentMs in (STALE_AFTER_MS + 1)..OFFLINE_TIMEOUT_MS,
                displayLabel = saved?.label?.ifBlank { null } ?: fallbackLabel,
                alphanumericCode = saved?.alphanumericCode ?: "",
                building = saved?.building ?: "",
                floor = saved?.floor ?: "",
                positionDescription = saved?.positionDescription ?: "",
                macAddress = live.macAddress,
                canonicalKey = live.identityKey,
                frameTypes = live.frameTypes,
                currentRssi = live.currentRssi,
                medianRssi = live.medianRssi,
                packetsPerSec = live.packetsPerSec,
                txPowerAdv = live.txPowerAdv,
                tlmBattMv = live.tlmBattMv,
                tlmTempC = live.tlmTempC,
                tlmAdvCount = live.tlmAdvCount,
                tlmUptimeSec = live.tlmUptimeSec,
                estimatedIntervalByFrame = live.estimatedIntervalByFrame,
                rawAdvHexByFrame = live.rawAdvHexByFrame,
                lastSeenMs = live.lastSeenMs,
            )
        }
    }

    fun sortInventoryTiles(
        tiles: List<ResolvedBeaconTile>,
        option: InventorySortOption,
    ): List<ResolvedBeaconTile> {
        val comparator = when (option) {
            InventorySortOption.AVAILABILITY_THEN_LABEL ->
                compareByDescending<ResolvedBeaconTile> { it.isOnline }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.alphanumericCode, b.alphanumericCode) }
                    .thenBy { it.tileKey }

            InventorySortOption.AVAILABILITY_THEN_RSSI ->
                compareByDescending<ResolvedBeaconTile> { it.isOnline }
                    .thenByDescending { it.currentRssi ?: Int.MIN_VALUE }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }

            InventorySortOption.LABEL_ASC ->
                Comparator<ResolvedBeaconTile> { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.alphanumericCode, b.alphanumericCode) }

            InventorySortOption.BUILDING_FLOOR_LABEL ->
                Comparator<ResolvedBeaconTile> { a, b -> NaturalOrderComparator.compare(a.building, b.building) }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.floor, b.floor) }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }

            InventorySortOption.CODE_ASC ->
                Comparator<ResolvedBeaconTile> { a, b ->
                    NaturalOrderComparator.compare(
                        a.alphanumericCode.ifBlank { "\uFFFF" },
                        b.alphanumericCode.ifBlank { "\uFFFF" },
                    )
                }.thenComparator { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }

            InventorySortOption.LAST_SEEN_DESC ->
                compareByDescending<ResolvedBeaconTile> { it.lastSeenMs }
                    .thenComparator { a, b -> NaturalOrderComparator.compare(a.displayLabel, b.displayLabel) }
        }
        return tiles.sortedWith(comparator)
    }

    private fun Collection<BeaconFrameType>.distinctByFramePriority(): List<BeaconFrameType> {
        val set = toSet()
        return framePriority.filter { it in set }
    }

    private fun shortIdentityLabel(live: GroupedLiveBeacon): String {
        val id = live.identityKey
        return when {
            id.startsWith("ed:") -> id.substringAfterLast(':').takeLast(6).uppercase()
            id.startsWith("ib:") -> {
                val parts = id.split(':')
                if (parts.size >= 4) "${parts[2]}:${parts[3]}" else id
            }
            id.isNotBlank() -> id.takeLast(8)
            else -> live.primaryFrame.frameType.wireName
        }
    }
}
