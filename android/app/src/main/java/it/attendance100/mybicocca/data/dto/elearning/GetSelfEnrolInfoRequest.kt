package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetSelfEnrolInfoRequest(
    @SerializedName("instanceid") val instanceId: Int
)