package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow

/** Persisted haptic-feedback preference backing the Impostazioni > Vibrazione page. */
interface HapticSettingsRepository {
    fun observeHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(enabled: Boolean)
}
