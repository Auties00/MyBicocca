package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserCareerCareer(
    @SerialName("averages")
    val averages: List<BicoccappCareerAverage> = emptyList(),

    @SerialName("stats")
    val stats: BicoccappCareerStats? = null,
)

