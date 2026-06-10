package it.attendance100.mybicocca.data.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import it.attendance100.mybicocca.data.local.credentials.CredentialsStore
import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * In-memory plus encrypted-preferences cache of per-account session tokens. Persistence is
 * best-effort: every token is recoverable through a fresh login, so the cache exists only to
 * skip the SAML / login dance on warm starts and across process restarts.
 *
 * The preferences instance is shared with [CredentialsStore] and injected lazily; deferring
 * `get()` to the first access — always on Dispatchers.IO — keeps the expensive
 * `EncryptedSharedPreferences.create()` off the cold-start main thread. A single mutex
 * serializes all reads and writes.
 */
@Singleton
class SessionCache @Inject constructor(
    @Named(CredentialsStore.EncryptedPrefsName) private val prefs: Lazy<SharedPreferences>,
) {

    private val mutex = Mutex()
    private val tokens = mutableMapOf<AccountId, SessionTokens>()

    suspend fun read(accountId: AccountId): SessionTokens = mutex.withLock { loadLocked(accountId) }

    suspend fun update(
        accountId: AccountId,
        transform: SessionTokens.() -> SessionTokens,
    ): SessionTokens = mutex.withLock {
        val updated = loadLocked(accountId).transform()
        tokens[accountId] = updated
        withContext(Dispatchers.IO) {
            prefs.get().edit {
                writeString(wsTokenKey(accountId), updated.wsToken)
                writeString(moodleSessionCookieKey(accountId), updated.moodleSessionCookie)
                writeString(jwtKey(accountId), updated.jwt)
                writeString(esse3LegacyCookiesKey(accountId), updated.esse3LegacyCookies)
            }
        }
        updated
    }

    suspend fun delete(accountId: AccountId): Unit = mutex.withLock {
        tokens.remove(accountId)
        withContext(Dispatchers.IO) {
            prefs.get().edit {
                remove(wsTokenKey(accountId))
                remove(moodleSessionCookieKey(accountId))
                remove(jwtKey(accountId))
                remove(esse3LegacyCookiesKey(accountId))
            }
        }
    }

    private suspend fun loadLocked(accountId: AccountId): SessionTokens {
        tokens[accountId]?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            val store = prefs.get()
            SessionTokens(
                wsToken = store.getString(wsTokenKey(accountId), null),
                moodleSessionCookie = store.getString(moodleSessionCookieKey(accountId), null),
                jwt = store.getString(jwtKey(accountId), null),
                esse3LegacyCookies = store.getString(esse3LegacyCookiesKey(accountId), null),
            )
        }
        tokens[accountId] = loaded
        return loaded
    }

    private fun SharedPreferences.Editor.writeString(key: String, value: String?) {
        if (value == null) remove(key) else putString(key, value)
    }

    private fun wsTokenKey(id: AccountId) = "session:${id.value}:wsToken"
    private fun moodleSessionCookieKey(id: AccountId) = "session:${id.value}:moodleSessionCookie"
    private fun jwtKey(id: AccountId) = "session:${id.value}:jwt"
    private fun esse3LegacyCookiesKey(id: AccountId) = "session:${id.value}:esse3LegacyCookies"
}
