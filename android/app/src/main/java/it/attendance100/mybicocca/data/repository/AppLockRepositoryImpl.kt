package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.AppLockManager
import it.attendance100.mybicocca.data.auth.AppLockVerifier
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.repository.AppLockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to the process-wide lock gate ([AppLockManager]) and the password check ([AppLockVerifier]). */
@Singleton
class AppLockRepositoryImpl @Inject constructor(
    private val appLockManager: AppLockManager,
    private val verifier: AppLockVerifier,
) : AppLockRepository {

    override fun observeLocked(): Flow<Boolean> = appLockManager.locked

    override fun unlock() = appLockManager.unlock()

    override suspend fun verifyPassword(password: String): UnlockResult =
        verifier.verifyPassword(password)
}
