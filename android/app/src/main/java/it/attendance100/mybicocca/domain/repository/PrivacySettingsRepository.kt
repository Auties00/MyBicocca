package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persisted privacy preferences, currently just the crash-reporting opt-out surfaced in Impostazioni > Sicurezza.
 * The observe method is a hot flow over the settings DataStore.
 * Crash reporting is on by default; applying the choice to the Crashlytics SDK is owned by the observability layer, not this repository.
 */
interface PrivacySettingsRepository {
    fun observeCrashReportingEnabled(): Flow<Boolean>
    suspend fun setCrashReportingEnabled(enabled: Boolean)
}
