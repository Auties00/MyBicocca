package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param error
 */


data class Error(

    @SerializedName("error")
    val error: String? = null

)

