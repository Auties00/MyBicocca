package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve all courses a user is enrolled in.
 *
 * @property userId The unique identifier of the user to retrieve courses for.
 */
@Serializable
class ElearningGetUserCoursesRequest(
    private val userId: Int
) : ElearningRequest<ElearningGetUserCoursesResponse> {
    override val functionName: String
        get() = "core_enrol_get_users_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("returnusercount", "0")
    }
}

/**
 * Response containing the list of courses a user is enrolled in.
 *
 * @property items The list of enrolled courses.
 */
@Serializable
data class ElearningGetUserCoursesResponse(
    override val items: List<ElearningEnrolledCourse>
) : ElearningListResponse<ElearningEnrolledCourse> {
    /**
     * Alias for [items] providing a more descriptive name.
     */
    val courses: List<ElearningEnrolledCourse> get() = items
}

/**
 * Request to retrieve the contents of a specific course.
 *
 * @property courseId The unique identifier of the course to retrieve contents for.
 */
@Serializable
class ElearningGetCourseContentsRequest(
    private val courseId: Int
) : ElearningRequest<ElearningGetCourseContentsResponse> {
    override val functionName: String
        get() = "core_course_get_contents"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
    }
}

/**
 * Response containing the sections and modules of a course.
 *
 * @property items The list of course sections.
 */
@Serializable
data class ElearningGetCourseContentsResponse(
    override val items: List<ElearningCourseSection>
) : ElearningListResponse<ElearningCourseSection> {
    /**
     * Alias for [items] providing a more descriptive name.
     */
    val sections: List<ElearningCourseSection> get() = items
}

/**
 * Represents a course that a user is enrolled in with full details.
 *
 * @property id The unique identifier of the course.
 * @property shortName The short identifier name of the course.
 * @property fullName The full display name of the course.
 * @property displayName The name displayed to the user.
 * @property enrolledUserCount The number of users enrolled in this course.
 * @property identificationNumber An external identification number for the course.
 * @property visible Whether the course is visible (1 = visible, 0 = hidden).
 * @property summary A summary description of the course.
 * @property summaryFormat The format of the summary (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 * @property format The course format (e.g., "topics", "weeks").
 * @property courseImageUrl URL to the course image.
 * @property showGrades Whether grades are shown to students.
 * @property language The language code for the course.
 * @property completionEnabled Whether completion tracking is enabled.
 * @property completionHasCriteria Whether the course has completion criteria defined.
 * @property completionUserTracked Whether the user is being tracked for completion.
 * @property categoryId The identifier of the course category.
 * @property progress The user's completion progress percentage (0-100).
 * @property completed Whether the user has completed the course.
 * @property startDateTimestamp Unix timestamp when the course starts.
 * @property endDateTimestamp Unix timestamp when the course ends.
 * @property marker The current section marker position.
 * @property lastAccessTimestamp Unix timestamp of the user's last access to this course.
 * @property isFavourite Whether the course is marked as a favourite.
 * @property hidden Whether the course is hidden from the user's view.
 * @property overviewFiles List of files displayed in the course overview.
 */
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
    val identificationNumber: String? = null,
    @SerialName("visible")
    val visible: Int? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("summaryformat")
    val summaryFormat: Int? = null,
    @SerialName("format")
    val format: String? = null,
    @SerialName("courseimage")
    val courseImageUrl: String? = null,
    @SerialName("showgrades")
    val showGrades: Boolean? = null,
    @SerialName("lang")
    val language: String? = null,
    @SerialName("enablecompletion")
    val completionEnabled: Boolean? = null,
    @SerialName("completionhascriteria")
    val completionHasCriteria: Boolean? = null,
    @SerialName("completionusertracked")
    val completionUserTracked: Boolean? = null,
    @SerialName("category")
    val categoryId: Int? = null,
    @SerialName("progress")
    val progress: Double? = null,
    @SerialName("completed")
    val completed: Boolean? = null,
    @SerialName("startdate")
    val startDateTimestamp: Long? = null,
    @SerialName("enddate")
    val endDateTimestamp: Long? = null,
    @SerialName("marker")
    val marker: Int? = null,
    @SerialName("lastaccess")
    val lastAccessTimestamp: Long? = null,
    @SerialName("isfavourite")
    val isFavourite: Boolean? = null,
    @SerialName("hidden")
    val hidden: Boolean? = null,
    @SerialName("overviewfiles")
    val overviewFiles: List<ElearningFile>? = null
)

