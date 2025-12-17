package it.attendance100.mybicocca.data.api.bicoccapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

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
     * Initiates the OpenID Connect authentication flow.
     *
     * This endpoint redirects the user to the University of Milano-Bicocca's
     * identity provider (IdP) for authentication. The IdP will present the
     * university's login page where users can enter their credentials.
     *
     * ## Response Behavior
     * This endpoint always returns a **302 redirect** to the university's
     * IdP login page. The redirect URL contains:
     * - `client_id`: The application's registered client identifier
     * - `redirect_uri`: The callback URL ([handleLoginCallback])
     * - `scope`: Requested OAuth scopes (typically `openid profile email`)
     * - `state`: CSRF protection token
     * - `nonce`: Replay attack prevention token
     *
     * ## Error Handling
     * - **Network errors:** Connection timeout or DNS resolution failures
     * - **Server errors:** 5xx responses indicate IdP or server issues
     *
     * @return A [Response] containing [Unit]. The response will have:
     *         - Status code 302 with `Location` header pointing to the IdP
     *         - Empty body (redirects don't contain response bodies)
     *
     * @see handleLoginCallback For processing the authentication result
     */
    @GET("auth/openid_connect")
    suspend fun initiateLogin(): Response<Unit>

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
        @Query("code") code: String? = null,
        @Query("state") state: String? = null
    ): Response<Unit>
}
