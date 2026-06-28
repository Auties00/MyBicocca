package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.HapticSettingsStore
import it.attendance100.mybicocca.domain.repository.HapticSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [HapticSettingsStore], the DataStore wrapper owning the persisted key. */
@Singleton
class HapticSettingsRepositoryImpl @Inject constructor(
    private val store: HapticSettingsStore,
) : HapticSettingsRepository {

    override fun observeHapticsEnabled(): Flow<Boolean> = store.hapticsEnabled

    override suspend fun setHapticsEnabled(enabled: Boolean) = store.setHapticsEnabled(enabled)
}
