package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardDegreesResponse(
    @SerialName("degrees")
    val degrees: List<BicoccappWizardDegree> = emptyList()
)

