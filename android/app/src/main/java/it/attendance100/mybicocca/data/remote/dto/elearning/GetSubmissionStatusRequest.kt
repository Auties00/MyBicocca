package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetSubmissionStatusRequest(
    @SerializedName("assignid") val assignId: Int,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("groupid") val groupId: Int? = null
)