package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve assignments for specified courses.
 *
 * @property courseIds The list of course identifiers to retrieve assignments for.
 * @property capabilities Optional list of capabilities to filter assignments.
 * @property includeNotEnrolledCourses Whether to include assignments from courses the user is not enrolled in.
 */
@Serializable
class ElearningGetAssignmentsRequest(
    private val courseIds: List<Int>,
    private val capabilities: List<String>? = null,
    private val includeNotEnrolledCourses: Boolean = false
) : ElearningRequest<ElearningGetAssignmentsResponse> {
    override val functionName = "mod_assign_get_assignments"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, id ->
            formData.append("courseids[$index]", id.toString())
        }
        capabilities?.forEachIndexed { index, capability ->
            formData.append("capabilities[$index]", capability)
        }
        if (includeNotEnrolledCourses) formData.append("includenotenrolledcourses", "1")
    }
}

/**
 * Response containing assignments grouped by course.
 *
 * @property courses The list of courses with their assignments.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetAssignmentsResponse(
    @SerialName("courses")
    val courses: List<ElearningCourseAssignments> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to retrieve the submission status for an assignment.
 *
 * @property assignmentId The unique identifier of the assignment.
 * @property userId Optional user identifier. If null, status for the current user is retrieved.
 * @property groupId Optional group identifier for group submissions.
 */
@Serializable
class ElearningGetSubmissionStatusRequest(
    private val assignmentId: Int,
    private val userId: Int? = null,
    private val groupId: Int? = null
) : ElearningRequest<ElearningGetSubmissionStatusResponse> {
    override val functionName = "mod_assign_get_submission_status"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("assignid", assignmentId.toString())
        userId?.let { formData.append("userid", it.toString()) }
        groupId?.let { formData.append("groupid", it.toString()) }
    }
}

/**
 * Response containing detailed submission status for an assignment.
 *
 * @property gradingSummary Summary of grading status for teachers.
 * @property lastAttempt Details of the user's last submission attempt.
 * @property feedback Feedback received for the submission.
 * @property previousAttempts List of previous submission attempts.
 * @property assignmentData Additional assignment data and attachments.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetSubmissionStatusResponse(
    @SerialName("gradingsummary")
    val gradingSummary: ElearningGradingSummary? = null,
    @SerialName("lastattempt")
    val lastAttempt: ElearningLastAttempt? = null,
    @SerialName("feedback")
    val feedback: ElearningAssignmentFeedback? = null,
    @SerialName("previousattempts")
    val previousAttempts: List<ElearningPreviousAttempt>? = null,
    @SerialName("assignmentdata")
    val assignmentData: ElearningAssignmentData? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents a course with its assignments.
 *
 * @property id The unique identifier of the course.
 * @property fullName The full name of the course.
 * @property shortName The short identifier name of the course.
 * @property modifiedTimestamp Unix timestamp when the course was last modified.
 * @property assignments The list of assignments in this course.
 */
@Serializable
data class ElearningCourseAssignments(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
    @SerialName("assignments")
    val assignments: List<ElearningAssignment> = emptyList()
)

/**
 * Represents an assignment activity in a course.
 *
 * @property id The unique identifier of the assignment.
 * @property courseModuleId The course module identifier.
 * @property courseId The identifier of the course containing this assignment.
 * @property name The name of the assignment.
 * @property noSubmissions Whether submissions are disabled (1 = disabled).
 * @property submissionDraftsEnabled Whether draft submissions are enabled.
 * @property sendNotifications Whether notifications are sent to graders.
 * @property sendLateNotifications Whether late submission notifications are sent.
 * @property sendStudentNotifications Whether students receive submission notifications.
 * @property dueDateTimestamp Unix timestamp of the assignment due date.
 * @property allowSubmissionsFromTimestamp Unix timestamp when submissions open.
 * @property maximumGrade The maximum grade for the assignment.
 * @property modifiedTimestamp Unix timestamp when the assignment was last modified.
 * @property completionSubmitRequired Whether submission is required for completion.
 * @property cutOffDateTimestamp Unix timestamp after which submissions are not accepted.
 * @property gradingDueDateTimestamp Unix timestamp when grading should be completed.
 * @property teamSubmissionEnabled Whether team submissions are enabled.
 * @property requireAllTeamMembersSubmit Whether all team members must submit.
 * @property teamSubmissionGroupingId The grouping identifier for team submissions.
 * @property blindMarkingEnabled Whether blind marking is enabled.
 * @property hideGrader Whether the grader identity is hidden.
 * @property revealIdentities Whether identities are revealed.
 * @property attemptReopenMethod Method for reopening attempts (none, manual, untilpass).
 * @property maximumAttempts Maximum number of attempts allowed.
 * @property markingWorkflowEnabled Whether marking workflow is enabled.
 * @property markingAllocationEnabled Whether marker allocation is enabled.
 * @property requireSubmissionStatement Whether submission statement is required.
 * @property preventSubmissionNotInGroup Whether to prevent submissions from users not in groups.
 * @property introduction The assignment introduction text.
 * @property introductionFormat The format of the introduction.
 * @property introductionFiles Files attached to the introduction.
 * @property introductionAttachments Attachment files for the introduction.
 * @property activity The activity description text.
 * @property activityFormat The format of the activity description.
 * @property activityAttachments Attachment files for the activity.
 * @property timeLimitSeconds Time limit for the assignment in seconds.
 * @property submissionAttachmentsEnabled Whether submission attachments are enabled.
 */
