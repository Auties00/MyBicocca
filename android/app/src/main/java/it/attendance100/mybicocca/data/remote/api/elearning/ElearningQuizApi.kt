package it.attendance100.mybicocca.data.remote.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Quiz API
 *
 * Handles quiz modules, attempts, reviews, and summaries.
 *
 * ## Key Features
 *
 * - **Quizzes:** Get quizzes by course, access info.
 * - **Attempts:** Start, process, save, and view user attempts.
 * - **Reviews:** Get attempt review, summary, and combined options.
 * - **Data:** Get attempt data, feedback, and grades.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get quizzes
 * val quizzes = quizApi.getQuizzesByCourses(
 *     GetQuizzesByCoursesRequest(courseIds = listOf(courseId))
 * )
 *
 * // Start an attempt
 * val attempt = quizApi.startAttempt(
 *     StartAttemptRequest(quizId = quizId)
 * )
 * ```
 */
interface ElearningQuizApi {

    /**
     * Returns a list of quizzes for the given courses.
     *
     * @param request Course IDs.
     * @return List of quizzes.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_quizzes_by_courses")
    suspend fun getQuizzesByCourses(@Body request: GetQuizzesByCoursesRequest): Response<GetQuizzesByCoursesResponse>

    /**
     * Return a list of attempts for to the current user and quiz.
     *
     * @param request Quiz ID and status.
     * @return List of attempts.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_user_attempts")
    suspend fun getUserAttempts(@Body request: GetUserAttemptsRequest): Response<GetUserAttemptsResponse>

    /**
     * Returns review information for the given finished attempt.
     *
     * @param request Attempt ID.
     * @return Review details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_attempt_review")
    suspend fun getAttemptReview(@Body request: GetAttemptReviewRequest): Response<GetAttemptReviewResponse>

    /**
     * Returns a summary of the attempt.
     *
     * @param request Attempt ID.
     * @return Attempt summary.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_attempt_summary")
    suspend fun getAttemptSummary(@Body request: GetAttemptSummaryRequest): Response<GetAttemptSummaryResponse>

    /**
     * Returns information for the given attempt page.
     *
     * @param request Attempt ID and page number.
     * @return Attempt data.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_attempt_data")
    suspend fun getAttemptData(@Body request: GetAttemptDataRequest): Response<GetAttemptDataResponse>

    /**
     * Return access information for a given attempt.
     *
     * @param request Quiz ID and attempt ID.
     * @return Access information.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_attempt_access_information")
    suspend fun getAttemptAccessInformation(@Body request: GetAttemptAccessInformationRequest): Response<AttemptAccessInformation>

    /**
     * Combines the review options from a number of different sources.
     *
     * @param request Quiz IDs and user ID.
     * @return Review options.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_combined_review_options")
    suspend fun getCombinedReviewOptions(@Body request: GetCombinedReviewOptionsRequest): Response<GetCombinedReviewOptionsResponse>

    /**
     * Starts a new attempt.
     *
     * @param request Quiz ID.
     * @return The new attempt.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_start_attempt")
    suspend fun startAttempt(@Body request: StartAttemptRequest): Response<StartAttemptResponse>

    /**
     * Process responses during an attempt.
     *
     * @param request Attempt ID and responses data.
     * @return Processing state.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_process_attempt")
    suspend fun processAttempt(@Body request: ProcessAttemptRequest): Response<ProcessAttemptResponse>

    /**
     * Process responses during an attempt.
     *
     * @param request Attempt ID and responses data.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_save_attempt")
    suspend fun saveAttempt(@Body request: SaveAttemptRequest): Response<StatusWithWarningsResponse>

    /**
     * Log that the quiz was viewed.
     *
     * @param request Quiz ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_view_quiz")
    suspend fun viewQuiz(@Body request: ViewQuizRequest): Response<StatusWithWarningsResponse>

    /**
     * Log that the attempt was viewed.
     *
     * @param request Attempt ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_view_attempt")
    suspend fun viewAttempt(@Body request: ViewAttemptRequest): Response<StatusWithWarningsResponse>

    /**
     * Log that the attempt summary was viewed.
     *
     * @param request Attempt ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_view_attempt_summary")
    suspend fun viewAttemptSummary(@Body request: ViewAttemptSummaryRequest): Response<StatusWithWarningsResponse>

    /**
     * Log that the attempt review was viewed.
     *
     * @param request Attempt ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_view_attempt_review")
    suspend fun viewAttemptReview(@Body request: ViewAttemptReviewRequest): Response<StatusWithWarningsResponse>

    /**
     * Return access information for a given quiz.
     *
     * @param request Quiz ID.
     * @return Access information.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_quiz_access_information")
    suspend fun getQuizAccessInformation(@Body request: GetQuizAccessInformationRequest): Response<QuizAccessInformation>

    /**
     * Return the potential question types that would be required for a given quiz.
     *
     * @param request Quiz ID.
     * @return List of question types.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_quiz_required_qtypes")
    suspend fun getQuizRequiredQtypes(@Body request: GetQuizRequiredQtypesRequest): Response<GetQuizRequiredQtypesResponse>

    /**
     * Get the feedback text that should be shown to a student who got the given grade.
     *
     * @param request Quiz ID and grade.
     * @return Feedback text.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_quiz_feedback_for_grade")
    suspend fun getQuizFeedbackForGrade(@Body request: GetQuizFeedbackForGradeRequest): Response<GetQuizFeedbackForGradeResponse>

    /**
     * Get the best grade for a F0quiz.
     *
     * @param request Quiz ID and user ID.
     * @return Best grade details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_quiz_get_user_best_grade")
    suspend fun getUserBestGrade(@Body request: GetUserBestGradeRequest): Response<GetUserBestGradeResponse>
}
