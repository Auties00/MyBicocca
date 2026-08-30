package it.attendance100.mybicocca.data.admin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import it.attendance100.mybicocca.domain.model.admin.AdminMessage
import it.attendance100.mybicocca.domain.repository.admin.AdminMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminMessageRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AdminMessageRepository {

    private val DISMISSED_IDS = stringSetPreferencesKey("dismissed_admin_messages")
    
    private val remoteMessage = MutableStateFlow<AdminMessage?>(null)

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
        }
        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)

        Firebase.remoteConfig.addOnConfigUpdateListener(object : com.google.firebase.remoteconfig.ConfigUpdateListener {
            override fun onUpdate(configUpdate: com.google.firebase.remoteconfig.ConfigUpdate) {
                if (configUpdate.updatedKeys.contains("admin_announcement")) {
                    Firebase.remoteConfig.activate().addOnCompleteListener {
                        parseAndUpdate()
                    }
                }
            }
            override fun onError(error: com.google.firebase.remoteconfig.FirebaseRemoteConfigException) {}
        })
    }

    override fun observeMessage(): Flow<AdminMessage?> {
        return combine(
            remoteMessage,
            dataStore.data.map { it[DISMISSED_IDS] ?: emptySet() }
        ) { message, dismissedIds ->
            if (message != null && !dismissedIds.contains(message.id)) {
                message
            } else {
                null
            }
        }
    }

    override suspend fun dismissMessage(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[DISMISSED_IDS] ?: emptySet()
            prefs[DISMISSED_IDS] = current + id
        }
    }

    override suspend fun fetch() {
        try {
            Firebase.remoteConfig.fetchAndActivate().await()
            parseAndUpdate()
        } catch (e: Exception) {
            // Ignore fetch errors
        }
    }

    private fun parseAndUpdate() {
        val jsonString = Firebase.remoteConfig.getString("admin_announcement")
        if (jsonString.isNotBlank()) {
            val element = runCatching { Json.parseToJsonElement(jsonString).jsonObject }.getOrNull() ?: return
            val id = element["id"]?.jsonPrimitive?.content ?: return
            val title = element["title"]?.jsonPrimitive?.content ?: return
            val message = element["message"]?.jsonPrimitive?.content ?: return
            val show = element["show"]?.jsonPrimitive?.content?.toBoolean() ?: false
            
            if (show) {
                remoteMessage.value = AdminMessage(id, title, message)
            } else {
                remoteMessage.value = null
            }
        } else {
            remoteMessage.value = null
        }
    }
}
