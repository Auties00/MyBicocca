package it.attendance100.mybicocca.domain.usecase.career

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

// Same wire as SwitchCareerUseCase but split for call-site clarity (first-time pick after
// sign-in vs. switching from the in-app selector). Keep them separate so telemetry and
// future flow forks don't conflate the two intents.
class PickCareerUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId, careerId: CareerId) {
        accountRepository.selectCareer(accountId, careerId)
    }
}
