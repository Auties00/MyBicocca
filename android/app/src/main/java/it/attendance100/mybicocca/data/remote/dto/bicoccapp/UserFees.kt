package it.attendance100.mybicocca.data.remote.dto.bicoccapp

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param career
 */


data class UserFees(

    @SerializedName("career")
    val career: UserFeesCareer? = null

)

