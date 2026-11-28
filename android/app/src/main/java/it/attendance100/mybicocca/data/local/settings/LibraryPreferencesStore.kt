package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists lightweight Biblioteca user preferences in the shared settings DataStore — currently
 * the Affluences site id of the library the student last opened or booked.
 */
@Singleton
class LibraryPreferencesStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun lastUsedLibraryId(): String? =
        dataStore.data.map { prefs -> prefs[LAST_USED_LIBRARY_ID_KEY]?.takeIf { it.isNotBlank() } }.first()

    suspend fun setLastUsedLibraryId(libraryId: String) {
        val trimmed = libraryId.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            prefs[LAST_USED_LIBRARY_ID_KEY] = trimmed
        }
    }

    private companion object {
        val LAST_USED_LIBRARY_ID_KEY = stringPreferencesKey("library_last_used_id")
    }
}
