package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve quizzes for specified courses.
 *
 * @property courseIds The list of course identifiers to retrieve quizzes for.
 */
@Serializable
class ElearningGetQuizzesRequest(
    private val courseIds: List<Int>
) : ElearningRequest<ElearningGetQuizzesResponse> {
    override val functionName: String
        get() = "mod_quiz_get_quizzes_by_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, courseId ->
            formData.append("courseids[$index]", courseId.toString())
        }
    }
}

/**
 * Response containing quizzes for the requested courses.
 *
 * @property quizzes The list of quizzes found.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetQuizzesResponse(
    @SerialName("quizzes")
    val quizzes: List<ElearningQuiz>,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to retrieve access information for a specific quiz.
 *
 * @property quizId The unique identifier of the quiz.
 */
@Serializable
class ElearningGetQuizAccessInfoRequest(
    private val quizId: Int
) : ElearningRequest<ElearningGetQuizAccessInfoResponse> {
    override val functionName: String
        get() = "mod_quiz_get_quiz_access_information"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
    }
}

/**
 * Response containing access permissions and rules for a quiz.
 *
 * @property canAttempt Whether the user can attempt the quiz.
 * @property canManage Whether the user can manage the quiz.
 * @property canPreview Whether the user can preview the quiz.
 * @property canReviewMyAttempts Whether the user can review their own attempts.
 * @property canViewReports Whether the user can view quiz reports.
 * @property accessRules List of access rule plugin names that apply.
 * @property activeRuleNames List of active access rule names.
 * @property preventAccessReasons List of reasons preventing access.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetQuizAccessInfoResponse(
    @SerialName("canattempt")
    val canAttempt: Boolean,
    @SerialName("canmanage")
    val canManage: Boolean,
    @SerialName("canpreview")
    val canPreview: Boolean,
    @SerialName("canreviewmyattempts")
    val canReviewMyAttempts: Boolean,
    @SerialName("canviewreports")
    val canViewReports: Boolean,
    @SerialName("accessrules")
    val accessRules: List<String>? = null,
    @SerialName("activerulenames")
    val activeRuleNames: List<String>? = null,
    @SerialName("preventaccessreasons")
    val preventAccessReasons: List<String>? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to retrieve a user's quiz attempts.
 *
 * @property quizId The unique identifier of the quiz.
 * @property userId Optional user identifier. If null, attempts for the current user are retrieved.
 * @property status The status filter for attempts.
 * @property includePreviews Whether to include preview attempts.
 */
@Serializable
class ElearningGetUserAttemptsRequest(
    private val quizId: Int,
    private val userId: Int? = null,
    private val status: AttemptStatusFilter = AttemptStatusFilter.ALL,
    private val includePreviews: Boolean = false
) : ElearningRequest<ElearningGetUserAttemptsResponse> {
    override val functionName: String
        get() = "mod_quiz_get_user_attempts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        if (userId != null) {
            formData.append("userid", userId.toString())
        }
        formData.append("status", status.value)
        formData.append("includepreviews", if (includePreviews) "1" else "0")
    }

    /**
     * Filter options for quiz attempt status.
     *
     * @property value The string value sent to the API.
     */
    enum class AttemptStatusFilter(val value: String) {
        /** Include all attempts regardless of status. */
        ALL("all"),
        /** Include only finished attempts. */
        FINISHED("finished"),
        /** Include only unfinished attempts. */
        UNFINISHED("unfinished")
    }
}

/**
 * Response containing a user's quiz attempts.
 *
 * @property attempts The list of quiz attempts.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetUserAttemptsResponse(
    @SerialName("attempts")
    val attempts: List<ElearningQuizAttempt>,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to start a new quiz attempt.
 *
 * @property quizId The unique identifier of the quiz.
 * @property preflightData Data required by access rules before starting.
 * @property forceNew Whether to force creating a new attempt even if one exists.
 */
@Serializable
class ElearningStartAttemptRequest(
    private val quizId: Int,
    private val preflightData: List<ElearningPreflightDataItem> = emptyList(),
    private val forceNew: Boolean = false
) : ElearningRequest<ElearningStartAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_start_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        formData.append("forcenew", if (forceNew) "1" else "0")
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

/**
 * Response after starting a new quiz attempt.
 *
 * @property attempt The created quiz attempt.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningStartAttemptResponse(
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to retrieve data for a specific page of a quiz attempt.
 *
 * @property attemptId The unique identifier of the attempt.
 * @property page The page number to retrieve (0-indexed).
 * @property preflightData Data required by access rules.
 */
