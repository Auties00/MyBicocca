package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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