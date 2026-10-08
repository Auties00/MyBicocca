package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers which accounts still owe the post-sign-in career choice. Kept on disk so that leaving
 * the picker, or the process dying on it, brings the user back to it instead of silently settling
 * for the default career.
 */
@Singleton
class CareerPickStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val pendingAccountIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[PENDING_KEY] ?: emptySet()
    }

    suspend fun setPending(accountId: String, pending: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[PENDING_KEY] ?: emptySet()
            prefs[PENDING_KEY] = if (pending) current + accountId else current - accountId
        }
    }

    private companion object {
        val PENDING_KEY = stringSetPreferencesKey("career_pick_pending_accounts")
    }
}
