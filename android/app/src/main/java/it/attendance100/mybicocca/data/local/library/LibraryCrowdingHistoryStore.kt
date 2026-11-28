package it.attendance100.mybicocca.data.local.library

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.data.mapper.library.LibraryZoneId
import it.attendance100.mybicocca.di.DataStoreModule
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingSlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dedicated Preferences DataStore storing per-library, per-weekday crowding history as JSON,
 * together with the initial background collector's lifecycle flags. Corrupt payloads read as empty,
 * and entries older than [RETENTION_DAYS] days are ignored on read and pruned on write.
 */
@Singleton
class LibraryCrowdingHistoryStore @Inject constructor(
    @Named(DataStoreModule.LIBRARY_CROWDING_HISTORY_DATA_STORE)
    private val dataStore: DataStore<Preferences>,
) {
    fun observeHistory(libraryId: String): Flow<Map<DayOfWeek, LibraryWeekdayCrowding>> =
        dataStore.data.map { prefs ->
            val cutoff = currentSiteDate().minusDays(RETENTION_DAYS)
            decodeValid(prefs[historyKey(libraryId)], cutoff)
        }

    suspend fun getHistory(libraryId: String): Map<DayOfWeek, LibraryWeekdayCrowding> =
        observeHistory(libraryId).first()

    suspend fun upsert(libraryId: String, entry: LibraryWeekdayCrowding) {
        val cutoff = currentSiteDate().minusDays(RETENTION_DAYS)
        dataStore.edit { prefs ->
            val key = historyKey(libraryId)
            val existing = decodeValid(prefs[key], cutoff).toMutableMap()
            if (!entry.date.isBefore(cutoff)) {
                existing[entry.weekday] = mergeSameDate(existing[entry.weekday], entry)
            } else {
                existing.remove(entry.weekday)
            }
            if (existing.isEmpty()) {
                prefs.remove(key)
            } else {
                prefs[key] = encode(existing)
            }
        }
    }

    // The service may answer with only the rest of the day, so the earlier part is kept.
    private fun mergeSameDate(
        old: LibraryWeekdayCrowding?,
        new: LibraryWeekdayCrowding,
    ): LibraryWeekdayCrowding {
        if (old == null || old.date != new.date || new.isClosed) return new
        val firstNewStart = new.slots.minOfOrNull { it.start } ?: return new
        val earlier = old.slots
            .filter { it.start.isBefore(firstNewStart) }
            .map { slot ->
                val overlaps = slot.end.isAfter(firstNewStart) || slot.end == LocalTime.MIDNIGHT
                if (overlaps) slot.copy(end = firstNewStart) else slot
            }
        return new.copy(slots = earlier + new.slots)
    }

    suspend fun pruneExpired() {
        val cutoff = currentSiteDate().minusDays(RETENTION_DAYS)
        dataStore.edit { prefs ->
            val historyKeys = prefs.asMap().keys
                .filterIsInstance<Preferences.Key<String>>()
                .filter { it.name.startsWith(HISTORY_KEY_PREFIX) }
            for (key in historyKeys) {
                val raw = prefs[key] ?: continue
                val valid = decodeValid(raw, cutoff)
                if (valid.isEmpty()) {
                    prefs.remove(key)
                } else {
                    prefs[key] = encode(valid)
                }
            }
        }
    }

    suspend fun firstCollectionRunAt(): Instant? =
        dataStore.data.map { prefs -> prefs[FIRST_RUN_MS_KEY]?.let(Instant::ofEpochMilli) }.first()

    suspend fun recordFirstCollectionRunAt(at: Instant) {
        dataStore.edit { prefs ->
            if (prefs[FIRST_RUN_MS_KEY] == null) {
                prefs[FIRST_RUN_MS_KEY] = at.toEpochMilli()
            }
        }
    }

    suspend fun isInitialCollectionDone(): Boolean =
        dataStore.data.map { prefs -> prefs[INITIAL_DONE_KEY] ?: false }.first()

    suspend fun markInitialCollectionDone() {
        dataStore.edit { prefs ->
            prefs[INITIAL_DONE_KEY] = true
        }
    }

    private fun currentSiteDate(): LocalDate =
        Instant.now(AppClock.clock).atZone(LibraryZoneId).toLocalDate()

    private fun historyKey(libraryId: String) =
        stringPreferencesKey("$HISTORY_KEY_PREFIX$libraryId")

    private fun encode(entries: Map<DayOfWeek, LibraryWeekdayCrowding>): String {
        val payload = entries.values
            .sortedBy { it.weekday.value }
            .map { entry ->
                StoredWeekdayEntry(
                    weekday = entry.weekday.name,
                    date = entry.date.format(DateFormat),
                    slots = entry.slots.map { slot ->
                        StoredSlot(
                            start = slot.start.format(TimeFormat),
                            end = slot.end.format(TimeFormat),
                            occupancy = slot.occupancyPercentage,
                        )
                    },
                    isClosed = entry.isClosed,
                )
            }
        return json.encodeToString(payload)
    }

    private fun decodeValid(raw: String?, cutoff: LocalDate): Map<DayOfWeek, LibraryWeekdayCrowding> {
        if (raw.isNullOrBlank()) return emptyMap()
        val stored = runCatching { json.decodeFromString<List<StoredWeekdayEntry>>(raw) }
            .getOrElse { return emptyMap() }
        val result = linkedMapOf<DayOfWeek, LibraryWeekdayCrowding>()
        for (item in stored) {
            val weekday = runCatching { DayOfWeek.valueOf(item.weekday) }.getOrNull() ?: continue
            val date = runCatching { LocalDate.parse(item.date, DateFormat) }.getOrNull() ?: continue
            if (date.isBefore(cutoff)) continue
            val slots = item.slots.mapNotNull { slot ->
                val start = runCatching { LocalTime.parse(slot.start, TimeFormat) }.getOrNull()
                    ?: return@mapNotNull null
                val end = runCatching { LocalTime.parse(slot.end, TimeFormat) }.getOrNull()
                    ?: return@mapNotNull null
                LibraryCrowdingSlot(start = start, end = end, occupancyPercentage = slot.occupancy)
            }
            result[weekday] = LibraryWeekdayCrowding(
                weekday = weekday,
                date = date,
                slots = slots,
                isClosed = item.isClosed,
            )
        }
        return result
    }

    @Serializable
    private data class StoredWeekdayEntry(
        val weekday: String,
        val date: String,
        val slots: List<StoredSlot> = emptyList(),
        val isClosed: Boolean = false,
    )

    @Serializable
    private data class StoredSlot(
        val start: String,
        val end: String,
        val occupancy: Int,
    )

    companion object {
        const val RETENTION_DAYS = 28L
        const val HISTORY_KEY_PREFIX = "history_"
        private val FIRST_RUN_MS_KEY = longPreferencesKey("initial_collection_first_run_ms")
        private val INITIAL_DONE_KEY = booleanPreferencesKey("initial_collection_done")
        private val DateFormat = DateTimeFormatter.ISO_LOCAL_DATE
        private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
        private val json = Json { ignoreUnknownKeys = true }
    }
}
