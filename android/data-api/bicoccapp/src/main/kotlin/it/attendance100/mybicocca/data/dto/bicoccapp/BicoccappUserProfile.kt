package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserProfile(
    @SerializedName("user")
    val user: it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserProfileUser? = null,

    @SerializedName("careers")
    val careers: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserProfileCareersInner> = emptyList()
)

