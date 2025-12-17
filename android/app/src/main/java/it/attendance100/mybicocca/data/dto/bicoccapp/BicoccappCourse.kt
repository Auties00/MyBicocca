package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourse(
    @SerializedName("cdsCode")
    val cdsCode: String? = null,

    @SerializedName("activityCode")
    val activityCode: String? = null,

    @SerializedName("lessonName")
    val lessonName: String? = null,

    @SerializedName("partition")
    val partition: String? = null,

    @SerializedName("courseCode")
    val courseCode: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean? = null,

    @SerializedName("is_booklet")
    val isBooklet: Boolean? = null,

    @SerializedName("matricId")
    val matricId: Double? = null,

    @SerializedName("activityItemId")
    val activityItemId: Double? = null
)