@Serializable
data class ElearningAssignment(
    @SerialName("id")
    val id: Int,
    @SerialName("cmid")
    val courseModuleId: Int? = null,
    @SerialName("course")
    val courseId: Int,
    @SerialName("name")
    val name: String,
    @SerialName("nosubmissions")
    val noSubmissions: Int? = null,
    @SerialName("submissiondrafts")
    val submissionDraftsEnabled: Int? = null,
    @SerialName("sendnotifications")
    val sendNotifications: Int? = null,
    @SerialName("sendlatenotifications")
    val sendLateNotifications: Int? = null,
    @SerialName("sendstudentnotifications")
    val sendStudentNotifications: Int? = null,
    @SerialName("duedate")
    val dueDateTimestamp: Long? = null,
    @SerialName("allowsubmissionsfromdate")
    val allowSubmissionsFromTimestamp: Long? = null,
    @SerialName("grade")
    val maximumGrade: Int? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
    @SerialName("completionsubmit")
    val completionSubmitRequired: Int? = null,
    @SerialName("cutoffdate")
    val cutOffDateTimestamp: Long? = null,
    @SerialName("gradingduedate")
    val gradingDueDateTimestamp: Long? = null,
    @SerialName("teamsubmission")
    val teamSubmissionEnabled: Int? = null,
    @SerialName("requireallteammemberssubmit")
    val requireAllTeamMembersSubmit: Int? = null,
    @SerialName("teamsubmissiongroupingid")
    val teamSubmissionGroupingId: Int? = null,
    @SerialName("blindmarking")
    val blindMarkingEnabled: Int? = null,
    @SerialName("hidegrader")
    val hideGrader: Int? = null,
    @SerialName("revealidentities")
    val revealIdentities: Int? = null,
    @SerialName("attemptreopenmethod")
    val attemptReopenMethod: String? = null,
    @SerialName("maxattempts")
    val maximumAttempts: Int? = null,
    @SerialName("markingworkflow")
    val markingWorkflowEnabled: Int? = null,
    @SerialName("markingallocation")
    val markingAllocationEnabled: Int? = null,
    @SerialName("requiresubmissionstatement")
    val requireSubmissionStatement: Int? = null,
    @SerialName("preventsubmissionnotingroup")
    val preventSubmissionNotInGroup: Int? = null,
    @SerialName("intro")
    val introduction: String? = null,
    @SerialName("introformat")
    val introductionFormat: Int? = null,
    @SerialName("introfiles")
    val introductionFiles: List<ElearningFile>? = null,
    @SerialName("introattachments")
    val introductionAttachments: List<ElearningFile>? = null,
    @SerialName("activity")
    val activity: String? = null,
    @SerialName("activityformat")
    val activityFormat: Int? = null,
    @SerialName("activityattachments")
    val activityAttachments: List<ElearningFile>? = null,
    @SerialName("timelimit")
    val timeLimitSeconds: Int? = null,
    @SerialName("submissionattachments")
    val submissionAttachmentsEnabled: Int? = null
) {
    /**
     * Whether the assignment is past its due date.
     */
    val isOverdue: Boolean
        get() = dueDateTimestamp != null && dueDateTimestamp > 0 && System.currentTimeMillis() / 1000 > dueDateTimestamp
}

