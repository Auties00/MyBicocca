package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetGuestEnrolInfoRequest(
    @SerializedName("instanceid") val instanceId: Int
)