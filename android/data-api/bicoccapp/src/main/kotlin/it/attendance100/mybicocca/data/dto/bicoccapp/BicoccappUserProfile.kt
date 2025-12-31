package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserProfile(
    @SerialName("user")
    val user: BicoccappUserProfileUser? = null,

    @SerialName("careers")
    val careers: List<BicoccappUserProfileCareersInner> = emptyList()
)

