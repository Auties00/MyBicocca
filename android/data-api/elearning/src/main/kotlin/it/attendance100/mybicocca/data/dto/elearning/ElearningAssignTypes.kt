package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an assignment.
 */
@Serializable
data class ElearningAssignment(
    @SerialName("id")
    val id: Int,
    @SerialName("cmid")
    val cmId: Int? = null,
    @SerialName("course")
    val course: Int,
    @SerialName("name")
    val name: String,
    @SerialName("nosubmissions")
    val noSubmissions: Int? = null,
    @SerialName("submissiondrafts")
    val submissionDrafts: Int? = null,
    @SerialName("sendnotifications")
    val sendNotifications: Int? = null,
    @SerialName("sendlatenotifications")
    val sendLateNotifications: Int? = null,
    @SerialName("sendstudentnotifications")
    val sendStudentNotifications: Int? = null,
    @SerialName("duedate")
    val dueDate: Long? = null,
    @SerialName("allowsubmissionsfromdate")
    val allowSubmissionsFromDate: Long? = null,
    @SerialName("grade")
    val grade: Int? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("completionsubmit")
    val completionSubmit: Int? = null,
    @SerialName("cutoffdate")
    val cutOffDate: Long? = null,
    @SerialName("gradingduedate")
    val gradingDueDate: Long? = null,
    @SerialName("teamsubmission")
    val teamSubmission: Int? = null,
    @SerialName("requireallteammemberssubmit")
    val requireAllTeamMembersSubmit: Int? = null,
    @SerialName("teamsubmissiongroupingid")
    val teamSubmissionGroupingId: Int? = null,
    @SerialName("blindmarking")
    val blindMarking: Int? = null,
    @SerialName("hidegrader")
    val hideGrader: Int? = null,
    @SerialName("revealidentities")
    val revealIdentities: Int? = null,
    @SerialName("attemptreopenmethod")
    val attemptReopenMethod: String? = null,
    @SerialName("maxattempts")
    val maxAttempts: Int? = null,
    @SerialName("markingworkflow")
    val markingWorkflow: Int? = null,
    @SerialName("markingallocation")
    val markingAllocation: Int? = null,
    @SerialName("requiresubmissionstatement")
    val requireSubmissionStatement: Int? = null,
    @SerialName("preventsubmissionnotingroup")
    val preventSubmissionNotInGroup: Int? = null,
    @SerialName("intro")
    val intro: String? = null,
    @SerialName("introformat")
    val introFormat: Int? = null,
    @SerialName("introfiles")
    val introFiles: List<ElearningFile>? = null,
    @SerialName("introattachments")
    val introAttachments: List<ElearningFile>? = null,
    @SerialName("activity")
    val activity: String? = null,
    @SerialName("activityformat")
    val activityFormat: Int? = null,
    @SerialName("activityattachments")
    val activityAttachments: List<ElearningFile>? = null,
    @SerialName("timelimit")
    val timeLimit: Int? = null,
    @SerialName("submissionattachments")
    val submissionAttachments: Int? = null
) {
    val isOverdue: Boolean
        get() = dueDate != null && dueDate > 0 && System.currentTimeMillis() / 1000 > dueDate
}

/**
 * Represents a submission to an assignment.
 */
@Serializable
data class ElearningAssignSubmission(
    @SerialName("id")
    val id: Int,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("timestarted")
    val timeStarted: Long? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("groupid")
    val groupId: Int? = null,
    @SerialName("assignment")
    val assignment: Int? = null,
    @SerialName("latest")
    val latest: Int? = null,
    @SerialName("plugins")
    val plugins: List<ElearningSubmissionPlugin>? = null,
    @SerialName("gradingstatus")
    val gradingStatus: String? = null
) {
    val isNew: Boolean get() = status == "new"
    val isDraft: Boolean get() = status == "draft"
    val isSubmitted: Boolean get() = status == "submitted"
}

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

@Serializable
data class ElearningSubmissionFileArea(
    @SerialName("area")
    val area: String? = null,
    @SerialName("files")
    val files: List<ElearningFile>? = null
)

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
 * Represents a grade for an assignment.
 */
@Serializable
data class ElearningAssignGrade(
    @SerialName("id")
    val id: Int,
    @SerialName("assignment")
    val assignment: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("grader")
    val grader: Int? = null,
    @SerialName("grade")
    val grade: String? = null,
    @SerialName("gradefordisplay")
    val gradeForDisplay: String? = null
)

/**
 * Represents a submission status.
 */
@Serializable
data class ElearningSubmissionStatus(
    @SerialName("gradingsummary")
    val gradingSummary: ElearningGradingSummary? = null,
    @SerialName("lastattempt")
    val lastAttempt: ElearningLastAttempt? = null,
    @SerialName("feedback")
    val feedback: ElearningAssignFeedback? = null,
    @SerialName("previousattempts")
    val previousAttempts: List<ElearningPreviousAttempt>? = null,
    @SerialName("assignmentdata")
    val assignmentData: ElearningAssignmentData? = null,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
)

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

@Serializable
data class ElearningLastAttempt(
    @SerialName("submission")
    val submission: ElearningAssignSubmission? = null,
    @SerialName("teamsubmission")
    val teamSubmission: ElearningAssignSubmission? = null,
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
    val extensionDueDate: Long? = null,
    @SerialName("blindmarking")
    val blindMarking: Boolean? = null,
    @SerialName("gradingstatus")
    val gradingStatus: String? = null,
    @SerialName("usergroups")
    val userGroups: List<Int>? = null,
    @SerialName("timelimit")
    val timeLimit: Int? = null
)

@Serializable
data class ElearningAssignFeedback(
    @SerialName("grade")
    val grade: ElearningAssignGrade? = null,
    @SerialName("gradefordisplay")
    val gradeForDisplay: String? = null,
    @SerialName("gradeddate")
    val gradedDate: Long? = null,
    @SerialName("plugins")
    val plugins: List<ElearningSubmissionPlugin>? = null
)

@Serializable
data class ElearningPreviousAttempt(
    @SerialName("attemptnumber")
    val attemptNumber: Int? = null,
    @SerialName("submission")
    val submission: ElearningAssignSubmission? = null,
    @SerialName("grade")
    val grade: ElearningAssignGrade? = null,
    @SerialName("feedbackplugins")
    val feedbackPlugins: List<ElearningSubmissionPlugin>? = null
)

@Serializable
data class ElearningAssignmentData(
    @SerialName("attachments")
    val attachments: ElearningAssignAttachments? = null,
    @SerialName("activity")
    val activity: String? = null,
    @SerialName("activityformat")
    val activityFormat: Int? = null
)

@Serializable
data class ElearningAssignAttachments(
    @SerialName("intro")
    val intro: List<ElearningFile>? = null,
    @SerialName("activity")
    val activity: List<ElearningFile>? = null
)
