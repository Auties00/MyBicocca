package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizAddRandomQuestions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizAddRandomQuestionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptData200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptReview200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptReviewRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptSummary200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetAttemptSummaryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetCombinedReviewOptions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetCombinedReviewOptionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizFeedbackForGrade200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizFeedbackForGradeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizRequiredQtypes200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetQuizzesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetReopenAttemptConfirmationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetUserAttempts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetUserAttemptsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetUserBestGrade200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizGetUserBestGradeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizProcessAttempt200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizProcessAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizReopenAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizSaveAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizSetQuestionVersion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizSetQuestionVersionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizStartAttempt200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizStartAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizUpdateFilterConditionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizViewAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModQuizViewAttemptReviewRequest

interface ModQuizApi {
    /**
     * POST mod_quiz_add_random_questions
     * Add a number of random questions to a quiz.
     * Add a number of random questions to a quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizAddRandomQuestionsRequest 
     * @return [Call]<[ElearningModQuizAddRandomQuestions200Response]>
     */
    @POST("mod_quiz_add_random_questions")
    fun modQuizAddRandomQuestions(@Body elearningModQuizAddRandomQuestionsRequest: ElearningModQuizAddRandomQuestionsRequest): Call<ElearningModQuizAddRandomQuestions200Response>

    /**
     * POST mod_quiz_get_attempt_access_information
     * Return access information for a given attempt in a quiz.
     * Return access information for a given attempt in a quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetAttemptAccessInformationRequest 
     * @return [Call]<[ElearningModQuizGetAttemptAccessInformation200Response]>
     */
    @POST("mod_quiz_get_attempt_access_information")
    fun modQuizGetAttemptAccessInformation(@Body elearningModQuizGetAttemptAccessInformationRequest: ElearningModQuizGetAttemptAccessInformationRequest): Call<ElearningModQuizGetAttemptAccessInformation200Response>

    /**
     * POST mod_quiz_get_attempt_data
     * Returns information for the given attempt page for a quiz attempt in progress.
     * Returns information for the given attempt page for a quiz attempt in progress.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetAttemptDataRequest 
     * @return [Call]<[ElearningModQuizGetAttemptData200Response]>
     */
    @POST("mod_quiz_get_attempt_data")
    fun modQuizGetAttemptData(@Body elearningModQuizGetAttemptDataRequest: ElearningModQuizGetAttemptDataRequest): Call<ElearningModQuizGetAttemptData200Response>

    /**
     * POST mod_quiz_get_attempt_review
     * Returns review information for the given finished attempt, can be used by users or teachers.
     * Returns review information for the given finished attempt, can be used by users or teachers.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetAttemptReviewRequest 
     * @return [Call]<[ElearningModQuizGetAttemptReview200Response]>
     */
    @POST("mod_quiz_get_attempt_review")
    fun modQuizGetAttemptReview(@Body elearningModQuizGetAttemptReviewRequest: ElearningModQuizGetAttemptReviewRequest): Call<ElearningModQuizGetAttemptReview200Response>

    /**
     * POST mod_quiz_get_attempt_summary
     * Returns a summary of a quiz attempt before it is submitted.
     * Returns a summary of a quiz attempt before it is submitted.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetAttemptSummaryRequest 
     * @return [Call]<[ElearningModQuizGetAttemptSummary200Response]>
     */
    @POST("mod_quiz_get_attempt_summary")
    fun modQuizGetAttemptSummary(@Body elearningModQuizGetAttemptSummaryRequest: ElearningModQuizGetAttemptSummaryRequest): Call<ElearningModQuizGetAttemptSummary200Response>

    /**
     * POST mod_quiz_get_combined_review_options
     * Combines the review options from a number of different quiz attempts.
     * Combines the review options from a number of different quiz attempts.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetCombinedReviewOptionsRequest 
     * @return [Call]<[ElearningModQuizGetCombinedReviewOptions200Response]>
     */
    @POST("mod_quiz_get_combined_review_options")
    fun modQuizGetCombinedReviewOptions(@Body elearningModQuizGetCombinedReviewOptionsRequest: ElearningModQuizGetCombinedReviewOptionsRequest): Call<ElearningModQuizGetCombinedReviewOptions200Response>

