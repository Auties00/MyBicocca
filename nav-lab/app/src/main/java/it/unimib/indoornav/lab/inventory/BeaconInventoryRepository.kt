package it.unimib.indoornav.lab.inventory

import android.content.Context
import it.unimib.indoornav.lab.format.CsvCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class BeaconInventoryRepository(context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val storeFile = File(context.filesDir, STORE_FILE_NAME)
    private val lock = Any()

    private val _entries = MutableStateFlow(loadFromDisk())
    val entries: StateFlow<List<SavedBeaconEntry>> = _entries.asStateFlow()

    private var lastTelemetryPersistMs: Long = 0L

    private fun loadFromDisk(): List<SavedBeaconEntry> {
        if (!storeFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<SavedBeaconEntry>>(storeFile.readText()).map(::withNumericPosition)
        }.getOrDefault(emptyList())
    }

    fun upsert(entry: SavedBeaconEntry) {
        val now = System.currentTimeMillis()
        val updatedList = synchronized(lock) {
            val current = _entries.value
            val existing = current.firstOrNull { candidate ->
                candidate.id == entry.id ||
                    (entry.canonicalKey.isNotBlank() && candidate.canonicalKey == entry.canonicalKey) ||
                    (entry.macAddress.isNotBlank() && candidate.macAddress.equals(entry.macAddress, ignoreCase = true))
            }
            val merged = if (existing != null) {
                entry.copy(
                    id = existing.id,
                    linkedKeys = (existing.linkedKeys + entry.linkedKeys + entry.canonicalKey)
                        .filter { it.isNotBlank() }
                        .distinct(),
                    seenFrameTypes = existing.seenFrameTypes + entry.seenFrameTypes,
                    createdAtMs = if (existing.createdAtMs > 0L) existing.createdAtMs else now,
                    updatedAtMs = now,
                )
            } else {
                entry.copy(
                    id = entry.id.ifBlank { UUID.randomUUID().toString() },
                    linkedKeys = (entry.linkedKeys + entry.canonicalKey)
                        .filter { it.isNotBlank() }
                        .distinct(),
                    createdAtMs = if (entry.createdAtMs > 0L) entry.createdAtMs else now,
                    updatedAtMs = now,
                )
            }
            val filtered = current.filterNot { candidate ->
                candidate.id == merged.id ||
                    (merged.canonicalKey.isNotBlank() && candidate.canonicalKey == merged.canonicalKey) ||
                    (merged.macAddress.isNotBlank() && candidate.macAddress.equals(merged.macAddress, ignoreCase = true))
            }
            filtered + merged
        }
        _entries.value = updatedList
        persistAsync(updatedList)
    }

    /** Adds the entries no saved beacon already covers (by ID or linked ID); returns how many were added. */
    fun importNew(imported: List<SavedBeaconEntry>): Int {
        val (updatedList, added) = synchronized(lock) {
            val current = _entries.value
            val knownKeys = current.flatMap { it.linkedKeys + it.canonicalKey }.filter { it.isNotBlank() }.toSet()
            val fresh = imported.filter { entry -> (entry.linkedKeys + entry.canonicalKey).none { it in knownKeys } }
            (current + fresh) to fresh.size
        }
        if (added > 0) {
            _entries.value = updatedList
            persistAsync(updatedList)
        }
        return added
    }

    fun updatePosition(id: String, x: Double?, y: Double?) = modify(id) { it.copy(x = x, y = y) }

    fun setPhoto(id: String, photoFile: String?) = modify(id) { it.copy(photoFile = photoFile) }

    private fun modify(id: String, change: (SavedBeaconEntry) -> SavedBeaconEntry) {
        val updatedList = synchronized(lock) {
            _entries.value.map { if (it.id == id) change(it).copy(updatedAtMs = System.currentTimeMillis()) else it }
        }
        _entries.value = updatedList
        persistAsync(updatedList)
    }

    fun remove(id: String) {
        val updatedList = synchronized(lock) {
            _entries.value.filterNot { it.id == id }
        }
        _entries.value = updatedList
        persistAsync(updatedList)
    }

    fun updateLiveTelemetry(liveGrouped: List<GroupedLiveBeacon>, nowMs: Long = System.currentTimeMillis()) {
        if (liveGrouped.isEmpty()) return
        var structureChanged = false
        var telemetryChanged = false
        val updatedList = synchronized(lock) {
            val current = _entries.value
            if (current.isEmpty()) return
            current.map { saved ->
                val live = liveGrouped.firstOrNull { BeaconGrouping.matchesSavedEntry(saved, it) }
                if (live == null) {
                    saved
                } else {
                    val nextLinked = (saved.linkedKeys + live.canonicalKeys)
                        .filter { it.isNotBlank() }
                        .distinct()
                    val nextFrames = saved.seenFrameTypes + live.frameTypes
                    val nextCanonical = if (live.identityKey.startsWith("ed:") || saved.canonicalKey.isBlank()) {
                        live.identityKey
                    } else {
                        saved.canonicalKey
                    }
                    val nextMac = live.macAddress.ifBlank { saved.macAddress }
                    if (nextLinked != saved.linkedKeys ||
                        nextFrames != saved.seenFrameTypes ||
                        nextCanonical != saved.canonicalKey ||
                        nextMac != saved.macAddress
                    ) {
                        structureChanged = true
                    }
                    telemetryChanged = true
                    saved.copy(
                        canonicalKey = nextCanonical,
                        macAddress = nextMac,
                        linkedKeys = nextLinked,
                        seenFrameTypes = nextFrames,
                        lastRssi = live.currentRssi,
                        lastMedianRssi = live.medianRssi,
                        lastTxPowerAdv = live.txPowerAdv ?: saved.lastTxPowerAdv,
                        lastBattMv = live.tlmBattMv ?: saved.lastBattMv,
                        lastTempC = live.tlmTempC ?: saved.lastTempC,
                        lastSeenMs = maxOf(saved.lastSeenMs, live.lastSeenMs),
                    )
                }
            }
        }
        if (telemetryChanged) {
            _entries.value = updatedList
            if (structureChanged || nowMs - lastTelemetryPersistMs >= TELEMETRY_PERSIST_INTERVAL_MS) {
                lastTelemetryPersistMs = nowMs
                persistAsync(updatedList)
            }
        }
    }

    suspend fun exportCatalogCsv(surveyDir: File): File = withContext(Dispatchers.IO) {
        surveyDir.mkdirs()
        val snapshot = _entries.value
        val outFile = File(surveyDir, CATALOG_CSV_NAME)
        val tmpFile = File(surveyDir, "$CATALOG_CSV_NAME.tmp")
        tmpFile.bufferedWriter().use { bw ->
            bw.write(
                listOf(
                    "id",
                    "canonical_key",
                    "mac_address",
                    "linked_keys",
                    "label",
                    "alphanumeric_code",
                    "building",
                    "floor",
                    "position_description",
                    "seen_frame_types",
                    "last_rssi",
                    "last_median_rssi",
                    "last_tx_power_adv",
                    "last_batt_mv",
                    "last_temp_c",
                    "last_seen_ms",
                    "created_at_ms",
                    "updated_at_ms",
                    "x",
                    "y",
                    "adv_interval_ms",
                    "active_slots",
                    "battery_reset_at_ms",
                    "photo_file",
                ).joinToString(","),
            )
            bw.newLine()
            for (e in snapshot) {
                bw.write(
                    CsvCodec.formatRow(
                        e.id,
                        e.canonicalKey,
                        e.macAddress,
                        e.linkedKeys.joinToString(";"),
                        e.label,
                        e.alphanumericCode,
                        e.building,
                        e.floor,
                        e.positionDescription,
                        e.seenFrameTypes.joinToString(";") { it.wireName },
                        e.lastRssi ?: "",
                        e.lastMedianRssi ?: "",
                        e.lastTxPowerAdv ?: "",
                        e.lastBattMv ?: "",
                        e.lastTempC ?: "",
                        e.lastSeenMs,
                        e.createdAtMs,
                        e.updatedAtMs,
                        e.x ?: "",
                        e.y ?: "",
                        e.advIntervalMs,
                        e.activeSlots.joinToString(";") { it.name },
                        e.batteryResetAtMs ?: "",
                        e.photoFile ?: "",
                    ),
                )
                bw.newLine()
            }
        }
        if (!tmpFile.renameTo(outFile)) {
            tmpFile.copyTo(outFile, overwrite = true)
            tmpFile.delete()
        }
        outFile
    }

    private fun persistAsync(snapshot: List<SavedBeaconEntry>) {
        ioScope.launch {
            writeAtomic(snapshot)
        }
    }

    private fun writeAtomic(snapshot: List<SavedBeaconEntry>) {
        synchronized(lock) {
            storeFile.parentFile?.mkdirs()
            val tmpFile = File(storeFile.parentFile, "$STORE_FILE_NAME.tmp")
            runCatching {
                tmpFile.writeText(json.encodeToString(snapshot))
                if (!tmpFile.renameTo(storeFile)) {
                    tmpFile.copyTo(storeFile, overwrite = true)
                    tmpFile.delete()
                }
            }
        }
    }

    private companion object {
        const val STORE_FILE_NAME = "beacon_inventory.json"
        const val CATALOG_CSV_NAME = "beacon_inventory_catalog.csv"
        const val TELEMETRY_PERSIST_INTERVAL_MS = 10_000L
    }
}

// Entries imported before positions were numeric carry them in the description as "x 19.31 m, y 6.16 m, ...".
private val TEXT_POSITION = Regex("""^x (-?\d+(?:\.\d+)?) m, y (-?\d+(?:\.\d+)?) m(?:, (.*))?$""")

internal fun withNumericPosition(entry: SavedBeaconEntry): SavedBeaconEntry {
    if (entry.x != null) return entry
    val match = TEXT_POSITION.matchEntire(entry.positionDescription.trim()) ?: return entry
    return entry.copy(
        x = match.groupValues[1].toDouble(),
        y = match.groupValues[2].toDouble(),
        positionDescription = match.groupValues[3],
    )
}
