package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param fees
 */


data class UserFeesCareer(

    @SerializedName("fees")
    val fees: List<Any>? = null

)

