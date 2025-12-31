package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCourse(
    @SerialName("cdsCode")
    val cdsCode: String? = null,

    @SerialName("activityCode")
    val activityCode: String? = null,

    @SerialName("lessonName")
    val lessonName: String? = null,

    @SerialName("partition")
    val partition: String? = null,

    @SerialName("courseCode")
    val courseCode: String? = null,

    @SerialName("is_active")
    val isActive: Boolean? = null,

    @SerialName("is_booklet")
    val isBooklet: Boolean? = null,

    @SerialName("matricId")
    val matricId: Double? = null,

    @SerialName("activityItemId")
    val activityItemId: Double? = null
)


