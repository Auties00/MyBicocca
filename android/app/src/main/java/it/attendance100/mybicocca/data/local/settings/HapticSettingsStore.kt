package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the haptic-feedback preference in the shared `mybicocca_settings` DataStore. */
@Singleton
class HapticSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAPTICS_ENABLED_KEY] ?: true
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[HAPTICS_ENABLED_KEY] = enabled }
    }

    private companion object {
        val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")
    }
}