/**
 * Represents a file in the Moodle system.
 *
 * @property fileName The name of the file.
 * @property filePath The path to the file within the file area.
 * @property fileSize The size of the file in bytes.
 * @property fileUrl The URL to download the file.
 * @property timeModifiedTimestamp Unix timestamp when the file was last modified.
 * @property mimeType The MIME type of the file.
 * @property isExternalFile Whether the file is stored externally.
 * @property repositoryType The type of repository storing the file.
 */
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
    val timeModifiedTimestamp: Long? = null,
    @SerialName("mimetype")
    val mimeType: String? = null,
    @SerialName("isexternalfile")
    val isExternalFile: Boolean? = null,
    @SerialName("repositorytype")
    val repositoryType: String? = null
)

/**
 * Represents a section within a course containing modules.
 *
 * @property id The unique identifier of the section.
 * @property name The name of the section.
 * @property visible Whether the section is visible (1 = visible, 0 = hidden).
 * @property summary The summary description of the section.
 * @property summaryFormat The format of the summary (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 * @property sectionNumber The section number within the course.
 * @property hiddenByNumberOfSections Whether hidden due to section limit.
 * @property userVisible Whether the section is visible to the current user.
 * @property availabilityInfo Information about availability restrictions.
 * @property modules List of modules (activities) in this section.
 */
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
    val sectionNumber: Int? = null,
    @SerialName("hiddenbynumsections")
    val hiddenByNumberOfSections: Int? = null,
    @SerialName("uservisible")
    val userVisible: Boolean? = null,
    @SerialName("availabilityinfo")
    val availabilityInfo: String? = null,
    @SerialName("modules")
    val modules: List<ElearningCourseModule> = emptyList()
)

/**
 * Represents an activity or resource module within a course section.
 *
 * @property id The unique identifier of the module.
 * @property url The URL to access the module.
 * @property name The name of the module.
 * @property instanceId The instance identifier of the module activity.
 * @property contextId The context identifier for permissions.
 * @property description The description of the module.
 * @property visible Whether the module is visible (1 = visible, 0 = hidden).
 * @property userVisible Whether the module is visible to the current user.
 * @property availabilityInfo Information about availability restrictions.
 * @property visibleOnCoursePage Whether visible on the main course page.
 * @property moduleIconUrl URL to the module type icon.
 * @property moduleName The type name of the module (e.g., "forum", "quiz", "assign").
 * @property modulePluralName The plural name of the module type.
 * @property indent The indentation level for display.
 * @property onClickHandler JavaScript to execute on click.
 * @property afterLinkHtml HTML to display after the module link.
 * @property customData Custom data associated with the module.
 * @property noViewLink Whether to hide the view link.
 * @property completionType The type of completion tracking (0 = none, 1 = manual, 2 = automatic).
 * @property completionData The user's completion data for this module.
 * @property contents List of content items within the module.
 * @property contentsInfo Summary information about the module contents.
 * @property dates List of important dates for the module.
 */
@Serializable
data class ElearningCourseModule(
    @SerialName("id")
    val id: Int,
    @SerialName("url")
    val url: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("instance")
    val instanceId: Int? = null,
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
    val moduleIconUrl: String? = null,
    @SerialName("modname")
    val moduleName: String? = null,
    @SerialName("modplural")
    val modulePluralName: String? = null,
    @SerialName("indent")
    val indent: Int? = null,
    @SerialName("onclick")
    val onClickHandler: String? = null,
    @SerialName("afterlink")
    val afterLinkHtml: String? = null,
    @SerialName("customdata")
    val customData: String? = null,
    @SerialName("noviewlink")
    val noViewLink: Boolean? = null,
    @SerialName("completion")
    val completionType: Int? = null,
    @SerialName("completiondata")
    val completionData: ElearningModuleCompletionData? = null,
    @SerialName("contents")
    val contents: List<ElearningModuleContent>? = null,
    @SerialName("contentsinfo")
    val contentsInfo: ElearningModuleContentsInfo? = null,
    @SerialName("dates")
    val dates: List<ElearningModuleDate>? = null
)

