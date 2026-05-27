package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CredentialsStore @Inject constructor(
    // Lazy: EncryptedSharedPreferences.create() (keystore + Tink init, tens-to-hundreds of ms) is
    // deferred to the first credential access on Dispatchers.IO below, instead of running when this is
    // constructed — which happens on the cold-start main thread via SessionManager. Credentials never
    // gate the first screen (the active account resolves from DataStore + Room), so they must not block startup.
    @Named(EncryptedPrefsName) private val prefs: Lazy<SharedPreferences>,
) {

    suspend fun save(accountId: AccountId, credentials: AccountCredentials): Unit =
        withContext(Dispatchers.IO) {
            prefs.get().edit {
                putString(usernameKey(accountId), credentials.username)
                putString(passwordKey(accountId), credentials.password)
            }
        }

    suspend fun read(accountId: AccountId): AccountCredentials? = withContext(Dispatchers.IO) {
        val store = prefs.get()
        val username = store.getString(usernameKey(accountId), null) ?: return@withContext null
        val password = store.getString(passwordKey(accountId), null) ?: return@withContext null
        AccountCredentials(username, password)
    }

    suspend fun delete(accountId: AccountId): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
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
