package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persisted app-lock preferences backing the Impostazioni > Sicurezza page and the lock screen.
 *
 * The observe methods are hot flows over the settings DataStore. The lock state machine itself
 * lives behind [AppLockRepository]; this repository only owns the user's configuration of it:
 * the master toggle, the re-lock timeout, and whether the window is flagged secure while the
 * lock is enabled (blocking screenshots and the recents thumbnail — off by default).
 */
interface SecuritySettingsRepository {
    fun observeAppLockEnabled(): Flow<Boolean>

    /** Minutes the app may stay backgrounded before it re-locks; `0` locks immediately. */
    fun observeLockTimeoutMinutes(): Flow<Int>
    fun observeSecureScreenEnabled(): Flow<Boolean>
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setLockTimeoutMinutes(minutes: Int)
    suspend fun setSecureScreenEnabled(enabled: Boolean)
}
