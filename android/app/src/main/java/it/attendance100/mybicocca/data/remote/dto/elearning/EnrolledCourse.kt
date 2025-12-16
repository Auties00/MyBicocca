package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class EnrolledCourse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("displayname") val displayName: String? = null,
    @SerializedName("enrolledusercount") val enrolledUserCount: Int? = null,
    @SerializedName("idnumber") val idNumber: String? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("summaryformat") val summaryFormat: Int? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("showgrades") val showGrades: Boolean? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("enablecompletion") val enableCompletion: Boolean? = null,
    @SerializedName("completionhascriteria") val completionHasCriteria: Boolean? = null,
    @SerializedName("completionusertracked") val completionUserTracked: Boolean? = null,
    @SerializedName("category") val category: Int? = null,
    @SerializedName("progress") val progress: BigDecimal? = null,
    @SerializedName("completed") val completed: Boolean? = null,
    @SerializedName("startdate") val startDate: Int? = null,
    @SerializedName("enddate") val endDate: Int? = null,
    @SerializedName("marker") val marker: Int? = null,
    @SerializedName("lastaccess") val lastAccess: Int? = null,
    @SerializedName("isfavourite") val isFavourite: Boolean? = null,
    @SerializedName("hidden") val hidden: Boolean? = null,
    @SerializedName("overviewfiles") val overviewFiles: List<MoodleFile>? = null
)