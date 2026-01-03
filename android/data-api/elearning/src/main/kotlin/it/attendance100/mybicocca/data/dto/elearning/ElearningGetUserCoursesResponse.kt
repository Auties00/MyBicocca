package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningEnrolledCourse(
    @SerialName("id")
    val id: Int,
    @SerialName("shortname")
    val shortName: String,
    @SerialName("fullname")
    val fullName: String,
    @SerialName("displayname")
    val displayName: String? = null,
    @SerialName("enrolledusercount")
    val enrolledUserCount: Int? = null,
    @SerialName("idnumber")
    val idNumber: String? = null,
    @SerialName("visible")
    val visible: Int? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("summaryformat")
    val summaryFormat: Int? = null,
    @SerialName("format")
    val format: String? = null,
    @SerialName("courseimage")
    val courseImage: String? = null,
    @SerialName("showgrades")
    val showGrades: Boolean? = null,
    @SerialName("lang")
    val lang: String? = null,
    @SerialName("enablecompletion")
    val enableCompletion: Boolean? = null,
    @SerialName("completionhascriteria")
    val completionHasCriteria: Boolean? = null,
    @SerialName("completionusertracked")
    val completionUserTracked: Boolean? = null,
    @SerialName("category")
    val category: Int? = null,
    @SerialName("progress")
    val progress: Double? = null,
    @SerialName("completed")
    val completed: Boolean? = null,
    @SerialName("startdate")
    val startDate: Long? = null,
    @SerialName("enddate")
    val endDate: Long? = null,
    @SerialName("marker")
    val marker: Int? = null,
    @SerialName("lastaccess")
    val lastAccess: Long? = null,
    @SerialName("isfavourite")
    val isFavourite: Boolean? = null,
    @SerialName("hidden")
    val hidden: Boolean? = null,
    @SerialName("overviewfiles")
    val overviewFiles: List<ElearningFile>? = null
)

@Serializable
data class ElearningFile(
    @SerialName("filename")
    val fileName: String? = null,
    @SerialName("filepath")
    val filePath: String? = null,
    @SerialName("filesize")
    val fileSize: Long? = null,
    @SerialName("fileurl")
    val fileUrl: String? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("mimetype")
    val mimeType: String? = null,
    @SerialName("isexternalfile")
    val isExternalFile: Boolean? = null,
    @SerialName("repositorytype")
    val repositoryType: String? = null
)

@Serializable
data class ElearningGetUserCoursesResponse(
    override val items: List<ElearningEnrolledCourse>
) : ElearningListResponse<ElearningEnrolledCourse> {
    val courses: List<ElearningEnrolledCourse> get() = items
}