    /**
     * POST mod_quiz_get_quiz_access_information
     * Return access information for a given quiz.
     * Return access information for a given quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetQuizAccessInformationRequest 
     * @return [Call]<[ElearningModQuizGetQuizAccessInformation200Response]>
     */
    @POST("mod_quiz_get_quiz_access_information")
    fun modQuizGetQuizAccessInformation(@Body elearningModQuizGetQuizAccessInformationRequest: ElearningModQuizGetQuizAccessInformationRequest): Call<ElearningModQuizGetQuizAccessInformation200Response>

    /**
     * POST mod_quiz_get_quiz_feedback_for_grade
     * Get the feedback text that should be show to a student who got the given grade in the given quiz.
     * Get the feedback text that should be show to a student who got the given grade in the given quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetQuizFeedbackForGradeRequest 
     * @return [Call]<[ElearningModQuizGetQuizFeedbackForGrade200Response]>
     */
    @POST("mod_quiz_get_quiz_feedback_for_grade")
    fun modQuizGetQuizFeedbackForGrade(@Body elearningModQuizGetQuizFeedbackForGradeRequest: ElearningModQuizGetQuizFeedbackForGradeRequest): Call<ElearningModQuizGetQuizFeedbackForGrade200Response>

    /**
     * POST mod_quiz_get_quiz_required_qtypes
     * Return the potential question types that would be required for a given quiz.
     * Return the potential question types that would be required for a given quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetQuizAccessInformationRequest 
     * @return [Call]<[ElearningModQuizGetQuizRequiredQtypes200Response]>
     */
    @POST("mod_quiz_get_quiz_required_qtypes")
    fun modQuizGetQuizRequiredQtypes(@Body elearningModQuizGetQuizAccessInformationRequest: ElearningModQuizGetQuizAccessInformationRequest): Call<ElearningModQuizGetQuizRequiredQtypes200Response>

    /**
     * POST mod_quiz_get_quizzes_by_courses
     * Returns a list of quizzes in a provided list of courses,                             if no list is provided all quizzes that the user can view will be returned.
     * Returns a list of quizzes in a provided list of courses,                             if no list is provided all quizzes that the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModQuizGetQuizzesByCourses200Response]>
     */
    @POST("mod_quiz_get_quizzes_by_courses")
    fun modQuizGetQuizzesByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModQuizGetQuizzesByCourses200Response>

    /**
     * POST mod_quiz_get_reopen_attempt_confirmation
     * Verify it is OK to re-open a given quiz attempt, and if so, return a suitable confirmation message.
     * Verify it is OK to re-open a given quiz attempt, and if so, return a suitable confirmation message.
     * Responses:
     *  - 200: Confirmation to show the user before the attempt is reopened.
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetReopenAttemptConfirmationRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_quiz_get_reopen_attempt_confirmation")
    fun modQuizGetReopenAttemptConfirmation(@Body elearningModQuizGetReopenAttemptConfirmationRequest: ElearningModQuizGetReopenAttemptConfirmationRequest): Call<kotlin.Any>

    /**
     * POST mod_quiz_get_user_attempts
     * Return a list of attempts for the given quiz and user.
     * Return a list of attempts for the given quiz and user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetUserAttemptsRequest 
     * @return [Call]<[ElearningModQuizGetUserAttempts200Response]>
     */
    @POST("mod_quiz_get_user_attempts")
    fun modQuizGetUserAttempts(@Body elearningModQuizGetUserAttemptsRequest: ElearningModQuizGetUserAttemptsRequest): Call<ElearningModQuizGetUserAttempts200Response>

    /**
     * POST mod_quiz_get_user_best_grade
     * Get the best current grade for the given user on a quiz.
     * Get the best current grade for the given user on a quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetUserBestGradeRequest 
     * @return [Call]<[ElearningModQuizGetUserBestGrade200Response]>
     */
    @POST("mod_quiz_get_user_best_grade")
    fun modQuizGetUserBestGrade(@Body elearningModQuizGetUserBestGradeRequest: ElearningModQuizGetUserBestGradeRequest): Call<ElearningModQuizGetUserBestGrade200Response>

