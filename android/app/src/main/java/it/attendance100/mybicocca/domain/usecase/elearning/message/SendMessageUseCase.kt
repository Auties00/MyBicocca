package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.MessageId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    suspend operator fun invoke(accountId: AccountId, toUserId: Int, text: String): MessageId =
        repository.send(accountId, toUserId, text)
}
