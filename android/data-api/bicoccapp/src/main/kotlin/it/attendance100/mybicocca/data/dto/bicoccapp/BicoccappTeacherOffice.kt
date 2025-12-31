package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappTeacherOffice(
    @SerialName("officeDescription")
    val officeDescription: String? = null
)