package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardDegree(
    @SerialName("name")
    val name: String? = null,

    @SerialName("code")
    val code: String? = null,
)