package it.attendance100.mybicocca.domain.model

/**
 * Complete session data extracted from the OAuth callback.
 *
 * Contains both authentication credentials and user information
 * returned by the identity provider after successful login.
 *
 * ## Callback URL Parameters
 *
 * This data is parsed from a callback URL like:
 * ```
 * https://backoffice-app.unimib.it/inappbrowser?
 *   access_token=...&client=...&uid=...&
 *   id=16474&fiscal_code=TRALSN05A12F205A&
 *   favourite_career=438589&user_type=student&
 *   user_authenticated=true
 * ```
 *
 * @property credentials Authentication tokens for API requests
 * @property userId Internal user identifier from the `id` parameter
 * @property fiscalCode User's tax code (codice fiscale)
 * @property favouriteCareer Preferred career/enrollment ID (matricola)
 * @property userType Type of user: "student", "professor", or "staff"
 * @property isAuthenticated Whether authentication was successful
 */
data class AuthSession(
    val credentials: AuthCredentials,
    val userId: Int,
    val fiscalCode: String,
    val favouriteCareer: Int,
    val userType: String,
    val isAuthenticated: Boolean
)
