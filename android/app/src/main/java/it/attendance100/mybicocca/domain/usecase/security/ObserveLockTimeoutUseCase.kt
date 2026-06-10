package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the re-lock timeout in minutes (`0` = lock as soon as the app is backgrounded). */
class ObserveLockTimeoutUseCase @Inject constructor(
    private val repository: SecuritySettingsRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeLockTimeoutMinutes()
}
