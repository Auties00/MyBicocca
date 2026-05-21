package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.elearning.dto.*
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserAttemptsRequest.AttemptStatusFilter
import kotlinx.serialization.json.Json

/**
 * API for quiz-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningQuizApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {
    /**
     * Gets quizzes from multiple courses.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseIds List of course IDs to get quizzes from
     * @return List of quizzes in the specified courses
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getQuizzes(wsToken: String, courseIds: List<Int>): ElearningGetQuizzesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetQuizzesRequest(courseIds))
    }

    /**
     * Gets quizzes from a single course.
     *
     * Convenience method that calls [getQuizzes] with a single course ID.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course ID to get quizzes from
     * @return List of quizzes in the course
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getQuizzesForCourse(wsToken: String, courseId: Int): ElearningGetQuizzesResponse {
        return getQuizzes(wsToken, listOf(courseId))
    }

    /**
     * Gets access information for a quiz.
     *
     * @param wsToken The web service token (32 characters)
     * @param quizId The quiz ID
     * @return Quiz access information
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getQuizAccessInfo(wsToken: String, quizId: Int): ElearningGetQuizAccessInfoResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetQuizAccessInfoRequest(quizId))
    }

    /**
     * Gets user attempts for a quiz.
     *
     * @param wsToken The web service token (32 characters)
     * @param quizId The quiz ID
     * @param userId Optional user ID (defaults to current user if null)
     * @param status Filter by attempt status (ALL, FINISHED, or UNFINISHED)
     * @param includePreviews Whether to include preview attempts
     * @return List of quiz attempts
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getUserAttempts(
        wsToken: String,
        quizId: Int,
        userId: Int? = null,
        status: AttemptStatusFilter = AttemptStatusFilter.ALL,
        includePreviews: Boolean = false
    ): ElearningGetUserAttemptsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetUserAttemptsRequest(quizId, userId, status, includePreviews)
        )
    }

    /**
     * Starts a new quiz attempt.
     *
     * @param wsToken The web service token (32 characters)
     * @param quizId The quiz ID
     * @param preflightData Optional preflight data (e.g., password)
     * @param forceNew Force creating a new attempt even if one exists
     * @return The started attempt
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails or quiz rules prevent starting
     */
    suspend fun startAttempt(
        wsToken: String,
        quizId: Int,
        preflightData: List<ElearningPreflightDataItem> = emptyList(),
        forceNew: Boolean = false
    ): ElearningStartAttemptResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningStartAttemptRequest(quizId, preflightData, forceNew)
        )
    }

    /**
     * Gets attempt data (questions) for a specific page.
     *
     * @param wsToken The web service token (32 characters)
     * @param attemptId The attempt ID
     * @param page The page number (0-indexed)
     * @param preflightData Optional preflight data
     * @return Attempt data with questions for the page
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getAttemptData(
        wsToken: String,
        attemptId: Int,
        page: Int = 0,
        preflightData: List<ElearningPreflightDataItem> = emptyList()
    ): ElearningGetAttemptDataResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetAttemptDataRequest(attemptId, page, preflightData)
        )
    }

    /**
     * Gets attempt summary (question statuses before submission).
     *
     * @param wsToken The web service token (32 characters)
     * @param attemptId The attempt ID
     * @param preflightData Optional preflight data
     * @return Attempt summary with question statuses
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getAttemptSummary(
        wsToken: String,
        attemptId: Int,
        preflightData: List<ElearningPreflightDataItem> = emptyList()
    ): ElearningGetAttemptSummaryResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetAttemptSummaryRequest(attemptId, preflightData)
        )
    }

    /**
     * Saves attempt answers (autosave).
     *
     * @param wsToken The web service token (32 characters)
     * @param attemptId The attempt ID
     * @param data Answer data to save
     * @param preflightData Optional preflight data
     * @return Save status (success/failure)
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun saveAttempt(
        wsToken: String,
        attemptId: Int,
        data: List<ElearningAttemptDataItem> = emptyList(),
        preflightData: List<ElearningPreflightDataItem> = emptyList()
    ): ElearningSaveAttemptResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningSaveAttemptRequest(attemptId, data, preflightData)
        )
    }

    /**
     * Processes (submits/finishes) an attempt.
     *
     * @param wsToken The web service token (32 characters)
     * @param attemptId The attempt ID
     * @param data Answer data to submit
     * @param finishAttempt Whether to finish the attempt (default: true)
     * @param timeUp Whether the time is up (triggers overdue handling)
     * @param preflightData Optional preflight data
     * @return The new attempt state
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun processAttempt(
        wsToken: String,
        attemptId: Int,
        data: List<ElearningAttemptDataItem> = emptyList(),
        finishAttempt: Boolean = true,
        timeUp: Boolean = false,
        preflightData: List<ElearningPreflightDataItem> = emptyList()
    ): ElearningProcessAttemptResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningProcessAttemptRequest(attemptId, data, finishAttempt, timeUp, preflightData)
        )
    }

    /**
     * Gets attempt review (after completion).
     *
     * @param wsToken The web service token (32 characters)
     * @param attemptId The attempt ID
     * @param page Optional page number to get questions for (null for all)
     * @return Attempt review with grade and questions
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getAttemptReview(
        wsToken: String,
        attemptId: Int,
        page: Int? = null
    ): ElearningGetAttemptReviewResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetAttemptReviewRequest(attemptId, page)
        )
    }

    /**
     * Gets user's best grade for a quiz.
     *
     * @param wsToken The web service token (32 characters)
     * @param quizId The quiz ID
     * @param userId Optional user ID (defaults to current user if null)
     * @return User's best grade information
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getUserBestGrade(
        wsToken: String,
        quizId: Int,
        userId: Int? = null
    ): ElearningGetUserBestGradeResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetUserBestGradeRequest(quizId, userId)
        )
    }
}
