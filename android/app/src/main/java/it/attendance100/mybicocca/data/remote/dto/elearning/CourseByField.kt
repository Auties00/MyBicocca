package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseByField(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("displayname") val displayName: String? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("summaryformat") val summaryFormat: Int? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("startdate") val startDate: Int? = null,
    @SerializedName("enddate") val endDate: Int? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("enablecompletion") val enableCompletion: Boolean? = null,
    @SerializedName("categoryname") val categoryName: String? = null,
    @SerializedName("sortorder") val sortOrder: Int? = null,
    @SerializedName("summaryfiles") val summaryFiles: List<MoodleFile>? = null,
    @SerializedName("overviewfiles") val overviewFiles: List<MoodleFile>? = null,
    @SerializedName("contacts") val contacts: List<CourseContact>? = null,
    @SerializedName("enrollmentmethods") val enrollmentMethods: List<String>? = null,
    @SerializedName("customfields") val customFields: List<CourseCustomField>? = null
)