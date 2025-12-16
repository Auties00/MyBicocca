package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetEnrolledUsersRequest(
    @SerializedName("courseid") val courseid: Int,
    @SerializedName("options") val options: List<GetEnrolledUsersOption>? = null
)

data class GetEnrolledUsersOption(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
