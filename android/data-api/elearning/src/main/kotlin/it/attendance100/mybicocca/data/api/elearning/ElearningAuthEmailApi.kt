package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.dto.elearning.ElearningAuthEmailGetSignupSettings200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningAuthEmailSignupUser200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningAuthEmailSignupUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthEmailApi {
    /**
     * POST auth_email_get_signup_settings
     * Get the signup required settings and profile fields.
     * Get the signup required settings and profile fields.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @return [ElearningAuthEmailGetSignupSettings200Response]
     */
    @POST("auth_email_get_signup_settings")
    suspend fun authEmailGetSignupSettings(): Response<ElearningAuthEmailGetSignupSettings200Response>

    /**
     * POST auth_email_signup_user
     * Adds a new user (pendingto be confirmed) in the site.
     * Adds a new user (pendingto be confirmed) in the site.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningAuthEmailSignupUserRequest 
     * @return [ElearningAuthEmailSignupUser200Response]
     */
    @POST("auth_email_signup_user")
    suspend fun authEmailSignupUser(@Body elearningAuthEmailSignupUserRequest: ElearningAuthEmailSignupUserRequest): Response<ElearningAuthEmailSignupUser200Response>

}
