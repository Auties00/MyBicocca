package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserRegistrationsCareer(
    @SerialName("registrations")
    val registrations: List<BicoccappCareerRegistration> = emptyList()
)