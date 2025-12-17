package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class ViewCourseRequest(
    @SerializedName("courseid") val courseid: Int,
    @SerializedName("sectionnumber") val sectionnumber: Int? = null
)
