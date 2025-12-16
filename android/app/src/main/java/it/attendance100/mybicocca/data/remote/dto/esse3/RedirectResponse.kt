package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * HTTP 302 redirect response
 *
 * @param location Redirect URL
 */


data class RedirectResponse(

    /* Redirect URL */
    @SerializedName("location")
    val location: java.net.URI? = null

)

