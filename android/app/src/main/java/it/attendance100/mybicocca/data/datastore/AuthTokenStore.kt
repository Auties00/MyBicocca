package it.attendance100.mybicocca.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "auth_tokens",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var esse3BasicAuth: String?
        get() = prefs.getString(KEY_ESSE3_BASIC_AUTH, null)
        set(value) = prefs.edit().putString(KEY_ESSE3_BASIC_AUTH, value).apply()

    var elearningWsToken: String?
        get() = prefs.getString(KEY_ELEARNING_WS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ELEARNING_WS_TOKEN, value).apply()

    var esse3PersonId: Long
        get() = prefs.getLong(KEY_ESSE3_PERSON_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ESSE3_PERSON_ID, value).apply()

    var esse3UserId: Long
        get() = prefs.getLong(KEY_ESSE3_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ESSE3_USER_ID, value).apply()

    var elearningUserId: Int
        get() = prefs.getInt(KEY_ELEARNING_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_ELEARNING_USER_ID, value).apply()

    val isLoggedIn: Boolean
        get() = esse3BasicAuth != null

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ESSE3_BASIC_AUTH = "esse3_basic_auth"
        const val KEY_ELEARNING_WS_TOKEN = "elearning_ws_token"
        const val KEY_ESSE3_PERSON_ID = "esse3_person_id"
        const val KEY_ESSE3_USER_ID = "esse3_user_id"
        const val KEY_ELEARNING_USER_ID = "elearning_user_id"
    }
}
