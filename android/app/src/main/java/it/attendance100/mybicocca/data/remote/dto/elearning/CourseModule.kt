package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class CourseModule(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("url") val url: URI? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("instance") val instance: Int? = null,
    @SerializedName("contextid") val contextId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("uservisible") val userVisible: Boolean? = null,
    @SerializedName("availabilityinfo") val availabilityInfo: String? = null,
    @SerializedName("visibleoncoursepage") val visibleOnCoursePage: Int? = null,
    @SerializedName("modicon") val modIcon: URI? = null,
    @SerializedName("modname") val modName: String? = null,
    @SerializedName("modplural") val modPlural: String? = null,
    @SerializedName("indent") val indent: Int? = null,
    @SerializedName("onclick") val onClick: String? = null,
    @SerializedName("afterlink") val afterLink: String? = null,
    @SerializedName("customdata") val customData: String? = null,
    @SerializedName("noviewlink") val noViewLink: Boolean? = null,
    @SerializedName("completion") val completion: Int? = null,
    @SerializedName("completiondata") val completionData: ModuleCompletionData? = null,
    @SerializedName("downloadcontent") val downloadContent: Int? = null,
    @SerializedName("dates") val dates: List<ModuleDate>? = null,
    @SerializedName("contents") val contents: List<MoodleFile>? = null
)