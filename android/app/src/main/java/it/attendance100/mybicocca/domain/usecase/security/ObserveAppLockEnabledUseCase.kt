package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the app-lock master toggle; read by the Sicurezza page and the activity's secure-window logic. */
class ObserveAppLockEnabledUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeAppLockEnabled()
}
