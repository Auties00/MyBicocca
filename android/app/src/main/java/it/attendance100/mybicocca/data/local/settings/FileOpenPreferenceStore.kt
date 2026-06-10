package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the per-file-kind open choices in the shared `mybicocca_settings` DataStore.
 *
 * Each remembered kind (image/video/audio/html/text/zip…) is one `file_open_<kind>` string key
 * holding a [FileOpenChoice] entry name; a kind with no key means "ask every time". Unknown
 * stored values are skipped rather than surfaced.
 */
@Singleton
class FileOpenPreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val choices: Flow<Map<String, FileOpenChoice>> = dataStore.data.map { prefs ->
        buildMap {
            prefs.asMap().forEach { (key, value) ->
                if (key.name.startsWith(PREFIX) && value is String) {
                    val choice = runCatching { FileOpenChoice.valueOf(value) }.getOrNull()
                    if (choice != null) put(key.name.removePrefix(PREFIX), choice)
                }
            }
        }
    }

    suspend fun setChoice(kind: String, choice: FileOpenChoice) {
        dataStore.edit { prefs -> prefs[keyFor(kind)] = choice.name }
    }

    suspend fun clearChoice(kind: String) {
        dataStore.edit { prefs -> prefs.remove(keyFor(kind)) }
    }

    private fun keyFor(kind: String) = stringPreferencesKey("$PREFIX$kind")

    private companion object {
        const val PREFIX = "file_open_"
    }
}
