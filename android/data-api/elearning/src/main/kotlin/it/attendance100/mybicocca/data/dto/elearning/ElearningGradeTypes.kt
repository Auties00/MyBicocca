package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve grade items for a course.
 *
 * @property courseId The course identifier to get grade items for.
 * @property userId Optional user identifier. If not provided, returns grades for all users the current user can view.
 * @property groupId Optional group identifier to filter users by group.
 */
@Serializable
class ElearningGetGradeItemsRequest(
    private val courseId: Int,
    private val userId: Int? = null,
    private val groupId: Int? = null
) : ElearningRequest<ElearningGetGradeItemsResponse> {
    override val functionName = "gradereport_user_get_grade_items"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        userId?.let { formData.append("userid", it.toString()) }
        groupId?.let { formData.append("groupid", it.toString()) }
    }
}

/**
 * Response containing grade items and user grades for a course.
 *
 * @property userGrades The list of user grades including their grade items.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningGetGradeItemsResponse(
    @SerialName("usergrades")
    val userGrades: List<ElearningUserGrades> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to retrieve course grades overview for the current user.
 *
 * @property userId Optional user identifier. If not provided, returns grades for the current user.
 */
@Serializable
class ElearningGetCourseGradesRequest(
    private val userId: Int? = null
) : ElearningRequest<ElearningGetCourseGradesResponse> {
    override val functionName = "gradereport_overview_get_course_grades"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
    }
}

/**
 * Response containing course grades overview.
 *
 * @property grades The list of course grades.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningGetCourseGradesResponse(
    @SerialName("grades")
    val grades: List<ElearningCourseGrade> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents a user's grades in a course.
 *
 * @property courseId The course identifier.
 * @property courseIdentificationNumber The identification number of the course.
 * @property userId The user identifier.
 * @property userFullName The full name of the user.
 * @property userIdentificationNumber The identification number of the user.
 * @property maxDepth The maximum depth of the grade tree.
 * @property gradeItems The list of grade items for this user.
 */
@Serializable
data class ElearningUserGrades(
    @SerialName("courseid")
    val courseId: Int,
    @SerialName("courseidnumber")
    val courseIdentificationNumber: String? = null,
    @SerialName("userid")
    val userId: Int,
    @SerialName("userfullname")
    val userFullName: String? = null,
    @SerialName("useridnumber")
    val userIdentificationNumber: String? = null,
    @SerialName("maxdepth")
    val maxDepth: Int? = null,
    @SerialName("gradeitems")
    val gradeItems: List<ElearningGradeItem> = emptyList()
)

/**
 * Represents a grade item in a course.
 *
 * @property id The unique identifier of the grade item.
 * @property itemName The name of the grade item.
 * @property itemType The type of grade item (e.g., "mod", "course", "category").
 * @property itemModule The module type if this is a module grade item.
 * @property itemInstance The instance identifier of the module.
 * @property itemNumber The item number for activities with multiple grade items.
 * @property categoryId The category identifier this grade item belongs to.
 * @property outcomeId The outcome identifier if this is an outcome grade item.
 * @property scaleId The scale identifier if using a scale for grading.
 * @property locked Whether the grade item is locked.
 * @property courseModuleId The course module identifier if this is a module grade item.
 * @property weightRaw The raw weight of this grade item in the course total.
 * @property weightFormatted The formatted weight percentage string.
 * @property gradeRaw The raw numeric grade value.
 * @property gradeDateSubmitted The Unix timestamp when the submission was made.
 * @property gradeDateGraded The Unix timestamp when the grade was assigned.
 * @property gradeHiddenByDate Whether the grade is hidden until a certain date.
 * @property gradeNeedsUpdate Whether the grade needs to be recalculated.
 * @property gradeIsHidden Whether the grade is hidden from the student.
 * @property gradeIsLocked Whether the grade is locked.
 * @property gradeIsOverridden Whether the grade has been manually overridden.
 * @property gradeFormatted The formatted grade string for display.
 * @property gradeMin The minimum possible grade.
 * @property gradeMax The maximum possible grade.
 * @property rangeFormatted The formatted grade range string.
 * @property percentageFormatted The formatted percentage string.
 * @property letterGradeFormatted The formatted letter grade string.
 * @property rank The user's rank for this grade item.
 * @property numberOfUsers The total number of users with grades for this item.
 * @property averageFormatted The formatted class average string.
 * @property feedback The feedback text for this grade.
 * @property feedbackFormat The format of the feedback (0=MOODLE, 1=HTML, 2=PLAIN, 4=MARKDOWN).
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
    val courseModuleId: Int? = null,
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
    val numberOfUsers: Int? = null,
    @SerialName("averageformatted")
    val averageFormatted: String? = null,
    @SerialName("feedback")
    val feedback: String? = null,
    @SerialName("feedbackformat")
    val feedbackFormat: Int? = null
)

/**
 * Represents a course grade overview.
 *
 * @property courseId The course identifier.
 * @property grade The formatted grade string.
 * @property rawGrade The raw grade value as a string.
 * @property rank The user's rank in the course.
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
