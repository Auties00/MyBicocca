package it.attendance100.mybicocca.data.remote.elearning.dto

/**
 * Response returned after a login attempt.
 */
sealed interface ElearningLoginResponse {
    /**
     * Successful login response containing authentication tokens and user information.
     */
    data class Success(
        /**
         * The standard Moodle web service token, used for all `webservice/rest/server.php` calls.
         */
        val wsToken: String,
        /**
         * The `MoodleSession` browser cookie value captured at the end of the SAML flow.
         *
         * Callers should persist it and hand it back to [it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi.resume]
         * on subsequent process starts so that web-scope endpoints (e.g. `mod/kalvidres/view.php`)
         * keep working without re-running the SAML round-trip every time.
         *
         * `null` if the cookie was not present after login (defensive — should not happen in
         * practice on stock Moodle deployments).
         */
        val moodleSessionCookie: String?,
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