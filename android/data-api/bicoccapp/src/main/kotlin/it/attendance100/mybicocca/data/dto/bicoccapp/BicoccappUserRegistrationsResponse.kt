package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserRegistrationsResponse(
    @SerialName("career")
    val career: BicoccappUserRegistrationsCareer? = null
)

