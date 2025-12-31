package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCareerRegistration(
    @SerialName("cdsId")
    val cdsId: Int? = null,

    @SerialName("activityId")
    val activityId: Int? = null,

    @SerialName("activityAppealId")
    val activityAppealId: Int? = null,

    @SerialName("activityItemId")
    val activityItemId: Int? = null,

    @SerialName("studentId")
    val studentId: Int? = null,

    @SerialName("appealDate")
    val appealDate: String? = null,

    @SerialName("appealDescr")
    val appealDescr: String? = null,

    @SerialName("courseDescr")
    val courseDescr: String? = null,

    @SerialName("sessionDescr")
    val sessionDescr: String? = null,

    @SerialName("typeAppealCode")
    val typeAppealCode: String? = null,

    @SerialName("position")
    val position: Int? = null,

    @SerialName("status")
    val status: String? = null
)