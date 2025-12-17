package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class CheckUpdatesRequest(
    @SerializedName("courseid") val courseid: Int,
    @SerializedName("tocheck") val tocheck: List<CoreCourseCheckUpdatesToCheck>? = null
)

data class CoreCourseCheckUpdatesToCheck(
    @SerializedName("contextlevel") val contextlevel: String,
    @SerializedName("id") val id: Int,
    @SerializedName("since") val since: Int
)