package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore persistence of recent searches: one JSON-serialized, most-recent-first list
 * per account, capped at [MAX_ENTRIES]. DataStore rather than Room because this is a tiny
 * user-preference-shaped list with no relational queries — the same shape as the other
 * settings stores, and it avoids a database migration.
 */
@Singleton
class SearchHistoryStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    fun history(accountId: AccountId): Flow<List<SearchHistoryEntry>> =
        dataStore.data.map { prefs -> prefs[keyFor(accountId)]?.let(::deserialize).orEmpty() }

    /**
     * Inserts the trimmed query at the head, dropping any previous entry with the same
     * text (case-insensitive) and trimming the list to [MAX_ENTRIES]. A null [pickedKey]
     * preserves the pick already learned for this query — a bare submit must not erase it.
     * Blank queries are ignored.
     */
    suspend fun add(accountId: AccountId, query: String, pickedKey: String? = null) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            val key = keyFor(accountId)
            val current = prefs[key]?.let(::deserialize).orEmpty()
            val previousPick = current
                .firstOrNull { it.query.equals(trimmed, ignoreCase = true) }
                ?.pickedKey
            val updated = buildList {
                add(SearchHistoryEntry(trimmed, Instant.now(), pickedKey ?: previousPick))
                addAll(current.filterNot { it.query.equals(trimmed, ignoreCase = true) })
            }.take(MAX_ENTRIES)
            prefs[key] = serialize(updated)
        }
    }

    suspend fun remove(accountId: AccountId, query: String) {
        dataStore.edit { prefs ->
            val key = keyFor(accountId)
            val current = prefs[key]?.let(::deserialize).orEmpty()
            prefs[key] = serialize(current.filterNot { it.query.equals(query, ignoreCase = true) })
        }
    }

    suspend fun clear(accountId: AccountId) {
        dataStore.edit { prefs -> prefs.remove(keyFor(accountId)) }
    }

    private fun serialize(entries: List<SearchHistoryEntry>): String =
        Json.encodeToString(entries.map { StoredEntry(it.query, it.timestamp.toEpochMilli(), it.pickedKey) })

    /** Tolerates corrupt payloads by treating them as an empty history. */
    private fun deserialize(raw: String): List<SearchHistoryEntry> =
        runCatching {
            Json.decodeFromString<List<StoredEntry>>(raw)
                .map { SearchHistoryEntry(it.query, Instant.ofEpochMilli(it.timestamp), it.pickedKey) }
        }.getOrDefault(emptyList())

    private fun keyFor(accountId: AccountId) =
        stringPreferencesKey("search_history_${accountId.value}")

    /**
     * JSON wire shape of one entry; pickedKey defaults to null so payloads written without
     * the field still deserialize.
     */
    @Serializable
    private data class StoredEntry(val query: String, val timestamp: Long, val pickedKey: String? = null)

    private companion object {
        const val MAX_ENTRIES = 20
    }
}
