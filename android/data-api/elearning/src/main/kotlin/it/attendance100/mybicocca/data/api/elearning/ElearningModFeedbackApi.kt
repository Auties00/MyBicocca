package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetAnalysis200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetAnalysisRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetCurrentCompletedTmp200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetCurrentCompletedTmpRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetFeedbackAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetFeedbackAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetFeedbacksByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetFinishedResponses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetFinishedResponsesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetItems200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetLastCompleted200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetNonRespondents200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetNonRespondentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetPageItems200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetPageItemsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetResponsesAnalysis200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetResponsesAnalysisRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackGetUnfinishedResponses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackLaunchFeedback200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackProcessPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackProcessPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFeedbackViewFeedbackRequest

interface ModFeedbackApi {
    /**
     * POST mod_feedback_get_analysis
     * Retrieves the feedback analysis.
     * Retrieves the feedback analysis.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetAnalysisRequest 
     * @return [Call]<[ElearningModFeedbackGetAnalysis200Response]>
     */
    @POST("mod_feedback_get_analysis")
    fun modFeedbackGetAnalysis(@Body elearningModFeedbackGetAnalysisRequest: ElearningModFeedbackGetAnalysisRequest): Call<ElearningModFeedbackGetAnalysis200Response>

    /**
     * POST mod_feedback_get_current_completed_tmp
     * Returns the temporary completion record for the current user.
     * Returns the temporary completion record for the current user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetCurrentCompletedTmpRequest 
     * @return [Call]<[ElearningModFeedbackGetCurrentCompletedTmp200Response]>
     */
    @POST("mod_feedback_get_current_completed_tmp")
    fun modFeedbackGetCurrentCompletedTmp(@Body elearningModFeedbackGetCurrentCompletedTmpRequest: ElearningModFeedbackGetCurrentCompletedTmpRequest): Call<ElearningModFeedbackGetCurrentCompletedTmp200Response>

    /**
     * POST mod_feedback_get_feedback_access_information
     * Return access information for a given feedback.
     * Return access information for a given feedback.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetFeedbackAccessInformationRequest 
     * @return [Call]<[ElearningModFeedbackGetFeedbackAccessInformation200Response]>
     */
    @POST("mod_feedback_get_feedback_access_information")
    fun modFeedbackGetFeedbackAccessInformation(@Body elearningModFeedbackGetFeedbackAccessInformationRequest: ElearningModFeedbackGetFeedbackAccessInformationRequest): Call<ElearningModFeedbackGetFeedbackAccessInformation200Response>

    /**
     * POST mod_feedback_get_feedbacks_by_courses
     * Returns a list of feedbacks in a provided list of courses, if no list is provided all feedbacks that                             the user can view will be returned.
     * Returns a list of feedbacks in a provided list of courses, if no list is provided all feedbacks that                             the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModFeedbackGetFeedbacksByCourses200Response]>
     */
    @POST("mod_feedback_get_feedbacks_by_courses")
    fun modFeedbackGetFeedbacksByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModFeedbackGetFeedbacksByCourses200Response>

    /**
     * POST mod_feedback_get_finished_responses
     * Retrieves responses from the last finished attempt.
     * Retrieves responses from the last finished attempt.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetFinishedResponsesRequest 
     * @return [Call]<[ElearningModFeedbackGetFinishedResponses200Response]>
     */
    @POST("mod_feedback_get_finished_responses")
    fun modFeedbackGetFinishedResponses(@Body elearningModFeedbackGetFinishedResponsesRequest: ElearningModFeedbackGetFinishedResponsesRequest): Call<ElearningModFeedbackGetFinishedResponses200Response>

    /**
     * POST mod_feedback_get_items
     * Returns the items (questions) in the given feedback.
     * Returns the items (questions) in the given feedback.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetCurrentCompletedTmpRequest 
     * @return [Call]<[ElearningModFeedbackGetItems200Response]>
     */
    @POST("mod_feedback_get_items")
    fun modFeedbackGetItems(@Body elearningModFeedbackGetCurrentCompletedTmpRequest: ElearningModFeedbackGetCurrentCompletedTmpRequest): Call<ElearningModFeedbackGetItems200Response>

