package it.attendance100.mybicocca.data.remote.api.esse3

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # Esse3 Authentication API
 *
 * This interface defines the endpoints for handling user authentication
 * and session management within the Esse3 system.
 *
 * ## Authentication Flow
 *
 * 1.  **Access Home/Login Page:** Call [getLogin] to initiate the session and
 *     redirect to the identity provider (IdP).
 * 2.  **IdP Interaction:** The user authenticates via the external IdP (Shibboleth/SAML).
 * 3.  **SAML Response:** The IdP posts a SAML response to [postShibbolethSaml].
 * 4.  **Session Establishment:** Upon successful SAML validation, the Esse3 session
 *     cookie is established.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Step 1: Access login page to start flow
 * val loginResponse = authApi.getLogin()
 *
 * // Step 2: Handle SAML POST (usually automated by web view or specialized handler)
 * val samlResponse = authApi.postShibbolethSaml(
 *     relayState = "...",
 *     samlResponse = "..."
 * )
 * ```
 */
interface Esse3AuthApi {

    /**
     * Accesses the Esse3 Home page.
     *
     * This endpoint is often used to check if a valid session exists or to
     * keep the session alive.
     *
     * @param menuOpenedCod Optional menu code to open a specific section.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/Home.do")
    suspend fun getHome(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Initiates the login process.
     *
     * Accessing this endpoint typically triggers a redirect to the configured
     * Identity Provider (e.g., Shibboleth) if the user is not already authenticated.
     *
     * @param menuOpenedCod Optional menu code to navigate to after login.
     * @return A [Response] containing [Unit] (usually a 302 Redirect).
     */
    @GET("auth/Logon.do")
    suspend fun getLogin(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Root endpoint of the Esse3 system.
     *
     * Can be used for connectivity checks or initial handshake.
     *
     * @return A [Response] containing the HTML content of the root page.
     */
    @GET("Root.do")
    suspend fun getRoot(): Response<String>

    /**
     * Consumes the SAML Response from the Identity Provider.
     *
     * This is the assertion consumer service (ACS) endpoint for Shibboleth/SAML.
     * It validates the signed SAML response provided by the IdP and establishes
     * the user's session.
     *
     * @param relayState The state parameter preserved during the SSO flow.
     * @param samlResponse The Base64-encoded SAML assertion XML.
     * @return A [Response] containing [Unit]. On success, this sets the session cookie.
     */
    @FormUrlEncoded
    @POST("Shibboleth.sso/SAML2/POST")
    suspend fun postShibbolethSaml(
        @Field("RelayState") relayState: String? = null,
        @Field("SAMLResponse") samlResponse: String? = null
    ): Response<Unit>
}
