package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    suspend operator fun invoke(accountId: AccountId, blockedUserId: Int) =
        repository.block(accountId, blockedUserId)
}
