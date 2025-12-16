package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param activityCode
 * @param cdsCode
 * @param lessonName
 * @param partition
 * @param courseCode
 * @param teachers
 * @param events
 */


data class CourseDetail(

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
    val teachers: List<Any>? = null,

    @SerializedName("events")
    val events: List<Any>? = null

)

