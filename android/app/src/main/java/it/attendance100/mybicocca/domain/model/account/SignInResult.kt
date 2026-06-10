package it.attendance100.mybicocca.domain.model.account

/**
 * Outcome of a sign-in attempt, returned (never thrown) by the data layer and rendered by the
 * auth screen.
 */
sealed interface SignInResult {
    /**
     * Both platform logins succeeded; the account is saved and active.
     *
     * @property requiresCareerPick True when more than one career is selectable, in which case
     *   the auth flow routes to the career picker before entering the app.
     */
    data class Success(
        val account: Account,
        val requiresCareerPick: Boolean,
    ) : SignInResult

    /** Sign-in did not complete; [reason] carries the classified cause. */
    data class Failure(
        val reason: SignInFailure,
    ) : SignInResult
}
