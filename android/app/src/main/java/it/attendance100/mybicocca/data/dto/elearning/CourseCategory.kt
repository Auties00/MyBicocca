package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseCategory(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("idnumber") val idNumber: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("descriptionformat") val descriptionFormat: Int? = null,
    @SerializedName("parent") val parent: Int? = null,
    @SerializedName("sortorder") val sortOrder: Int? = null,
    @SerializedName("coursecount") val courseCount: Int? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("visibleold") val visibleOld: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("depth") val depth: Int? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("theme") val theme: String? = null
)