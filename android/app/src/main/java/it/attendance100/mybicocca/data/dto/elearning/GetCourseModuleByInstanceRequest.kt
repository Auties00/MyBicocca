package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class GetCourseModuleByInstanceRequest(
    @SerializedName("module") val module: String,
    @SerializedName("instance") val instance: Int
)
