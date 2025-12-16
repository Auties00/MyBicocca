package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetUsersCoursesRequest(
    @SerializedName("userid") val userid: Int,
    @SerializedName("returnusercount") val returnusercount: Boolean? = null
)
