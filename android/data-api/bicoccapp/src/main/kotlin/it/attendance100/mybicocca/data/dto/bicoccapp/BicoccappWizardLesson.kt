package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardLesson(
    @SerialName("activity_code")
    val activityCode: String? = null,

    @SerialName("cds_code")
    val cdsCode: String? = null,

    @SerialName("lesson_name")
    val lessonName: String? = null,

    @SerialName("course_code")
    val courseCode: String? = null,

    @SerialName("partition")
    val partition: String? = null,

    @SerialName("teachers")
    val teachers: List<BicoccappWizardTeacher> = emptyList()
)