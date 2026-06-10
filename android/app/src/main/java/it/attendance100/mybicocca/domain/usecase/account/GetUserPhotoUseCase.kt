package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Resolves the student's Esse3 profile photo to a locally cached file path, downloading it on
 * first access. Backs the avatar in the profile screen and the account switcher.
 */
class GetUserPhotoUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId): String =
        accountRepository.getUserPhoto(accountId)
}
