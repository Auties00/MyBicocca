package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.AppLockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams whether the lock gate is engaged; while true the root UI hides everything behind the unlock screen. */
class ObserveAppLockUseCase @Inject constructor(
    private val repository: AppLockRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeLocked()
}
