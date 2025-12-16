package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param notations
 */


data class UserExamsCareer(

    @SerializedName("notations")
    val notations: List<Any>? = null

)

