package it.attendance100.mybicocca.data.remote.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Authentication API
 *
 * Handles authentication, password resets, and token management for the Elearning platform.
 *
 * ## Key Features
 *
 * - **QR Login:** Retrieve tokens for QR code login flow.
 * - **Auto-login:** Generate keys for auto-login URLs.
 * - **Subscription:** Validate subscription keys.
 * - **Account Management:** Check minor status, reset password, resend confirmation email.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get tokens for QR login
 * val tokens = authApi.getTokensForQrLogin(
 *     QrLoginRequest(qrLoginKey = "...", userId = 123)
 * )
 * ```
 */
interface ElearningAuthApi {

    /**
     * Returns a WebService token (and private token) for QR login.
     *
     * @param request Request containing the QR login key and user ID.
     * @return A [Response] containing the tokens.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_get_tokens_for_qr_login")
    suspend fun getTokensForQrLogin(@Body request: QrLoginRequest): Response<QrLoginResponse>

    /**
     * Creates an auto-login key for the current user.
     *
     * @param request Request containing private token.
     * @return A [Response] containing the auto-login key and URL.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_get_autologin_key")
    suspend fun getAutologinKey(@Body request: AutologinKeyRequest): Response<AutologinKeyResponse>

    /**
     * Validates a subscription key.
     *
     * @param request Request containing the subscription key.
     * @return A [Response] containing validation status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_validate_subscription_key")
    suspend fun validateSubscriptionKey(@Body request: ValidateSubscriptionKeyRequest): Response<ValidateSubscriptionKeyResponse>

    /**
     * Checks if the user is considered a minor.
     *
     * @param request Request containing age and country.
     * @return A [Response] containing minor status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_auth_is_minor")
    suspend fun isMinor(@Body request: CheckMinorRequest): Response<CheckMinorResponse>

    /**
     * Requests a password reset.
     *
     * @param request Request containing username or email.
     * @return A [Response] containing request status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_auth_request_password_reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<PasswordResetResponse>

    /**
     * Resends the confirmation email.
     *
     * @param request Request containing username and password.
     * @return A [Response] containing status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_auth_resend_confirmation_email")
    suspend fun resendConfirmationEmail(@Body request: ResendConfirmationEmailRequest): Response<ResendConfirmationEmailResponse>
}
