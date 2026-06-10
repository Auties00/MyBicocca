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

/**
 * Stores each account's university credentials in EncryptedSharedPreferences (keystore-backed),
 * keyed by account id, so expired sessions can be re-established silently.
 *
 * The preferences are injected lazily because `EncryptedSharedPreferences.create()` runs
 * keystore and Tink initialization that costs tens to hundreds of milliseconds, while this
 * store is constructed on the cold-start main thread via SessionManager. Deferring creation to
 * the first credential access — always on Dispatchers.IO — keeps it off the startup path;
 * credentials never gate the first screen, which resolves the active account from DataStore
 * and Room.
 */
@Singleton
class CredentialsStore @Inject constructor(
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
