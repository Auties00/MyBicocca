package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.account.SignInResult
import kotlinx.coroutines.flow.Flow

/**
 * Saved-accounts contract: sign-in and sign-out, account and career switching, and hot streams
 * of the locally persisted account set. The flows emit from the local source of truth (Room
 * plus the active-account preference) and never perform network work; suspend functions that
 * reach the network throw on failure unless documented otherwise.
 */
interface AccountRepository {

    /** Streams the currently active account, null when nobody is signed in. */
    fun observeActiveAccount(): Flow<Account?>

    /** Streams every saved account, most recently used first. */
    fun observeAccounts(): Flow<List<Account>>

    /** Streams one-shot session events (career reconciliation, re-auth requests); hot, without replay. */
    fun observeEvents(): Flow<AccountEvent>

    /**
     * Signs in against Esse3 and Moodle and persists the account on success. Failures come
     * back as a classified [SignInResult.Failure] rather than being thrown.
     */
    suspend fun signIn(username: String, password: String): SignInResult

    /** Removes the account together with its credentials, cached sessions, and cached data. */
    suspend fun signOut(accountId: AccountId)

    /** Makes the given saved account the active one. */
    suspend fun switchAccount(accountId: AccountId)

    /** Marks the career as the account's selected one; the career must belong to the account. */
    suspend fun selectCareer(accountId: AccountId, careerId: CareerId)

    /** Replaces the cached career list with a fresh Esse3 snapshot; emits [events] on changes. */
    suspend fun refreshCareers(accountId: AccountId)

    /**
     * Returns the path of a locally cached copy of the student's Esse3 profile photo,
     * downloading it on first access.
     */
    suspend fun getUserPhoto(accountId: AccountId): String
}
