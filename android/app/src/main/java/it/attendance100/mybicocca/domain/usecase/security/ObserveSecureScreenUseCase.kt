package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams whether the window should be flagged secure while the app lock is enabled (hiding the
 * app from recents and blocking screenshots). The activity combines this with the master toggle.
 */
class ObserveSecureScreenUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeSecureScreenEnabled()
}
