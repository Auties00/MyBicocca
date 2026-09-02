package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists notification preferences in the shared `mybicocca_settings` DataStore. */
@Singleton
class NotificationSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /**
     * Whether the `POST_NOTIFICATIONS` prompt has already been shown once. Android only surfaces
     * its dialog for the first two requests and silently ignores the rest, so re-asking on every
     * launch would spend that budget for nothing and leave the user with no prompt on the one
     * occasion they'd have said yes. Asked once; after that it's a settings trip.
     */
    val permissionAsked: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PERMISSION_ASKED_KEY] ?: false
    }

    suspend fun setPermissionAsked() {
        dataStore.edit { prefs -> prefs[PERMISSION_ASKED_KEY] = true }
    }

    private companion object {
        val PERMISSION_ASKED_KEY = booleanPreferencesKey("notification_permission_asked")
    }
}
