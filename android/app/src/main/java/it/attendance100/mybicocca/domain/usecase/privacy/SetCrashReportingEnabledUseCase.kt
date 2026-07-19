package it.attendance100.mybicocca.domain.usecase.privacy

import it.attendance100.mybicocca.domain.repository.PrivacySettingsRepository
import javax.inject.Inject

/** Persists the crash-reporting opt-in. */
class SetCrashReportingEnabledUseCase @Inject constructor(
    private val repository: PrivacySettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setCrashReportingEnabled(enabled)
}
