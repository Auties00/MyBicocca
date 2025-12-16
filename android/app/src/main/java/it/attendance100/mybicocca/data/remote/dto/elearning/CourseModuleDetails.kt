package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseModuleDetails(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("course") val course: Int? = null,
    @SerializedName("module") val module: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("modname") val modName: String? = null,
    @SerializedName("instance") val instance: Int? = null,
    @SerializedName("section") val section: Int? = null,
    @SerializedName("sectionnum") val sectionNum: Int? = null,
    @SerializedName("groupmode") val groupMode: Int? = null,
    @SerializedName("groupingid") val groupingId: Int? = null,
    @SerializedName("completion") val completion: Int? = null,
    @SerializedName("idnumber") val idNumber: String? = null,
    @SerializedName("added") val added: Int? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("indent") val indent: Int? = null,
    @SerializedName("visible") val visible: Int? = null,
    @SerializedName("visibleoncoursepage") val visibleOnCoursePage: Int? = null,
    @SerializedName("visibleold") val visibleOld: Int? = null,
    @SerializedName("completiongradeitemnumber") val completionGradeItemNumber: Int? = null,
    @SerializedName("downloadcontent") val downloadContent: Int? = null,
    @SerializedName("lang") val lang: String? = null
)