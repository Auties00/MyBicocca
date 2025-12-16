package it.attendance100.mybicocca.data.remote.dto.bicoccapp

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param career
 */


data class UserExams(

    @SerializedName("career")
    val career: UserExamsCareer? = null

)

