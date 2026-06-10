package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.repository.AppLockRepository
import javax.inject.Inject

/** Disarms the lock gate after a successful biometric or password challenge, dropping the locked stream to false. */
class UnlockAppUseCase @Inject constructor(
    private val repository: AppLockRepository,
) {
    operator fun invoke() = repository.unlock()
}
