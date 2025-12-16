package it.attendance100.mybicocca.data.remote.dto.bicoccapp

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param user
 * @param careers
 */


data class UserProfile(

    @SerializedName("user")
    val user: UserProfileUser? = null,

    @SerializedName("careers")
    val careers: List<UserProfileCareersInner>? = null

)