@Serializable
class ElearningGetAttemptDataRequest(
    private val attemptId: Int,
    private val page: Int = 0,
    private val preflightData: List<ElearningPreflightDataItem> = emptyList()
) : ElearningRequest<ElearningGetAttemptDataResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_data"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        formData.append("page", page.toString())
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

/**
 * Response containing quiz attempt data with questions for a specific page.
 *
 * @property attempt The quiz attempt details.
 * @property messages Any messages to display to the user.
 * @property nextPage The next page number, or -1 if this is the last page.
 * @property questions The list of questions on this page.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetAttemptDataResponse(
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("messages")
    val messages: List<String>? = null,
    @SerialName("nextpage")
    val nextPage: Int? = null,
    @SerialName("questions")
    val questions: List<ElearningQuizQuestion>,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to retrieve a summary of all questions in a quiz attempt.
 *
 * @property attemptId The unique identifier of the attempt.
 * @property preflightData Data required by access rules.
 */
@Serializable
class ElearningGetAttemptSummaryRequest(
    private val attemptId: Int,
    private val preflightData: List<ElearningPreflightDataItem> = emptyList()
) : ElearningRequest<ElearningGetAttemptSummaryResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_summary"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

/**
 * Response containing a summary of all questions in a quiz attempt.
 *
 * @property questions The list of question summaries.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetAttemptSummaryResponse(
    @SerialName("questions")
    val questions: List<ElearningQuestionSummary>,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to save responses for a quiz attempt without finishing.
 *
 * @property attemptId The unique identifier of the attempt.
 * @property data The list of response data items to save.
 * @property preflightData Data required by access rules.
 */
@Serializable
class ElearningSaveAttemptRequest(
    private val attemptId: Int,
    private val data: List<ElearningAttemptDataItem> = emptyList(),
    private val preflightData: List<ElearningPreflightDataItem> = emptyList()
) : ElearningRequest<ElearningSaveAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_save_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        data.forEachIndexed { index, item ->
            formData.append("data[$index][name]", item.name)
            formData.append("data[$index][value]", item.value)
        }
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

/**
 * Response after saving quiz attempt data.
 *
 * @property status Whether the save operation was successful.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningSaveAttemptResponse(
    @SerialName("status")
    val status: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to process and optionally finish a quiz attempt.
 *
 * @property attemptId The unique identifier of the attempt.
 * @property data The list of response data items to submit.
 * @property finishAttempt Whether to finish the attempt after processing.
 * @property timeUp Whether the time limit has expired.
 * @property preflightData Data required by access rules.
 */
@Serializable
class ElearningProcessAttemptRequest(
    private val attemptId: Int,
    private val data: List<ElearningAttemptDataItem> = emptyList(),
    private val finishAttempt: Boolean = true,
    private val timeUp: Boolean = false,
    private val preflightData: List<ElearningPreflightDataItem> = emptyList()
) : ElearningRequest<ElearningProcessAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_process_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        formData.append("finishattempt", if (finishAttempt) "1" else "0")
        formData.append("timeup", if (timeUp) "1" else "0")
        data.forEachIndexed { index, item ->
            formData.append("data[$index][name]", item.name)
            formData.append("data[$index][value]", item.value)
        }
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

/**
 * Response after processing a quiz attempt.
 *
 * @property state The current state of the attempt after processing.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningProcessAttemptResponse(
    @SerialName("state")
    val state: String,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse {
    /**
     * Whether the attempt is now in a finished state.
     */
    val isFinished: Boolean
        get() = state == ElearningQuizAttempt.STATE_FINISHED
}

/**
 * Request to retrieve a review of a finished quiz attempt.
 *
 * @property attemptId The unique identifier of the attempt.
 * @property page Optional page number to retrieve specific questions.
 */
@Serializable
class ElearningGetAttemptReviewRequest(
    private val attemptId: Int,
    private val page: Int? = null
) : ElearningRequest<ElearningGetAttemptReviewResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_review"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        if (page != null) {
            formData.append("page", page.toString())
        }
    }
}

/**
 * Response containing a review of a finished quiz attempt.
 *
 * @property grade The formatted grade string for the attempt.
 * @property attempt The quiz attempt details.
 * @property additionalData Additional data items to display in the review.
 * @property questions The list of questions with answers and feedback.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetAttemptReviewResponse(
    @SerialName("grade")
    val grade: String? = null,
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("additionaldata")
    val additionalData: List<ElearningAdditionalData>? = null,
    @SerialName("questions")
    val questions: List<ElearningQuizQuestion>,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Request to retrieve a user's best grade for a quiz.
 *
 * @property quizId The unique identifier of the quiz.
 * @property userId Optional user identifier. If null, the current user's best grade is retrieved.
 */
