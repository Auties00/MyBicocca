package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Stores the Affluences "my reservations" account state: the per-install device id and api key
 * minted via check-in and, once the user validates an e-mail address, the session token and
 * that e-mail. The token grants access to the user's bookings, so everything lives in the
 * encrypted preferences shared with the credential store.
 */
@Singleton
class LibraryAccountStore @Inject constructor(
    @Named(CredentialsStore.EncryptedPrefsName) private val prefs: Lazy<SharedPreferences>,
) {

    /** Opaque device id sent on check-in; generated once per install and reused. */
    suspend fun deviceId(): String = withContext(Dispatchers.IO) {
        val store = prefs.get()
        store.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().replace("-", "").take(16)
            .also { store.edit { putString(KEY_DEVICE_ID, it) } }
    }

    suspend fun apiKey(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_API_KEY, null) }

    suspend fun setApiKey(value: String): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit { putString(KEY_API_KEY, value) }
    }

    suspend fun authToken(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_AUTH_TOKEN, null) }

    suspend fun email(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_EMAIL, null) }

    suspend fun setSession(email: String, authToken: String): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            putString(KEY_EMAIL, email)
            putString(KEY_AUTH_TOKEN, authToken)
        }
    }

    suspend fun clearSession(): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            remove(KEY_EMAIL)
            remove(KEY_AUTH_TOKEN)
        }
    }

    private companion object {
        const val KEY_DEVICE_ID = "affluences:device_id"
        const val KEY_API_KEY = "affluences:api_key"
        const val KEY_AUTH_TOKEN = "affluences:auth_token"
        const val KEY_EMAIL = "affluences:email"
    }
}
