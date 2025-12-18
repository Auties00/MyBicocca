package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourseDetailResponse(
    @SerializedName("activity_code")
    val activityCode: String? = null,

    @SerializedName("cds_code")
    val cdsCode: String? = null,

    @SerializedName("lesson_name")
    val lessonName: String? = null,

    @SerializedName("partition")
    val partition: String? = null,

    @SerializedName("course_code")
    val courseCode: String? = null,

    @SerializedName("teachers")
    val teachers: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseTeacher> = emptyList(),

    @SerializedName("events")
    val events: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseEvent> = emptyList()
)