@Serializable
class ElearningGetUserBestGradeRequest(
    private val quizId: Int,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetUserBestGradeResponse> {
    override val functionName: String
        get() = "mod_quiz_get_user_best_grade"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        if (userId != null) {
            formData.append("userid", userId.toString())
        }
    }
}

/**
 * Response containing a user's best grade for a quiz.
 *
 * @property hasGrade Whether the user has a grade for this quiz.
 * @property grade The user's best grade value.
 * @property gradeToPass The minimum grade required to pass.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetUserBestGradeResponse(
    @SerialName("hasgrade")
    val hasGrade: Boolean,
    @SerialName("grade")
    val grade: Double? = null,
    @SerialName("gradetopass")
    val gradeToPass: Double? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null
) : ElearningResponse

/**
 * Represents a quiz activity in a course.
 *
 * @property id The unique identifier of the quiz.
 * @property courseId The identifier of the course containing this quiz.
 * @property courseModuleId The course module identifier.
 * @property name The name of the quiz.
 * @property introduction The introduction text for the quiz.
 * @property introductionFormat The format of the introduction (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 * @property introductionFiles Files attached to the introduction.
 * @property openTimestamp Unix timestamp when the quiz opens.
 * @property closeTimestamp Unix timestamp when the quiz closes.
 * @property timeLimitSeconds Time limit for attempts in seconds.
 * @property overdueHandling How overdue attempts are handled (autosubmit, graceperiod, autoabandon).
 * @property gracePeriodSeconds Grace period in seconds for overdue handling.
 * @property preferredBehaviour The question behaviour mode.
 * @property canRedoQuestions Whether questions can be redone within an attempt.
 * @property maximumAttempts Maximum number of attempts allowed (0 = unlimited).
 * @property attemptOnLastEnabled Whether each attempt builds on the last.
 * @property gradeMethod The method for calculating the final grade.
 * @property decimalPoints Number of decimal places for grades.
 * @property questionDecimalPoints Number of decimal places for question grades.
 * @property reviewAttempt When attempt summary can be reviewed.
 * @property reviewCorrectness When answer correctness can be reviewed.
 * @property reviewMaximumMarks When maximum marks can be reviewed.
 * @property reviewMarks When marks can be reviewed.
 * @property reviewSpecificFeedback When specific feedback can be reviewed.
 * @property reviewGeneralFeedback When general feedback can be reviewed.
 * @property reviewRightAnswer When correct answers can be reviewed.
 * @property reviewOverallFeedback When overall feedback can be reviewed.
 * @property questionsPerPage Number of questions displayed per page.
 * @property navigationMethod Navigation method (free or sequential).
 * @property shuffleAnswers Whether to shuffle answer options.
 * @property sumGrades Total of all question grades.
 * @property maximumGrade Maximum grade for the quiz.
 * @property hasQuestions Whether the quiz has questions (1 = yes, 0 = no).
 * @property hasFeedback Whether the quiz has feedback defined.
 * @property sectionNumber The section number within the course.
 * @property visible Whether the quiz is visible to students.
 * @property groupMode The group mode setting.
 * @property groupingId The grouping identifier.
 * @property autosavePeriodSeconds How often to autosave in seconds.
 * @property allowOfflineAttempts Whether offline attempts are allowed.
 */
