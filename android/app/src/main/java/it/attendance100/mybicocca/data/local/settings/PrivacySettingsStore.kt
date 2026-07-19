package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the privacy preferences.
 */
@Singleton
class PrivacySettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** On by default: crash reports are collected unless the user opts out. */
    val crashReportingEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CRASH_REPORTING_ENABLED_KEY] ?: true
    }

    suspend fun setCrashReportingEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[CRASH_REPORTING_ENABLED_KEY] = enabled }
    }

    private companion object {
        val CRASH_REPORTING_ENABLED_KEY = booleanPreferencesKey("privacy_crash_reporting_enabled")
    }
}
