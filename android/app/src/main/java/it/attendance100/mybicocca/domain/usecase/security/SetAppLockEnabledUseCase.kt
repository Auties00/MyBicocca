package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import javax.inject.Inject

/**
 * Persists the app-lock master toggle. Callers that just authenticated should also invoke
 * [UnlockAppUseCase] so enabling the lock does not immediately re-challenge the user.
 */
class SetAppLockEnabledUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAppLockEnabled(enabled)
}
