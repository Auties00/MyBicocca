package it.attendance100.mybicocca.domain.model.account

/**
 * Classified outcome of a failed sign-in attempt.
 *
 * Sign-in runs Esse3 and Elearning logins in parallel and either path can fail
 * with backend-specific exception types. The data layer collapses those raw
 * throwables into one of these cases at the boundary so the UI never has to
 * reach into data-api types to render a message.
 */
sealed interface SignInFailure {
    /** At least one backend returned HTTP 401 — username or password is wrong. */
    data object BadCredentials : SignInFailure

    /** No usable network on either path (DNS failure, connection reset, etc.). */
    data object NoConnection : SignInFailure

    /** Anything else — backend outage, unexpected SSO response shape, server bug. */
    data object Unknown : SignInFailure
}
