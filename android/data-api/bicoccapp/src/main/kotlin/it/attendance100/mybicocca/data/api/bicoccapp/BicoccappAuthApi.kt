package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLogoutResponse

/**
 * Initiates the OpenID Connect authentication flow.
 *
 * This endpoint redirects the user to the University of Milano-Bicocca's
 * identity provider (IdP) for authentication. The IdP will present the
 * university's login page where users can enter their credentials.
 *
 * @return A string that represents the URL of the auth provider
 *
 * @see handleLoginCallback For processing the authentication result
 */

const val BICOCCAPP_AUTH_URL = "/auth/openid_connect"

/**
 * # BicoccApp Authentication API
 *
 * This interface defines the endpoints for handling user authentication
 * via OpenID Connect protocol. The authentication flow uses the university's
 * identity provider (IdP) for Single Sign-On (SSO).
 *
 * ## Authentication Flow
 *
 * 1. Client calls [initiateLogin] to start the authentication process
 * 2. User is redirected to the university's IdP login page
 * 3. After successful login, IdP redirects back to [handleLoginCallback]
 * 4. The callback endpoint exchanges the authorization code for tokens
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Step 1: Initiate login (will return 302 redirect)
 * val loginResponse = authApi.initiateLogin()
 *
 * // Step 2: Handle the callback after user authenticates
 * val tokenResponse = authApi.handleLoginCallback(
 *     code = "authorization_code_from_idp",
 *     state = "csrf_state_token"
 * )
 * ```
 */
interface BicoccappAuthApi {
    /**
     * Handles the OAuth2/OpenID Connect callback from the identity provider.
     *
     * After the user successfully authenticates with the university's IdP,
     * they are redirected back to this endpoint with an authorization code.
     * This endpoint exchanges the code for access and refresh tokens.
     *
     * ## Token Exchange
     * The authorization code received is exchanged for:
     * - **Access Token:** Short-lived token for API requests
     * - **Refresh Token:** Long-lived token to obtain new access tokens
     * - **ID Token:** Contains user identity claims (JWT format)
     *
     * ## Security Considerations
     * - The `state` parameter **must** match the one sent in [initiateLogin]
     *   to prevent CSRF attacks
     * - Authorization codes are single-use and expire quickly (typically 10 minutes)
     * - Always validate the `state` parameter before processing the response
     *
     * @param code The authorization code issued by the identity provider.
     *             This single-use code is exchanged for tokens.
     *             Will be `null` if authentication failed.
     *
     * @param state The CSRF protection token that was originally sent with
     *              the authorization request. Must be validated against the
     *              stored state to prevent cross-site request forgery attacks.
     *              Will be `null` if authentication failed.
     *
     * @return A [Response] containing [Unit]. The response will be a 302 redirect:
     *         - On success: Redirects to the app with tokens in cookies/headers
     *         - On failure: Redirects to an error page or login screen
     *
     * @see initiateLogin For starting the authentication flow
     */
    @GET("auth/openid_connect/callback")
    suspend fun handleLoginCallback(
	    @Query("code") code: String,
	    @Query("state") state: String,
	    @Header("Cookie") cookies: String
    ): Response<Unit>

    /**
     * Terminates the user's authenticated session.
     *
     * This endpoint invalidates the current user's session on the server side,
     * effectively logging them out of the BicoccApp platform. After a successful
     * logout, all associated tokens (access, refresh, and ID tokens) are revoked
     * and can no longer be used for API requests.
     *
     * ## Session Termination
     * The logout process performs the following actions:
     * - Invalidates the server-side session
     * - Revokes all active tokens associated with the session
     * - Clears any session cookies set by the server
     *
     * ## Client-Side Cleanup
     * After receiving a successful response, the client application should:
     * - Clear any locally stored tokens (access, refresh, ID tokens)
     * - Clear cached user profile information
     * - Navigate the user to the login screen or public area
     *
     * ## Error Handling
     * - **401 Unauthorized:** Session already expired or invalid
     * - **Network errors:** Connection timeout or DNS resolution failures
     * - **Server errors:** 5xx responses indicate server-side issues
     *
     * Even if the request fails, the client should still perform local cleanup
     * to ensure the user is logged out on the device.
     *
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLogoutResponse] with:
     *         - Status code 200 on successful logout
     *         - Confirmation of session termination in the response body
     *
     * @see initiateLogin For starting a new authentication flow after logout
     * @see handleLoginCallback For the complete authentication process
     */
    @DELETE("auth/sign_out")
    suspend fun logout(): Response<BicoccappLogoutResponse>
}
