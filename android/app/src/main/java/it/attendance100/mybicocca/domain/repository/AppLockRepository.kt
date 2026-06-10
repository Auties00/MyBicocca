package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.security.UnlockResult
import kotlinx.coroutines.flow.Flow

/**
 * The app-lock gate: a soft, local lock that hides the signed-in UI behind a biometric or
 * password challenge on cold start and after the configured idle timeout. Its user-facing
 * configuration lives in [SecuritySettingsRepository]; this contract exposes the live gate
 * state and the ways through it.
 */
interface AppLockRepository {
    fun observeLocked(): Flow<Boolean>

    /** Disarms the gate after a successful challenge (also called right after enabling the lock, so it does not immediately re-challenge). */
    fun unlock()

    /** Verifies the password fallback against the active account's stored sign-in credential. */
    suspend fun verifyPassword(password: String): UnlockResult
}
