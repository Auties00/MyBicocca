package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param code Error code
 * @param message Error message
 * @param details Additional error details
 */


data class Error(

    /* Error code */
    @SerializedName("code")
    val code: String? = null,

    /* Error message */
    @SerializedName("message")
    val message: String? = null,

    /* Additional error details */
    @SerializedName("details")
    val details: String? = null

)

