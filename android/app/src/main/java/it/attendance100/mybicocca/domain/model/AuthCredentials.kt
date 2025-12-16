package it.attendance100.mybicocca.domain.model

/**
 * Authentication credentials required for BicoccApp API requests.
 *
 * These values are extracted from the OAuth callback URL and must be
 * included as headers in all authenticated API requests:
 * - `access-token`: [accessToken]
 * - `client`: [client]
 * - `uid`: [uid]
 *
 * @property accessToken The access token for API authorization
 * @property client The client identifier issued by the auth server
 * @property uid The user identifier (typically the institutional email)
 */
data class AuthCredentials(
    val accessToken: String,
    val client: String,
    val uid: String
)
