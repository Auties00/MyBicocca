package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Stable per-install identifier sent as the EasyBadge `device_token` when
// certifying presence. Generated once and persisted in the shared settings store.
@Singleton
class DeviceIdentityStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun deviceId(): String {
        dataStore.data.map { it[KEY] }.first()?.let { return it }
        val generated = UUID.randomUUID().toString()
        dataStore.edit { prefs -> prefs[KEY] = prefs[KEY] ?: generated }
        return dataStore.data.map { it[KEY] }.first() ?: generated
    }

    private companion object {
        val KEY = stringPreferencesKey("easybadge_device_id")
    }
}