/**
 * Represents a submission to an assignment.
 *
 * @property id The unique identifier of the submission.
 * @property userId The identifier of the user who made the submission.
 * @property attemptNumber The attempt number for this submission.
 * @property createdTimestamp Unix timestamp when the submission was created.
 * @property modifiedTimestamp Unix timestamp when the submission was last modified.
 * @property startedTimestamp Unix timestamp when the submission was started.
 * @property status The current status of the submission (new, draft, submitted).
 * @property groupId The group identifier for group submissions.
 * @property assignmentId The identifier of the assignment.
 * @property isLatest Whether this is the latest submission (1 = yes).
 * @property plugins List of submission plugins with their data.
 * @property gradingStatus The grading status of the submission.
 */
@Serializable
data class ElearningAssignmentSubmission(
    @SerialName("id")
    val id: Int,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("timecreated")
    val createdTimestamp: Long? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
    @SerialName("timestarted")
    val startedTimestamp: Long? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("groupid")
    val groupId: Int? = null,
    @SerialName("assignment")
    val assignmentId: Int? = null,
    @SerialName("latest")
    val isLatest: Int? = null,
    @SerialName("plugins")
    val plugins: List<ElearningSubmissionPlugin>? = null,
    @SerialName("gradingstatus")
    val gradingStatus: String? = null
) {
    /** Whether the submission is in a new state. */
    val isNew: Boolean get() = status == "new"
    /** Whether the submission is in a draft state. */
    val isDraft: Boolean get() = status == "draft"
    /** Whether the submission has been submitted. */
    val isSubmitted: Boolean get() = status == "submitted"
}

/**
 * Represents a plugin that handles submission data.
 *
 * @property type The type identifier of the plugin.
 * @property name The display name of the plugin.
 * @property fileAreas List of file areas containing uploaded files.
 * @property editorFields List of editor fields with text content.
 */
@Serializable
data class ElearningSubmissionPlugin(
    @SerialName("type")
    val type: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("fileareas")
    val fileAreas: List<ElearningSubmissionFileArea>? = null,
    @SerialName("editorfields")
    val editorFields: List<ElearningEditorField>? = null
)

/**
 * Represents a file area within a submission plugin.
 *
 * @property area The name of the file area.
 * @property files List of files in this area.
 */
@Serializable
data class ElearningSubmissionFileArea(
    @SerialName("area")
    val area: String? = null,
    @SerialName("files")
    val files: List<ElearningFile>? = null
)

/**
 * Represents an editor field with text content.
 *
 * @property name The name of the editor field.
 * @property description The description of the field.
 * @property text The text content of the field.
 * @property format The format of the text (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 */
@Serializable
data class ElearningEditorField(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("text")
    val text: String? = null,
    @SerialName("format")
    val format: Int? = null
)

/**
 * Represents a grade for an assignment submission.
 *
 * @property id The unique identifier of the grade.
 * @property assignmentId The identifier of the assignment.
 * @property userId The identifier of the graded user.
 * @property attemptNumber The attempt number this grade is for.
 * @property createdTimestamp Unix timestamp when the grade was created.
 * @property modifiedTimestamp Unix timestamp when the grade was last modified.
 * @property graderId The identifier of the user who assigned the grade.
 * @property grade The raw grade value.
 * @property gradeForDisplay The formatted grade string for display.
 */
@Serializable
data class ElearningAssignmentGrade(
    @SerialName("id")
    val id: Int,
    @SerialName("assignment")
    val assignmentId: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("timecreated")
    val createdTimestamp: Long? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
    @SerialName("grader")
    val graderId: Int? = null,
    @SerialName("grade")
    val grade: String? = null,
    @SerialName("gradefordisplay")
    val gradeForDisplay: String? = null
)

/**
 * Summary of grading status for an assignment (visible to teachers).
 *
 * @property participantCount Total number of participants.
 * @property submissionDraftsCount Number of draft submissions.
 * @property submissionsEnabled Whether submissions are enabled.
 * @property submissionsSubmittedCount Number of submitted submissions.
 * @property submissionsNeedGradingCount Number of submissions needing grading.
 * @property warnOfUngroupedUsers Warning about users not in groups.
 */
@Serializable
data class ElearningGradingSummary(
    @SerialName("participantcount")
    val participantCount: Int? = null,
    @SerialName("submissiondraftscount")
    val submissionDraftsCount: Int? = null,
    @SerialName("submissionsenabled")
    val submissionsEnabled: Boolean? = null,
    @SerialName("submissionssubmittedcount")
    val submissionsSubmittedCount: Int? = null,
    @SerialName("submissionsneedgradingcount")
    val submissionsNeedGradingCount: Int? = null,
    @SerialName("warnofungroupedusers")
    val warnOfUngroupedUsers: String? = null
)

