package it.attendance100.mybicocca.data.remote.bicoccapp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response returned after a login attempt.
 */
sealed interface BicoccappLoginResponse {
    /**
     * Successful login response containing authentication tokens and user information.
     */
    data class Success(
        /**
         * The access token for authenticated API requests.
         */
        val accessToken: String,

        /**
         * The client identifier.
         */
        val client: String,

        /**
         * The user's preferred career ID.
         */
        val favouriteCareer: Long,

        /**
         * The user's fiscal code.
         */
        val fiscalCode: String,

        /**
         * The user's internal ID.
         */
        val id: Long,

        /**
         * The user's unique identifier (email).
         */
        val uid: String,

        /**
         * The type of user (e.g., "student").
         */
        val userType: String?,
    ) : BicoccappLoginResponse

    /**
     * Failed login response.
     */
    data class Error(
        /**
         * The error message describing why authentication failed.
         */
        val message: String
    ) : BicoccappLoginResponse
}

/**
 * Response returned after a logout attempt.
 */
@Serializable
data class BicoccappLogoutResponse(
    /**
     * Indicates if the logout was successful.
     */
    @SerialName("success")
    val success: Boolean,
)