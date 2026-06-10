package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.SecuritySettingsStore
import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [SecuritySettingsStore], the DataStore wrapper owning the persisted keys. */
@Singleton
class SecuritySettingsRepositoryImpl @Inject constructor(
    private val store: SecuritySettingsStore,
) : SecuritySettingsRepository {

    override fun observeAppLockEnabled(): Flow<Boolean> = store.appLockEnabled

    override fun observeLockTimeoutMinutes(): Flow<Int> = store.lockTimeoutMinutes

    override fun observeSecureScreenEnabled(): Flow<Boolean> = store.secureScreenEnabled

    override suspend fun setAppLockEnabled(enabled: Boolean) = store.setAppLockEnabled(enabled)

    override suspend fun setLockTimeoutMinutes(minutes: Int) = store.setLockTimeoutMinutes(minutes)

    override suspend fun setSecureScreenEnabled(enabled: Boolean) = store.setSecureScreenEnabled(enabled)
}
