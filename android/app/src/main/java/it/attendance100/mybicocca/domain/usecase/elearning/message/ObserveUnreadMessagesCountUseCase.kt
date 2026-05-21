package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUnreadMessagesCountUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Int> =
        repository.observeUnreadCount(accountId)
}
