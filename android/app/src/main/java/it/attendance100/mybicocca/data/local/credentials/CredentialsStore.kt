package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import androidx.core.content.edit
import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CredentialsStore @Inject constructor(
    @Named(EncryptedPrefsName) private val prefs: SharedPreferences,
) {

    suspend fun save(accountId: AccountId, credentials: AccountCredentials): Unit =
        withContext(Dispatchers.IO) {
            prefs.edit {
                putString(usernameKey(accountId), credentials.username)
                putString(passwordKey(accountId), credentials.password)
            }
        }

    suspend fun read(accountId: AccountId): AccountCredentials? = withContext(Dispatchers.IO) {
        val username = prefs.getString(usernameKey(accountId), null) ?: return@withContext null
        val password = prefs.getString(passwordKey(accountId), null) ?: return@withContext null
        AccountCredentials(username, password)
    }

    suspend fun delete(accountId: AccountId): Unit = withContext(Dispatchers.IO) {
        prefs.edit {
            remove(usernameKey(accountId))
            remove(passwordKey(accountId))
        }
    }

    private fun usernameKey(id: AccountId) = "creds:${id.value}:username"
    private fun passwordKey(id: AccountId) = "creds:${id.value}:password"

    companion object {
        const val EncryptedPrefsName = "encrypted_prefs"
    }
}
