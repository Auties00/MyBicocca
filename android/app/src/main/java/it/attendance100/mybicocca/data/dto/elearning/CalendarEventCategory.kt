package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventCategory(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("idnumber") val idNumber: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("parent") val parent: Int? = null,
    @SerializedName("coursecount") val courseCount: Int? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("depth") val depth: Int? = null,
    @SerializedName("nestedname") val nestedName: String? = null,
    @SerializedName("url") val url: String? = null
)