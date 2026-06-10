package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams one-shot session events — career changes detected by a refresh and re-auth requests —
 * which the account UI surfaces as banners and dialogs.
 */
class ObserveAccountEventsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<AccountEvent> = accountRepository.observeEvents()
}
