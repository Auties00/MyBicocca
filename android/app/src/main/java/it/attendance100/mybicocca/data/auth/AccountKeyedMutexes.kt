package it.attendance100.mybicocca.data.auth

import it.attendance100.mybicocca.domain.model.account.AccountId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

// Per-account lock — prevents concurrent calls on the same account from triggering
// duplicate auth dances when a session goes stale.
@Singleton
class AccountKeyedMutexes @Inject constructor() {

    private val parent = Mutex()
    private val mutexes = mutableMapOf<AccountId, Mutex>()

    suspend fun <T> withLock(accountId: AccountId, action: suspend () -> T): T {
        val mutex = parent.withLock { mutexes.getOrPut(accountId) { Mutex() } }
        return mutex.withLock { action() }
    }

    suspend fun forget(accountId: AccountId) {
        parent.withLock { mutexes.remove(accountId) }
    }
}
