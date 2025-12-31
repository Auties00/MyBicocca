package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCareerAverage(
    @SerialName("base")
    val base: Int? = null,

    @SerialName("basedefinition")
    val baseDefinition: String? = null,

    @SerialName("weighted")
    val weighted: Double? = null,

    @SerialName("arithmetic")
    val arithmetic: Double? = null
)