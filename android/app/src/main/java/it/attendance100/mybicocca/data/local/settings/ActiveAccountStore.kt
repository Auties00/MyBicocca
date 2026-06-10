package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed pointer to the active account id; null means nobody is signed in. The
 * session layer resolves this id against the Room account table to expose the active-account
 * stream, and clears or repoints it on sign-out.
 */
@Singleton
class ActiveAccountStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val activeAccountId: Flow<AccountId?> = dataStore.data.map { prefs ->
        prefs[ACTIVE_ACCOUNT_KEY]?.let(::AccountId)
    }

    suspend fun set(accountId: AccountId?) {
        dataStore.edit { prefs ->
            if (accountId == null) prefs.remove(ACTIVE_ACCOUNT_KEY)
            else prefs[ACTIVE_ACCOUNT_KEY] = accountId.value
        }
    }

    private companion object {
        val ACTIVE_ACCOUNT_KEY = stringPreferencesKey("active_account_id")
    }
}
