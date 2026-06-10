package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetAssignmentsRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetAssignmentsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSubmissionStatusResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningRemoveSubmissionRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningResponseWarning
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningSaveSubmissionRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningSubmitForGradingRequest
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json

/**
 * API for assignment-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningAssignApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets assignments for courses.
     */
    suspend fun getAssignments(
        wsToken: String,
        courseIds: List<Int>,
        capabilities: List<String>? = null,
        includeNotEnrolledCourses: Boolean = false
    ): ElearningGetAssignmentsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetAssignmentsRequest(courseIds, capabilities, includeNotEnrolledCourses)
        )
    }

    /**
     * Gets assignments for a single course.
     */
    suspend fun getAssignmentsForCourse(wsToken: String, courseId: Int): ElearningGetAssignmentsResponse {
        return getAssignments(wsToken, listOf(courseId))
    }

    /**
     * Gets submission status for an assignment.
     */
    suspend fun getSubmissionStatus(
        wsToken: String,
        assignId: Int,
        userId: Int? = null,
        groupId: Int? = null
    ): ElearningGetSubmissionStatusResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetSubmissionStatusRequest(assignId, userId, groupId)
        )
    }

    /**
     * Saves the current user's submission as a draft (creating it if necessary).
     *
     * Pass plugin data only for the plugins enabled on the assignment. For file submissions,
     * [filesDraftItemId] is the draft-area id returned by [ElearningFileApi.uploadToDraftArea].
     * Moodle reports a failed save as a non-empty warnings array (with a 200 status) rather than
     * an exception, so this method translates a non-empty result into an [ElearningException].
     *
     * @throws ElearningException If Moodle returns any warning.
     */
    suspend fun saveSubmission(
        wsToken: String,
        assignmentId: Int,
        onlineText: String? = null,
        onlineTextDraftItemId: Int = 0,
        filesDraftItemId: Int? = null
    ) {
        val response = executeAuthenticatedRequest(
            wsToken,
            ElearningSaveSubmissionRequest(
                assignmentId = assignmentId,
                onlineText = onlineText,
                onlineTextDraftItemId = onlineTextDraftItemId,
                filesDraftItemId = filesDraftItemId
            )
        )
        response.warnings.throwIfPresent()
    }

    /**
     * Finalizes the current user's draft submission for grading.
     *
     * Irreversible from the student side; call only after explicit confirmation. When the
     * assignment requires a submission statement, [acceptSubmissionStatement] must be `true`.
     *
     * @throws ElearningException If Moodle returns any warning.
     */
    suspend fun submitForGrading(
        wsToken: String,
        assignmentId: Int,
        acceptSubmissionStatement: Boolean
    ) {
        val response = executeAuthenticatedRequest(
            wsToken,
            ElearningSubmitForGradingRequest(assignmentId, acceptSubmissionStatement)
        )
        response.warnings.throwIfPresent()
    }

    /**
     * Removes (discards) a user's submission, reverting it to an empty state.
     *
     * Requires both [assignmentId] and [userId] (the current user's Moodle id).
     *
     * @throws ElearningException If Moodle returns any warning.
     */
    suspend fun removeSubmission(
        wsToken: String,
        assignmentId: Int,
        userId: Int
    ) {
        val response = executeAuthenticatedRequest(
            wsToken,
            ElearningRemoveSubmissionRequest(assignmentId, userId)
        )
        response.warnings.throwIfPresent()
    }

    private fun List<ElearningResponseWarning>.throwIfPresent() {
        val warning = firstOrNull() ?: return
        throw ElearningException(
            warning.warningCode,
            warning.message ?: warning.warningCode ?: "Assignment operation failed"
        )
    }
}
