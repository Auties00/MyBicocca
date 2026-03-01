package it.attendance100.mybicocca.data.dto.elearning

/**
 * Response returned after a login attempt.
 */
sealed interface ElearningLoginResponse {
    /**
     * Successful login response containing authentication tokens and user information.
     */
    data class Success(
        /**
         * The ws token
         */
        val wsToken: String,
    ) : ElearningLoginResponse

    /**
     * Failed login response.
     */
    data class Error(
        /**
         * The error message describing why authentication failed.
         */
        val message: String
    ) : ElearningLoginResponse
}