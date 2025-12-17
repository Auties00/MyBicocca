package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseModuleResponse(
    @SerializedName("cm") val cm: CourseModuleDetails? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)