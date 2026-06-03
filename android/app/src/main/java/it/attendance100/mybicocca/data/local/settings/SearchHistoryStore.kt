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

// Recent searches, most-recent-first, capped per account. DataStore (not Room) because this
// is a tiny user-preference-shaped list with no relational queries — same shape as the other
// settings stores, and it avoids a database migration.
@Singleton
class SearchHistoryStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    fun history(accountId: AccountId): Flow<List<SearchHistoryEntry>> =
        dataStore.data.map { prefs -> prefs[keyFor(accountId)]?.let(::deserialize).orEmpty() }

    suspend fun add(accountId: AccountId, query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            val key = keyFor(accountId)
            val current = prefs[key]?.let(::deserialize).orEmpty()
            val updated = buildList {
                add(SearchHistoryEntry(trimmed, Instant.now()))
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
        Json.encodeToString(entries.map { StoredEntry(it.query, it.timestamp.toEpochMilli()) })

    private fun deserialize(raw: String): List<SearchHistoryEntry> =
        runCatching {
            Json.decodeFromString<List<StoredEntry>>(raw)
                .map { SearchHistoryEntry(it.query, Instant.ofEpochMilli(it.timestamp)) }
        }.getOrDefault(emptyList())

    private fun keyFor(accountId: AccountId) =
        stringPreferencesKey("search_history_${accountId.value}")

    @Serializable
    private data class StoredEntry(val query: String, val timestamp: Long)

    private companion object {
        const val MAX_ENTRIES = 20
    }
}
