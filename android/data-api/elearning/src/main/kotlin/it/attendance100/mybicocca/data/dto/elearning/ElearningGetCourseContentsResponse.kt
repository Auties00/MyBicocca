package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningCourseSection(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("visible")
    val visible: Int? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("summaryformat")
    val summaryFormat: Int? = null,
    @SerialName("section")
    val section: Int? = null,
    @SerialName("hiddenbynumsections")
    val hiddenByNumSections: Int? = null,
    @SerialName("uservisible")
    val userVisible: Boolean? = null,
    @SerialName("availabilityinfo")
    val availabilityInfo: String? = null,
    @SerialName("modules")
    val modules: List<ElearningCourseModule> = emptyList()
)

@Serializable
data class ElearningCourseModule(
    @SerialName("id")
    val id: Int,
    @SerialName("url")
    val url: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("instance")
    val instance: Int? = null,
    @SerialName("contextid")
    val contextId: Int? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("visible")
    val visible: Int? = null,
    @SerialName("uservisible")
    val userVisible: Boolean? = null,
    @SerialName("availabilityinfo")
    val availabilityInfo: String? = null,
    @SerialName("visibleoncoursepage")
    val visibleOnCoursePage: Int? = null,
    @SerialName("modicon")
    val modIcon: String? = null,
    @SerialName("modname")
    val modName: String? = null,
    @SerialName("modplural")
    val modPlural: String? = null,
    @SerialName("indent")
    val indent: Int? = null,
    @SerialName("onclick")
    val onClick: String? = null,
    @SerialName("afterlink")
    val afterLink: String? = null,
    @SerialName("customdata")
    val customData: String? = null,
    @SerialName("noviewlink")
    val noViewLink: Boolean? = null,
    @SerialName("completion")
    val completion: Int? = null,
    @SerialName("completiondata")
    val completionData: ElearningCompletionData? = null,
    @SerialName("contents")
    val contents: List<ElearningModuleContent>? = null,
    @SerialName("contentsinfo")
    val contentsInfo: ElearningContentsInfo? = null,
    @SerialName("dates")
    val dates: List<ElearningModuleDate>? = null
)

@Serializable
data class ElearningCompletionData(
    @SerialName("state")
    val state: Int? = null,
    @SerialName("timecompleted")
    val timeCompleted: Long? = null,
    @SerialName("overrideby")
    val overrideBy: Int? = null,
    @SerialName("valueused")
    val valueUsed: Boolean? = null,
    @SerialName("hascompletion")
    val hasCompletion: Boolean? = null,
    @SerialName("isautomatic")
    val isAutomatic: Boolean? = null,
    @SerialName("istrackeduser")
    val isTrackedUser: Boolean? = null,
    @SerialName("uservisible")
    val userVisible: Boolean? = null
)

@Serializable
data class ElearningModuleContent(
    @SerialName("type")
    val type: String? = null,
    @SerialName("filename")
    val fileName: String? = null,
    @SerialName("filepath")
    val filePath: String? = null,
    @SerialName("filesize")
    val fileSize: Long? = null,
    @SerialName("fileurl")
    val fileUrl: String? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("sortorder")
    val sortOrder: Int? = null,
    @SerialName("mimetype")
    val mimeType: String? = null,
    @SerialName("isexternalfile")
    val isExternalFile: Boolean? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("author")
    val author: String? = null,
    @SerialName("license")
    val license: String? = null,
    @SerialName("content")
    val content: String? = null
)

@Serializable
data class ElearningContentsInfo(
    @SerialName("filescount")
    val filesCount: Int? = null,
    @SerialName("filessize")
    val filesSize: Long? = null,
    @SerialName("lastmodified")
    val lastModified: Long? = null,
    @SerialName("mimetypes")
    val mimeTypes: List<String>? = null,
    @SerialName("repositorytype")
    val repositoryType: String? = null
)

@Serializable
data class ElearningModuleDate(
    @SerialName("label")
    val label: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("relativeto")
    val relativeTo: Long? = null,
    @SerialName("dataid")
    val dataId: String? = null
)

@Serializable
data class ElearningGetCourseContentsResponse(
    override val items: List<ElearningCourseSection>
) : ElearningListResponse<ElearningCourseSection> {
    val sections: List<ElearningCourseSection> get() = items
}
