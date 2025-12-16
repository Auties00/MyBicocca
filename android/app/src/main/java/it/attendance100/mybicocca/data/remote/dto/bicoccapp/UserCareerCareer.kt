package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param averages
 * @param stats
 * @param degree
 */


data class UserCareerCareer(

    @SerializedName("averages")
    val averages: List<Any>? = null,

    @SerializedName("stats")
    val stats: Any? = null,

    @SerializedName("degree")
    val degree: Any? = null

)

