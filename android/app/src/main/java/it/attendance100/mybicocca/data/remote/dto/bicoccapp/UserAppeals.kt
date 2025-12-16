package it.attendance100.mybicocca.data.remote.dto.bicoccapp

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param career
 */


data class UserAppeals(

    @SerializedName("career")
    val career: UserAppealsCareer? = null

)