    /**
     * POST mod_feedback_get_last_completed
     * Retrieves the last completion record for the current user.
     * Retrieves the last completion record for the current user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetCurrentCompletedTmpRequest 
     * @return [Call]<[ElearningModFeedbackGetLastCompleted200Response]>
     */
    @POST("mod_feedback_get_last_completed")
    fun modFeedbackGetLastCompleted(@Body elearningModFeedbackGetCurrentCompletedTmpRequest: ElearningModFeedbackGetCurrentCompletedTmpRequest): Call<ElearningModFeedbackGetLastCompleted200Response>

    /**
     * POST mod_feedback_get_non_respondents
     * Retrieves a list of students who didn&#39;t submit the feedback.
     * Retrieves a list of students who didn&#39;t submit the feedback.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetNonRespondentsRequest 
     * @return [Call]<[ElearningModFeedbackGetNonRespondents200Response]>
     */
    @POST("mod_feedback_get_non_respondents")
    fun modFeedbackGetNonRespondents(@Body elearningModFeedbackGetNonRespondentsRequest: ElearningModFeedbackGetNonRespondentsRequest): Call<ElearningModFeedbackGetNonRespondents200Response>

    /**
     * POST mod_feedback_get_page_items
     * Get a single feedback page items.
     * Get a single feedback page items.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetPageItemsRequest 
     * @return [Call]<[ElearningModFeedbackGetPageItems200Response]>
     */
    @POST("mod_feedback_get_page_items")
    fun modFeedbackGetPageItems(@Body elearningModFeedbackGetPageItemsRequest: ElearningModFeedbackGetPageItemsRequest): Call<ElearningModFeedbackGetPageItems200Response>

    /**
     * POST mod_feedback_get_responses_analysis
     * Return the feedback user responses analysis.
     * Return the feedback user responses analysis.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetResponsesAnalysisRequest 
     * @return [Call]<[ElearningModFeedbackGetResponsesAnalysis200Response]>
     */
    @POST("mod_feedback_get_responses_analysis")
    fun modFeedbackGetResponsesAnalysis(@Body elearningModFeedbackGetResponsesAnalysisRequest: ElearningModFeedbackGetResponsesAnalysisRequest): Call<ElearningModFeedbackGetResponsesAnalysis200Response>

    /**
     * POST mod_feedback_get_unfinished_responses
     * Retrieves responses from the current unfinished attempt.
     * Retrieves responses from the current unfinished attempt.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetFinishedResponsesRequest 
     * @return [Call]<[ElearningModFeedbackGetUnfinishedResponses200Response]>
     */
    @POST("mod_feedback_get_unfinished_responses")
    fun modFeedbackGetUnfinishedResponses(@Body elearningModFeedbackGetFinishedResponsesRequest: ElearningModFeedbackGetFinishedResponsesRequest): Call<ElearningModFeedbackGetUnfinishedResponses200Response>

    /**
     * POST mod_feedback_launch_feedback
     * Starts or continues a feedback submission.
     * Starts or continues a feedback submission.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackGetCurrentCompletedTmpRequest 
     * @return [Call]<[ElearningModFeedbackLaunchFeedback200Response]>
     */
    @POST("mod_feedback_launch_feedback")
    fun modFeedbackLaunchFeedback(@Body elearningModFeedbackGetCurrentCompletedTmpRequest: ElearningModFeedbackGetCurrentCompletedTmpRequest): Call<ElearningModFeedbackLaunchFeedback200Response>

    /**
     * POST mod_feedback_process_page
     * Process a jump between pages.
     * Process a jump between pages.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackProcessPageRequest 
     * @return [Call]<[ElearningModFeedbackProcessPage200Response]>
     */
    @POST("mod_feedback_process_page")
    fun modFeedbackProcessPage(@Body elearningModFeedbackProcessPageRequest: ElearningModFeedbackProcessPageRequest): Call<ElearningModFeedbackProcessPage200Response>

    /**
     * POST mod_feedback_view_feedback
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFeedbackViewFeedbackRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_feedback_view_feedback")
    fun modFeedbackViewFeedback(@Body elearningModFeedbackViewFeedbackRequest: ElearningModFeedbackViewFeedbackRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
