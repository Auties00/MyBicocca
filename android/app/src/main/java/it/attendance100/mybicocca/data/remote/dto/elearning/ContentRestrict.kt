package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ContentRestrict(
    @SerializedName("users") val users: List<Int>? = null,
    @SerializedName("courses") val courses: List<Int>? = null
)