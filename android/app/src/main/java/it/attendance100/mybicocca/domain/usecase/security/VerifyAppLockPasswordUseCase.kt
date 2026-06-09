package it.attendance100.mybicocca.domain.usecase.security

import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.repository.AppLockRepository
import javax.inject.Inject

class VerifyAppLockPasswordUseCase @Inject constructor(
    private val repository: AppLockRepository,
) {
    suspend operator fun invoke(password: String): UnlockResult =
        repository.verifyPassword(password)
}