@Serializable
data class ElearningQuiz(
    @SerialName("id")
    val id: Int,
    @SerialName("course")
    val courseId: Int,
    @SerialName("coursemodule")
    val courseModuleId: Int,
    @SerialName("name")
    val name: String,
    @SerialName("intro")
    val introduction: String? = null,
    @SerialName("introformat")
    val introductionFormat: Int? = null,
    @SerialName("introfiles")
    val introductionFiles: List<ElearningFile>? = null,
    @SerialName("timeopen")
    val openTimestamp: Long? = null,
    @SerialName("timeclose")
    val closeTimestamp: Long? = null,
    @SerialName("timelimit")
    val timeLimitSeconds: Int? = null,
    @SerialName("overduehandling")
    val overdueHandling: String? = null,
    @SerialName("graceperiod")
    val gracePeriodSeconds: Int? = null,
    @SerialName("preferredbehaviour")
    val preferredBehaviour: String? = null,
    @SerialName("canredoquestions")
    val canRedoQuestions: Int? = null,
    @SerialName("attempts")
    val maximumAttempts: Int? = null,
    @SerialName("attemptonlast")
    val attemptOnLastEnabled: Int? = null,
    @SerialName("grademethod")
    val gradeMethod: Int? = null,
    @SerialName("decimalpoints")
    val decimalPoints: Int? = null,
    @SerialName("questiondecimalpoints")
    val questionDecimalPoints: Int? = null,
    @SerialName("reviewattempt")
    val reviewAttempt: Int? = null,
    @SerialName("reviewcorrectness")
    val reviewCorrectness: Int? = null,
    @SerialName("reviewmaxmarks")
    val reviewMaximumMarks: Int? = null,
    @SerialName("reviewmarks")
    val reviewMarks: Int? = null,
    @SerialName("reviewspecificfeedback")
    val reviewSpecificFeedback: Int? = null,
    @SerialName("reviewgeneralfeedback")
    val reviewGeneralFeedback: Int? = null,
    @SerialName("reviewrightanswer")
    val reviewRightAnswer: Int? = null,
    @SerialName("reviewoverallfeedback")
    val reviewOverallFeedback: Int? = null,
    @SerialName("questionsperpage")
    val questionsPerPage: Int? = null,
    @SerialName("navmethod")
    val navigationMethod: String? = null,
    @SerialName("shuffleanswers")
    val shuffleAnswers: Int? = null,
    @SerialName("sumgrades")
    val sumGrades: Double? = null,
    @SerialName("grade")
    val maximumGrade: Double? = null,
    @SerialName("hasquestions")
    val hasQuestions: Int? = null,
    @SerialName("hasfeedback")
    val hasFeedback: Int? = null,
    @SerialName("section")
    val sectionNumber: Int? = null,
    @SerialName("visible")
    val visible: Boolean? = null,
    @SerialName("groupmode")
    val groupMode: Int? = null,
    @SerialName("groupingid")
    val groupingId: Int? = null,
    @SerialName("autosaveperiod")
    val autosavePeriodSeconds: Int? = null,
    @SerialName("allowofflineattempts")
    val allowOfflineAttempts: Int? = null
) {
    companion object {
        /** Grade method: use the highest attempt grade. */
        const val GRADE_METHOD_HIGHEST = 1
        /** Grade method: use the average of all attempt grades. */
        const val GRADE_METHOD_AVERAGE = 2
        /** Grade method: use the first attempt grade. */
        const val GRADE_METHOD_FIRST = 3
        /** Grade method: use the last attempt grade. */
        const val GRADE_METHOD_LAST = 4
    }
}

/**
 * Represents an attempt at a quiz.
 *
 * @property id The unique identifier of the attempt.
 * @property quizId The identifier of the quiz.
 * @property userId The identifier of the user who made the attempt.
 * @property attemptNumber The attempt number for this user.
 * @property uniqueId A unique identifier for the question usage.
 * @property layout The layout string defining question order and pages.
 * @property currentPage The current page of the attempt.
 * @property isPreview Whether this is a preview attempt (1 = yes, 0 = no).
 * @property state The current state of the attempt.
 * @property startTimestamp Unix timestamp when the attempt started.
 * @property finishTimestamp Unix timestamp when the attempt finished.
 * @property modifiedTimestamp Unix timestamp when the attempt was last modified.
 * @property modifiedOfflineTimestamp Unix timestamp of last offline modification.
 * @property checkStateTimestamp Unix timestamp when the state should be checked.
 * @property sumGrades The sum of grades for all answered questions.
 * @property gradedNotificationSentTimestamp Unix timestamp when graded notification was sent.
 */
@Serializable
data class ElearningQuizAttempt(
    @SerialName("id")
    val id: Int,
    @SerialName("quiz")
    val quizId: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attempt")
    val attemptNumber: Int? = null,
    @SerialName("uniqueid")
    val uniqueId: Int? = null,
    @SerialName("layout")
    val layout: String? = null,
    @SerialName("currentpage")
    val currentPage: Int? = null,
    @SerialName("preview")
    val isPreview: Int? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("timestart")
    val startTimestamp: Long? = null,
    @SerialName("timefinish")
    val finishTimestamp: Long? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
    @SerialName("timemodifiedoffline")
    val modifiedOfflineTimestamp: Long? = null,
    @SerialName("timecheckstate")
    val checkStateTimestamp: Long? = null,
    @SerialName("sumgrades")
    val sumGrades: Double? = null,
    @SerialName("gradednotificationsenttime")
    val gradedNotificationSentTimestamp: Long? = null
) {
    companion object {
        /** The attempt is currently in progress. */
        const val STATE_IN_PROGRESS = "inprogress"
        /** The attempt is overdue and awaiting submission. */
        const val STATE_OVERDUE = "overdue"
        /** The attempt has been finished and submitted. */
        const val STATE_FINISHED = "finished"
        /** The attempt was abandoned without submission. */
        const val STATE_ABANDONED = "abandoned"
    }

    /** Whether the attempt has been finished and submitted. */
    val isFinished: Boolean
        get() = state == STATE_FINISHED

    /** Whether the attempt is overdue. */
    val isOverdue: Boolean
        get() = state == STATE_OVERDUE

    /** Whether the attempt is currently in progress. */
    val isInProgress: Boolean
        get() = state == STATE_IN_PROGRESS

    /** Whether the attempt was abandoned. */
    val isAbandoned: Boolean
        get() = state == STATE_ABANDONED
}

