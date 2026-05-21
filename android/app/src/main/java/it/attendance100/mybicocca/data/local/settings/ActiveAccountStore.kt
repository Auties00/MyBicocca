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
