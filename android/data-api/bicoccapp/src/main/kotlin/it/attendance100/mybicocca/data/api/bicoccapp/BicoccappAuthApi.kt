package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLogoutResponse

/** Path for initiating the OpenID Connect authentication flow. */
const val BICOCCAPP_AUTH_PATH = "/auth/openid_connect"

/**
 * Authentication API using OpenID Connect via the university's identity provider.
 */
interface BicoccappAuthApi {
    /**
     * Handles the OAuth2 callback after IdP authentication.
     * Exchanges the authorization code for access, refresh, and ID tokens.
     *
     * @param code Authorization code from the IdP (single-use).
     * @param state CSRF token to validate against the original request.
     * @param cookies Session cookies from the auth flow.
     */
    @GET("auth/openid_connect/callback")
    suspend fun handleLoginCallback(
	    @Query("code") code: String,
	    @Query("state") state: String,
	    @Header("Cookie") cookies: String
    )

    /**
     * Terminates the user session and revokes all associated tokens.
     * Client should clear local tokens regardless of response status.
     */
    @DELETE("auth/sign_out")
    suspend fun logout(): BicoccappLogoutResponse
}
