package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class SelfEnrolUserRequest(
    @SerializedName("courseid") val courseid: Int,
    @SerializedName("password") val password: String? = null,
    @SerializedName("instanceid") val instanceid: Int? = 0
)
