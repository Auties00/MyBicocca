package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class ValidateGuestPasswordRequest(
    @SerializedName("instanceid") val instanceId: Int,
    @SerializedName("password") val password: String
)