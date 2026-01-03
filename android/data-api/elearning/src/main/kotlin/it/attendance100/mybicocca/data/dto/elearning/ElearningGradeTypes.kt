package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a grade item in a course.
 */
@Serializable
data class ElearningGradeItem(
    @SerialName("id")
    val id: Int,
    @SerialName("itemname")
    val itemName: String? = null,
    @SerialName("itemtype")
    val itemType: String? = null,
    @SerialName("itemmodule")
    val itemModule: String? = null,
    @SerialName("iteminstance")
    val itemInstance: Int? = null,
    @SerialName("itemnumber")
    val itemNumber: Int? = null,
    @SerialName("categoryid")
    val categoryId: Int? = null,
    @SerialName("outcomeid")
    val outcomeId: Int? = null,
    @SerialName("scaleid")
    val scaleId: Int? = null,
    @SerialName("locked")
    val locked: Boolean? = null,
    @SerialName("cmid")
    val cmId: Int? = null,
    @SerialName("weightraw")
    val weightRaw: Double? = null,
    @SerialName("weightformatted")
    val weightFormatted: String? = null,
    @SerialName("graderaw")
    val gradeRaw: Double? = null,
    @SerialName("gradedatesubmitted")
    val gradeDateSubmitted: Long? = null,
    @SerialName("gradedategraded")
    val gradeDateGraded: Long? = null,
    @SerialName("gradehiddenbydate")
    val gradeHiddenByDate: Boolean? = null,
    @SerialName("gradeneedsupdate")
    val gradeNeedsUpdate: Boolean? = null,
    @SerialName("gradeishidden")
    val gradeIsHidden: Boolean? = null,
    @SerialName("gradeislocked")
    val gradeIsLocked: Boolean? = null,
    @SerialName("gradeisoverridden")
    val gradeIsOverridden: Boolean? = null,
    @SerialName("gradeformatted")
    val gradeFormatted: String? = null,
    @SerialName("grademin")
    val gradeMin: Double? = null,
    @SerialName("grademax")
    val gradeMax: Double? = null,
    @SerialName("rangeformatted")
    val rangeFormatted: String? = null,
    @SerialName("percentageformatted")
    val percentageFormatted: String? = null,
    @SerialName("lettergradeformatted")
    val letterGradeFormatted: String? = null,
    @SerialName("rank")
    val rank: Int? = null,
    @SerialName("numusers")
    val numUsers: Int? = null,
    @SerialName("averageformatted")
    val averageFormatted: String? = null,
    @SerialName("feedback")
    val feedback: String? = null,
    @SerialName("feedbackformat")
    val feedbackFormat: Int? = null
)

/**
 * Represents a course grade overview.
 */
@Serializable
data class ElearningCourseGrade(
    @SerialName("courseid")
    val courseId: Int,
    @SerialName("grade")
    val grade: String? = null,
    @SerialName("rawgrade")
    val rawGrade: String? = null,
    @SerialName("rank")
    val rank: Int? = null
)
