package it.attendance100.mybicocca.domain.usecase.career

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

class RefreshCareersUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId) {
        accountRepository.refreshCareers(accountId)
    }
}
