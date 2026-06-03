package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the app-lock preferences (master enable + idle timeout). Backed by the shared
 * `mybicocca_settings` DataStore, mirroring [ElearningFilterStore].
 */
@Singleton
class SecuritySettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val appLockEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[APP_LOCK_ENABLED_KEY] ?: false
    }

    /** Minutes the app may stay backgrounded before it re-locks. `0` = lock immediately. */
    val lockTimeoutMinutes: Flow<Int> = dataStore.data.map { prefs ->
        prefs[LOCK_TIMEOUT_MINUTES_KEY] ?: DEFAULT_TIMEOUT_MINUTES
    }

    /**
     * Whether to mark the window secure (FLAG_SECURE) while the lock is active.
     * Off by default: screenshots and the recents thumbnail are allowed unless the user opts in.
     */
    val secureScreenEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SECURE_SCREEN_ENABLED_KEY] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[APP_LOCK_ENABLED_KEY] = enabled }
    }

    suspend fun setLockTimeoutMinutes(minutes: Int) {
        dataStore.edit { prefs -> prefs[LOCK_TIMEOUT_MINUTES_KEY] = minutes }
    }

    suspend fun setSecureScreenEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SECURE_SCREEN_ENABLED_KEY] = enabled }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MINUTES = 0
        val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("security_app_lock_enabled")
        val LOCK_TIMEOUT_MINUTES_KEY = intPreferencesKey("security_lock_timeout_minutes")
        val SECURE_SCREEN_ENABLED_KEY = booleanPreferencesKey("security_secure_screen_enabled")
    }
}