/**
 * Details about the user's last submission attempt.
 *
 * @property submission The individual user's submission.
 * @property teamSubmission The team's submission for group assignments.
 * @property submissionGroup The group identifier for the submission.
 * @property submissionGroupMembersWhoNeedToSubmit Group members who haven't submitted.
 * @property submissionsEnabled Whether submissions are enabled.
 * @property locked Whether the submission is locked.
 * @property graded Whether the submission has been graded.
 * @property canEdit Whether the user can edit the submission.
 * @property canEditOwner Whether the owner can edit the submission.
 * @property canSubmit Whether the user can submit.
 * @property extensionDueDateTimestamp Extended due date if granted.
 * @property blindMarking Whether blind marking is enabled.
 * @property gradingStatus The current grading status.
 * @property userGroups The groups the user belongs to.
 * @property timeLimitSeconds Time limit for the submission in seconds.
 */
@Serializable
data class ElearningLastAttempt(
    @SerialName("submission")
    val submission: ElearningAssignmentSubmission? = null,
    @SerialName("teamsubmission")
    val teamSubmission: ElearningAssignmentSubmission? = null,
    @SerialName("submissiongroup")
    val submissionGroup: Int? = null,
    @SerialName("submissiongroupmemberswhoneedtosubmit")
    val submissionGroupMembersWhoNeedToSubmit: List<Int>? = null,
    @SerialName("submissionsenabled")
    val submissionsEnabled: Boolean? = null,
    @SerialName("locked")
    val locked: Boolean? = null,
    @SerialName("graded")
    val graded: Boolean? = null,
    @SerialName("canedit")
    val canEdit: Boolean? = null,
    @SerialName("caneditowner")
    val canEditOwner: Boolean? = null,
    @SerialName("cansubmit")
    val canSubmit: Boolean? = null,
    @SerialName("extensionduedate")
    val extensionDueDateTimestamp: Long? = null,
    @SerialName("blindmarking")
    val blindMarking: Boolean? = null,
    @SerialName("gradingstatus")
    val gradingStatus: String? = null,
    @SerialName("usergroups")
    val userGroups: List<Int>? = null,
    @SerialName("timelimit")
    val timeLimitSeconds: Int? = null
)

/**
 * Feedback received for an assignment submission.
 *
 * @property grade The grade details.
 * @property gradeForDisplay The formatted grade string for display.
 * @property gradedDateTimestamp Unix timestamp when the submission was graded.
 * @property plugins List of feedback plugins with their data.
 */
@Serializable
data class ElearningAssignmentFeedback(
    @SerialName("grade")
    val grade: ElearningAssignmentGrade? = null,
    @SerialName("gradefordisplay")
    val gradeForDisplay: String? = null,
    @SerialName("gradeddate")
    val gradedDateTimestamp: Long? = null,
    @SerialName("plugins")
    val plugins: List<ElearningSubmissionPlugin>? = null
)

/**
 * Details about a previous submission attempt.
 *
 * @property attemptNumber The attempt number.
 * @property submission The submission details for this attempt.
 * @property grade The grade for this attempt.
 * @property feedbackPlugins Feedback plugins for this attempt.
 */
@Serializable
data class ElearningPreviousAttempt(
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("submission")
    val submission: ElearningAssignmentSubmission? = null,
    @SerialName("grade")
    val grade: ElearningAssignmentGrade? = null,
    @SerialName("feedbackplugins")
    val feedbackPlugins: List<ElearningSubmissionPlugin>? = null
)

/**
 * Additional assignment data and attachments.
 *
 * @property attachments File attachments for the assignment.
 * @property activity The activity description text.
 * @property activityFormat The format of the activity description.
 */
@Serializable
data class ElearningAssignmentData(
    @SerialName("attachments")
    val attachments: ElearningAssignmentAttachments? = null,
    @SerialName("activity")
    val activity: String? = null,
    @SerialName("activityformat")
    val activityFormat: Int? = null
)

/**
 * File attachments for an assignment.
 *
 * @property introductionFiles Files attached to the introduction.
 * @property activityFiles Files attached to the activity.
 */
@Serializable
data class ElearningAssignmentAttachments(
    @SerialName("intro")
    val introductionFiles: List<ElearningFile>? = null,
    @SerialName("activity")
    val activityFiles: List<ElearningFile>? = null
)
