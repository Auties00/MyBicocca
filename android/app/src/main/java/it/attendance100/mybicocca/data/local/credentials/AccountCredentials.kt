package it.attendance100.mybicocca.data.local.credentials

/**
 * University username/password pair as captured at sign-in (username in full e-mail form),
 * reused for silent re-authentication against every platform when a session expires.
 */
data class AccountCredentials(
    val username: String,
    val password: String,
)
