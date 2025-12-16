package it.attendance100.mybicocca.data.remote.dto.bicoccapp

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param career
 */


data class UserCareer(

    @SerializedName("career")
    val career: UserCareerCareer? = null

)

