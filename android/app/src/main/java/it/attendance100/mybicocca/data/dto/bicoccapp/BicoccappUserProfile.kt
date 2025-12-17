package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserProfile(
    @SerializedName("user")
    val user: BicoccappUserProfileUser? = null,

    @SerializedName("careers")
    val careers: List<BicoccappUserProfileCareersInner> = emptyList()
)

