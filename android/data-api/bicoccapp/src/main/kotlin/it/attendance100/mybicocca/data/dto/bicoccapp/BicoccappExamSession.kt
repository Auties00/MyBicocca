package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappExamSession(
    @SerialName("yearFreqId")
    val yearFreqId: Int? = null,

    @SerialName("activityCode")
    val activityCode: String? = null,

    @SerialName("activityItemId")
    val activityItemId: Int? = null,

    @SerialName("activityDescr")
    val activityDescr: String? = null,

    @SerialName("courseYear")
    val courseYear: Int? = null,

    @SerialName("activityId")
    val activityId: Int? = null,

    @SerialName("activityId_o")
    val activityIdOriginal: Int? = null, // mapped from activityId_o

    @SerialName("cdsId")
    val cdsId: Int? = null,

    @SerialName("cdsId_o")
    val cdsIdOriginal: Int? = null, // mapped from cdsId_o

    @SerialName("appeals")
    val appeals: List<BicoccappAppealSession> = emptyList()
)