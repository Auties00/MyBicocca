package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCareerStats(
    @SerialName("valueCode")
    val valueCode: String? = null,

    @SerialName("valueDescr")
    val valueDescr: String? = null,

    @SerialName("examsDone")
    val examsDone: Int? = null,

    @SerialName("totalToDo")
    val totalToDo: Double? = null,

    @SerialName("totalDone")
    val totalDone: Double? = null
)