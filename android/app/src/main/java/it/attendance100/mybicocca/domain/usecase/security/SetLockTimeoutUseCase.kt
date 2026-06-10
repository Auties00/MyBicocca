package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import javax.inject.Inject

/** Persists the re-lock timeout in minutes picked in the Sicurezza page. */
class SetLockTimeoutUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    suspend operator fun invoke(minutes: Int) = repository.setLockTimeoutMinutes(minutes)
}
