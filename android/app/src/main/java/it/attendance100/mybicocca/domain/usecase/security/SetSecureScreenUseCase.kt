package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import javax.inject.Inject

/** Persists the secure-window opt-in picked in the Sicurezza page. */
class SetSecureScreenUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setSecureScreenEnabled(enabled)
}
