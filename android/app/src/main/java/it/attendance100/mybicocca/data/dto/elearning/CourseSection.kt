package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseSection(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("summaryformat") val summaryFormat: Int? = null,
    @SerializedName("section") val section: Int? = null,
    @SerializedName("hiddenbynumsections") val hiddenByNumSections: Int? = null,
    @SerializedName("uservisible") val userVisible: Boolean? = null,
    @SerializedName("modules") val modules: List<CourseModule>? = null
)