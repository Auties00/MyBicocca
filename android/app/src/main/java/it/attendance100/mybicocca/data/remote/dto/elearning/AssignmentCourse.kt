package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AssignmentCourse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("assignments") val assignments: List<Assignment>? = null
)