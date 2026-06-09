package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.security.UnlockResult
import kotlinx.coroutines.flow.Flow

interface AppLockRepository {
    fun observeLocked(): Flow<Boolean>
    fun unlock()
    suspend fun verifyPassword(password: String): UnlockResult
}
