package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.AppLockRepository
import javax.inject.Inject

class UnlockAppUseCase @Inject constructor(
    private val repository: AppLockRepository,
) {
    operator fun invoke() = repository.unlock()
}
