package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(username: String, password: String): SignInResult =
        accountRepository.signIn(username, password)
}
