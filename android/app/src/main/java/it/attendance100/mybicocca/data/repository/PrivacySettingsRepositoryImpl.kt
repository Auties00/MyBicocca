package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.PrivacySettingsStore
import it.attendance100.mybicocca.domain.repository.PrivacySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [PrivacySettingsStore], the DataStore wrapper owning the persisted keys. */
@Singleton
class PrivacySettingsRepositoryImpl @Inject constructor(
    private val store: PrivacySettingsStore,
) : PrivacySettingsRepository {

    override fun observeCrashReportingEnabled(): Flow<Boolean> = store.crashReportingEnabled

    override suspend fun setCrashReportingEnabled(enabled: Boolean) =
        store.setCrashReportingEnabled(enabled)
}
