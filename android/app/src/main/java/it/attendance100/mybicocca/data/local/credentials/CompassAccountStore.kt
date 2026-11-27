package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Stores the Fresh & Natural / RistoCloud account state: the user's e-mail, the persistent Bearer
 * token returned by `POST /api/auth/v1/basic/verify`, and cached profile display fields (full name
 * and user badge/barcode). Because the Bearer token grants access to wallet balance and takeaway
 * bookings, everything lives in the encrypted preferences shared with [CredentialsStore].
 */
@Singleton
class CompassAccountStore @Inject constructor(
    @Named(CredentialsStore.EncryptedPrefsName) private val prefs: Lazy<SharedPreferences>,
) {

    suspend fun email(): String? = withContext(Dispatchers.IO) {
        prefs.get().getString(KEY_EMAIL, null)
    }

    suspend fun authToken(): String? = withContext(Dispatchers.IO) {
        prefs.get().getString(KEY_AUTH_TOKEN, null)
    }

    suspend fun userName(): String? = withContext(Dispatchers.IO) {
        prefs.get().getString(KEY_USER_NAME, null)
    }

    suspend fun barcode(): String? = withContext(Dispatchers.IO) {
        prefs.get().getString(KEY_BARCODE, null)
    }

    suspend fun setSession(
        email: String,
        authToken: String,
        userName: String? = null,
        barcode: String? = null,
    ): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            putString(KEY_EMAIL, email)
            putString(KEY_AUTH_TOKEN, authToken)
            if (userName != null) putString(KEY_USER_NAME, userName) else remove(KEY_USER_NAME)
            if (barcode != null) putString(KEY_BARCODE, barcode) else remove(KEY_BARCODE)
        }
    }

    suspend fun updateProfile(userName: String?, barcode: String?): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            if (userName != null) putString(KEY_USER_NAME, userName)
            if (barcode != null) putString(KEY_BARCODE, barcode)
        }
    }

    suspend fun updateEmail(email: String): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit { putString(KEY_EMAIL, email) }
    }

    suspend fun clearSession(): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            remove(KEY_EMAIL)
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_BARCODE)
        }
    }

    private companion object {
        const val KEY_EMAIL = "compass:email"
        const val KEY_AUTH_TOKEN = "compass:auth_token"
        const val KEY_USER_NAME = "compass:user_name"
        const val KEY_BARCODE = "compass:barcode"
    }
}
