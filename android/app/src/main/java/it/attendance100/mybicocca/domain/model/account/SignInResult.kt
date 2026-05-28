package it.attendance100.mybicocca.domain.model.account

sealed interface SignInResult {
    data class Success(
        val account: Account,
        val requiresCareerPick: Boolean,
    ) : SignInResult

    data class Failure(
        val reason: SignInFailure,
    ) : SignInResult
}