    /**
     * POST mod_quiz_process_attempt
     * Process responses during an attempt at a quiz and also deals with attempts finishing.
     * Process responses during an attempt at a quiz and also deals with attempts finishing.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizProcessAttemptRequest 
     * @return [Call]<[ElearningModQuizProcessAttempt200Response]>
     */
    @POST("mod_quiz_process_attempt")
    fun modQuizProcessAttempt(@Body elearningModQuizProcessAttemptRequest: ElearningModQuizProcessAttemptRequest): Call<ElearningModQuizProcessAttempt200Response>

    /**
     * POST mod_quiz_reopen_attempt
     * Re-open an attempt that is currently in the never submitted state.
     * Re-open an attempt that is currently in the never submitted state.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizReopenAttemptRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_quiz_reopen_attempt")
    fun modQuizReopenAttempt(@Body elearningModQuizReopenAttemptRequest: ElearningModQuizReopenAttemptRequest): Call<kotlin.Any>

    /**
     * POST mod_quiz_save_attempt
     * Processes save requests during the quiz.                             This function is intended for the quiz auto-save feature.
     * Processes save requests during the quiz.                             This function is intended for the quiz auto-save feature.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizSaveAttemptRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_quiz_save_attempt")
    fun modQuizSaveAttempt(@Body elearningModQuizSaveAttemptRequest: ElearningModQuizSaveAttemptRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_quiz_set_question_version
     * Set the version of question that would be required for a given quiz.
     * Set the version of question that would be required for a given quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizSetQuestionVersionRequest 
     * @return [Call]<[ElearningModQuizSetQuestionVersion200Response]>
     */
    @POST("mod_quiz_set_question_version")
    fun modQuizSetQuestionVersion(@Body elearningModQuizSetQuestionVersionRequest: ElearningModQuizSetQuestionVersionRequest): Call<ElearningModQuizSetQuestionVersion200Response>

    /**
     * POST mod_quiz_start_attempt
     * Starts a new attempt at a quiz.
     * Starts a new attempt at a quiz.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizStartAttemptRequest 
     * @return [Call]<[ElearningModQuizStartAttempt200Response]>
     */
    @POST("mod_quiz_start_attempt")
    fun modQuizStartAttempt(@Body elearningModQuizStartAttemptRequest: ElearningModQuizStartAttemptRequest): Call<ElearningModQuizStartAttempt200Response>

    /**
     * POST mod_quiz_update_filter_condition
     * Update filter condition for a random question slot.
     * Update filter condition for a random question slot.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizUpdateFilterConditionRequest 
     * @return [Call]<[ElearningModQuizAddRandomQuestions200Response]>
     */
    @POST("mod_quiz_update_filter_condition")
    fun modQuizUpdateFilterCondition(@Body elearningModQuizUpdateFilterConditionRequest: ElearningModQuizUpdateFilterConditionRequest): Call<ElearningModQuizAddRandomQuestions200Response>

    /**
     * POST mod_quiz_view_attempt
     * Trigger the attempt viewed event.
     * Trigger the attempt viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizViewAttemptRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_quiz_view_attempt")
    fun modQuizViewAttempt(@Body elearningModQuizViewAttemptRequest: ElearningModQuizViewAttemptRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_quiz_view_attempt_review
     * Trigger the attempt reviewed event.
     * Trigger the attempt reviewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizViewAttemptReviewRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_quiz_view_attempt_review")
    fun modQuizViewAttemptReview(@Body elearningModQuizViewAttemptReviewRequest: ElearningModQuizViewAttemptReviewRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_quiz_view_attempt_summary
     * Trigger the attempt summary viewed event.
     * Trigger the attempt summary viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetAttemptSummaryRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_quiz_view_attempt_summary")
    fun modQuizViewAttemptSummary(@Body elearningModQuizGetAttemptSummaryRequest: ElearningModQuizGetAttemptSummaryRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_quiz_view_quiz
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModQuizGetQuizAccessInformationRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_quiz_view_quiz")
    fun modQuizViewQuiz(@Body elearningModQuizGetQuizAccessInformationRequest: ElearningModQuizGetQuizAccessInformationRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
