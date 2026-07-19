package it.attendance100.mybicocca.domain.usecase.privacy

import it.attendance100.mybicocca.domain.repository.PrivacySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams whether Crashlytics collection is enabled, for the toggle in the security sheet. */
class ObserveCrashReportingEnabledUseCase @Inject constructor(
    private val repository: PrivacySettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeCrashReportingEnabled()
}