/**
 * Represents a question within a quiz attempt with full details.
 *
 * @property slot The position of the question in the quiz.
 * @property type The question type identifier.
 * @property page The page number where this question appears.
 * @property questionNumber The displayed question number.
 * @property number The numeric question number.
 * @property html The rendered HTML of the question.
 * @property responseFileAreas Areas for file response uploads.
 * @property sequenceCheck The sequence check value for submission validation.
 * @property lastActionTimestamp Unix timestamp of the last action on this question.
 * @property hasAutosavedStep Whether there is an autosaved step.
 * @property flagged Whether the question has been flagged by the user.
 * @property state The current state of the question.
 * @property status The status description of the question.
 * @property blockedByPrevious Whether the question is blocked by a previous unanswered question.
 * @property mark The mark awarded for this question.
 * @property maximumMark The maximum possible mark for this question.
 * @property settings Question-specific settings as JSON.
 */
@Serializable
data class ElearningQuizQuestion(
    @SerialName("slot")
    val slot: Int,
    @SerialName("type")
    val type: String? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("questionnumber")
    val questionNumber: String? = null,
    @SerialName("number")
    val number: Int? = null,
    @SerialName("html")
    val html: String? = null,
    @SerialName("responsefileareas")
    val responseFileAreas: List<ElearningResponseFileArea>? = null,
    @SerialName("sequencecheck")
    val sequenceCheck: Int? = null,
    @SerialName("lastactiontime")
    val lastActionTimestamp: Long? = null,
    @SerialName("hasautosavedstep")
    val hasAutosavedStep: Boolean? = null,
    @SerialName("flagged")
    val flagged: Boolean? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("blockedbyprevious")
    val blockedByPrevious: Boolean? = null,
    @SerialName("mark")
    val mark: String? = null,
    @SerialName("maxmark")
    val maximumMark: Double? = null,
    @SerialName("settings")
    val settings: String? = null
)

/**
 * Represents a file area for question responses.
 *
 * @property area The name of the file area.
 * @property files The list of files in this area.
 */
@Serializable
data class ElearningResponseFileArea(
    @SerialName("area")
    val area: String? = null,
    @SerialName("files")
    val files: List<ElearningFile>? = null
)

/**
 * Summary information about a question in a quiz attempt.
 *
 * @property slot The position of the question in the quiz.
 * @property type The question type identifier.
 * @property page The page number where this question appears.
 * @property questionNumber The displayed question number.
 * @property number The numeric question number.
 * @property flagged Whether the question has been flagged by the user.
 * @property state The current state of the question.
 * @property status The status description of the question.
 * @property blockedByPrevious Whether the question is blocked by a previous unanswered question.
 * @property mark The mark awarded for this question.
 * @property maximumMark The maximum possible mark for this question.
 */
@Serializable
data class ElearningQuestionSummary(
    @SerialName("slot")
    val slot: Int,
    @SerialName("type")
    val type: String? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("questionnumber")
    val questionNumber: String? = null,
    @SerialName("number")
    val number: Int? = null,
    @SerialName("flagged")
    val flagged: Boolean? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("blockedbyprevious")
    val blockedByPrevious: Boolean? = null,
    @SerialName("mark")
    val mark: String? = null,
    @SerialName("maxmark")
    val maximumMark: Double? = null
)

/**
 * Represents data required by access rules before starting or continuing a quiz.
 *
 * @property name The name of the preflight data item.
 * @property value The value of the preflight data item.
 */
@Serializable
data class ElearningPreflightDataItem(
    val name: String,
    val value: String
)

/**
 * Represents a name-value pair for quiz attempt response data.
 *
 * @property name The field name for the response.
 * @property value The response value.
 */
@Serializable
data class ElearningAttemptDataItem(
    val name: String,
    val value: String
)

/**
 * Represents additional data displayed in quiz reviews.
 *
 * @property id The identifier of the additional data item.
 * @property title The title of the data item.
 * @property content The content of the data item (typically HTML).
 */
@Serializable
data class ElearningAdditionalData(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("content")
    val content: String? = null
)
