package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetEnrolmentMethodsRequest(
    @SerializedName("courseid") val courseid: Int
)