/**
 * Represents completion tracking data for a module.
 *
 * @property state The completion state (0 = incomplete, 1 = complete, 2 = complete pass, 3 = complete fail).
 * @property timeCompletedTimestamp Unix timestamp when completion was recorded.
 * @property overrideByUserId The user who overrode the completion state.
 * @property valueUsed Whether a value was used to determine completion.
 * @property hasCompletion Whether completion tracking is enabled.
 * @property isAutomatic Whether completion is tracked automatically.
 * @property isTrackedUser Whether the current user is tracked for completion.
 * @property userVisible Whether completion data is visible to the user.
 */
@Serializable
data class ElearningModuleCompletionData(
    @SerialName("state")
    val state: Int? = null,
    @SerialName("timecompleted")
    val timeCompletedTimestamp: Long? = null,
    @SerialName("overrideby")
    val overrideByUserId: Int? = null,
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

/**
 * Represents content within a module such as files or embedded content.
 *
 * @property type The type of content (e.g., "file", "content").
 * @property fileName The name of the file.
 * @property filePath The path to the file.
 * @property fileSize The size of the file in bytes.
 * @property fileUrl The URL to download or access the content.
 * @property timeCreatedTimestamp Unix timestamp when the content was created.
 * @property timeModifiedTimestamp Unix timestamp when the content was last modified.
 * @property sortOrder The order for displaying content items.
 * @property mimeType The MIME type of the content.
 * @property isExternalFile Whether the file is stored externally.
 * @property userId The user who uploaded the content.
 * @property author The author name of the content.
 * @property license The license applied to the content.
 * @property content Embedded content as text.
 */
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
    val timeCreatedTimestamp: Long? = null,
    @SerialName("timemodified")
    val timeModifiedTimestamp: Long? = null,
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

/**
 * Summary information about module contents.
 *
 * @property filesCount The number of files in the module.
 * @property filesSize The total size of all files in bytes.
 * @property lastModifiedTimestamp Unix timestamp of the most recent modification.
 * @property mimeTypes List of MIME types present in the contents.
 * @property repositoryType The type of repository storing the files.
 */
@Serializable
data class ElearningModuleContentsInfo(
    @SerialName("filescount")
    val filesCount: Int? = null,
    @SerialName("filessize")
    val filesSize: Long? = null,
    @SerialName("lastmodified")
    val lastModifiedTimestamp: Long? = null,
    @SerialName("mimetypes")
    val mimeTypes: List<String>? = null,
    @SerialName("repositorytype")
    val repositoryType: String? = null
)

/**
 * Represents an important date associated with a module.
 *
 * @property label The label describing the date.
 * @property timestamp Unix timestamp of the date.
 * @property relativeToTimestamp Timestamp this date is relative to.
 * @property dataId An identifier for the date data.
 */
@Serializable
data class ElearningModuleDate(
    @SerialName("label")
    val label: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("relativeto")
    val relativeToTimestamp: Long? = null,
    @SerialName("dataid")
    val dataId: String? = null
)

/**
 * Represents a top-level area grouping course categories (e.g., faculties, departments).
 *
 * @property name The name of the course area.
 * @property categories List of course categories within this area.
 */
@Serializable
data class ElearningCourseArea(
    val name: String,
    val categories: List<ElearningCourseCategory>
)

/**
 * Represents a category of courses.
 *
 * @property name The name of the category.
 * @property url The URL to view the category contents.
 */
@Serializable
data class ElearningCourseCategory(
    val name: String,
    val url: String
)

/**
 * Represents the contents of a course category including subcategories and courses.
 *
 * @property name The name of the category.
 * @property subcategories List of subcategories within this category.
 * @property courses List of courses directly under this category.
 */
@Serializable
data class ElearningCourseCategoryContents(
    val name: String,
    val subcategories: List<ElearningCourseCategory>,
    val courses: List<ElearningCourse>
)

/**
 * Represents a basic course entry from category listings.
 *
 * @property name The display name of the course.
 * @property code The short code identifying the course.
 * @property url The URL to access the course.
 * @property id The unique identifier of the course.
 * @property hasPassword Whether the course requires a password to enrol.
 */
@Serializable
data class ElearningCourse(
    val name: String,
    val code: String,
    val url: String,
    val id: Int,
    val hasPassword: Boolean
)

/**
 * Public, no-authentication information scraped from `/course/info.php?id={id}`.
 *
 * @property id The unique identifier of the course (matches `data-courseid`).
 * @property name The full display name of the course.
 * @property code The short course code (e.g. "2526-3-E3101Q113").
 * @property viewUrl URL to the (gated) course content page.
 * @property metadata Course "Scheda" metadata block (CFU, period, etc.).
 * @property syllabus Per-language syllabus content (typically one entry for "it" and one for "en").
 * @property staff Course staff grouped by role ("Docente", "Tutor", "Esercitatore", ...).
 * @property enrolmentMethods Localized names of available enrolment methods.
 * @property studentOpinionUrl URL to the previous-year course evaluation, if exposed.
 * @property bibliographyUrl URL to the library catalog search for this course, if exposed.
 */
@Serializable
data class ElearningCoursePublicInfo(
    val id: Int,
    val name: String,
    val code: String,
    val viewUrl: String,
    val metadata: ElearningCoursePublicMetadata,
    val syllabus: List<ElearningCourseSyllabus>,
    val staff: List<ElearningCourseStaffGroup>,
    val enrolmentMethods: List<String>,
    val studentOpinionUrl: String?,
    val bibliographyUrl: String?
)

/**
 * "Scheda del corso" metadata block. Categorical fields are kept as the
 * server-localized strings (e.g. "Primo Semestre", "Obbligatorio", "Italiano").
 *
 * @property disciplinarySector Italian SSD code, e.g. "INF/01", "MAT/02".
 * @property cfu Credit value (CFU) parsed as an integer.
 * @property period Teaching period (e.g. "Primo Semestre").
 * @property activityType Activity type (e.g. "Obbligatorio", "Obbligatorio a scelta").
 * @property hours Total contact hours parsed as an integer.
 * @property degreeType Degree type (e.g. "Laurea Triennale").
 * @property language Teaching language (e.g. "Italiano").
 */
@Serializable
data class ElearningCoursePublicMetadata(
    val disciplinarySector: String?,
    val cfu: Int?,
    val period: String?,
    val activityType: String?,
    val hours: Int?,
    val degreeType: String?,
    val language: String?
)

/**
 * Syllabus content for a single language tab on the public info page.
 *
 * @property language Language code from the tab id (typically "it" or "en").
 * @property exportPdfUrl URL to the syllabus PDF export for this language.
 * @property fields Ordered list of syllabus fields shown in the tab.
 */
@Serializable
data class ElearningCourseSyllabus(
    val language: String,
    val exportPdfUrl: String?,
    val fields: List<ElearningCourseSyllabusField>
)

/**
 * A single syllabus field (server-localized title plus HTML body).
 *
 * @property title Localized field title (e.g. "Obiettivi", "Aims").
 * @property htmlContent Raw HTML body of the field.
 */
@Serializable
data class ElearningCourseSyllabusField(
    val title: String,
    val htmlContent: String
)

/**
 * A group of staff members sharing the same role on the public info page.
 *
 * @property role Localized role name (e.g. "Docente", "Tutor", "Esercitatore").
 * @property members Members belonging to this role, in display order.
 */
@Serializable
data class ElearningCourseStaffGroup(
    val role: String,
    val members: List<ElearningCourseStaffMember>
)

/**
 * A single staff member as listed on the public info page.
 *
 * @property name Display name of the staff member.
 * @property profileUrl URL to the staff member's Moodle profile page.
 * @property userId Numeric user id parsed from [profileUrl], if present.
 * @property initials Two-letter initials shown in the avatar bubble.
 * @property email Email address parsed from the row id, if present.
 */
@Serializable
data class ElearningCourseStaffMember(
    val name: String,
    val profileUrl: String,
    val userId: Int?,
    val initials: String?,
    val email: String?
)

/**
 * Request to enroll the current user into a course using self-enrollment.
 *
 * @property courseId The unique identifier of the course to enroll into.
 * @property password The enrollment password, if the course requires one.
 * @property instanceId The self-enrollment instance ID, if multiple enrollment methods exist.
 */
@Serializable
class ElearningEnrollIntoCourseRequest(
    private val courseId: Int,
    private val password: String? = null,
    private val instanceId: Int? = null
) : ElearningRequest<ElearningEnrollIntoCourseResponse> {
    override val functionName: String
        get() = "enrol_self_enrol_user"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        password?.let { formData.append("password", it) }
        instanceId?.let { formData.append("instanceid", it.toString()) }
    }
}

/**
 * Response from enrolling into a course.
 *
 * @property status Whether the enrollment was successful.
 * @property warnings List of warnings that occurred during enrollment.
 */
@Serializable
data class ElearningEnrollIntoCourseResponse(
    @SerialName("status")
    val status: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse