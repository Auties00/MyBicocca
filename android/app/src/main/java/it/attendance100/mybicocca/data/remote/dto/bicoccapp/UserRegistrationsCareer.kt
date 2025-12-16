package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param registrations
 */


data class UserRegistrationsCareer(

    @SerializedName("registrations")
    val registrations: List<Any>? = null

)

