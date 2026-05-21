package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.account.SignInResult
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    val activeAccount: Flow<Account?>

    val accounts: Flow<List<Account>>

    val events: Flow<AccountEvent>

    suspend fun signIn(username: String, password: String): SignInResult

    suspend fun signOut(accountId: AccountId)

    suspend fun switchAccount(accountId: AccountId)

    suspend fun selectCareer(accountId: AccountId, careerId: CareerId)

    suspend fun refreshCareers(accountId: AccountId)

    suspend fun getUserPhoto(accountId: AccountId): String
}
