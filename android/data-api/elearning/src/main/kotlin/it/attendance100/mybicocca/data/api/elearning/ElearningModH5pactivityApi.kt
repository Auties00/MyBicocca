package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetAttempts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetAttemptsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetH5pactivitiesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetH5pactivityAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetH5pactivityAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetResults200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetResultsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetUserAttempts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityGetUserAttemptsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityLogReportViewedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModH5pactivityViewH5pactivityRequest

interface ModH5pactivityApi {
    /**
     * POST mod_h5pactivity_get_attempts
     * Return the information needed to list a user attempts.
     * Return the information needed to list a user attempts.
     * Responses:
     *  - 200: Activity attempts data
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityGetAttemptsRequest 
     * @return [Call]<[ElearningModH5pactivityGetAttempts200Response]>
     */
    @POST("mod_h5pactivity_get_attempts")
    fun modH5pactivityGetAttempts(@Body elearningModH5pactivityGetAttemptsRequest: ElearningModH5pactivityGetAttemptsRequest): Call<ElearningModH5pactivityGetAttempts200Response>

    /**
     * POST mod_h5pactivity_get_h5pactivities_by_courses
     * Returns a list of h5p activities in a list of             provided courses, if no list is provided all h5p activities             that the user can view will be returned.
     * Returns a list of h5p activities in a list of             provided courses, if no list is provided all h5p activities             that the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModH5pactivityGetH5pactivitiesByCourses200Response]>
     */
    @POST("mod_h5pactivity_get_h5pactivities_by_courses")
    fun modH5pactivityGetH5pactivitiesByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModH5pactivityGetH5pactivitiesByCourses200Response>

    /**
     * POST mod_h5pactivity_get_h5pactivity_access_information
     * Return access information for a given h5p activity.
     * Return access information for a given h5p activity.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityGetH5pactivityAccessInformationRequest 
     * @return [Call]<[ElearningModH5pactivityGetH5pactivityAccessInformation200Response]>
     */
    @POST("mod_h5pactivity_get_h5pactivity_access_information")
    fun modH5pactivityGetH5pactivityAccessInformation(@Body elearningModH5pactivityGetH5pactivityAccessInformationRequest: ElearningModH5pactivityGetH5pactivityAccessInformationRequest): Call<ElearningModH5pactivityGetH5pactivityAccessInformation200Response>

    /**
     * POST mod_h5pactivity_get_results
     * Return the information needed to list a user attempt results.
     * Return the information needed to list a user attempt results.
     * Responses:
     *  - 200: Activity attempts results data
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityGetResultsRequest 
     * @return [Call]<[ElearningModH5pactivityGetResults200Response]>
     */
    @POST("mod_h5pactivity_get_results")
    fun modH5pactivityGetResults(@Body elearningModH5pactivityGetResultsRequest: ElearningModH5pactivityGetResultsRequest): Call<ElearningModH5pactivityGetResults200Response>

    /**
     * POST mod_h5pactivity_get_user_attempts
     * Return the information needed to list all enrolled user attempts.
     * Return the information needed to list all enrolled user attempts.
     * Responses:
     *  - 200: Activity attempts data
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityGetUserAttemptsRequest 
     * @return [Call]<[ElearningModH5pactivityGetUserAttempts200Response]>
     */
    @POST("mod_h5pactivity_get_user_attempts")
    fun modH5pactivityGetUserAttempts(@Body elearningModH5pactivityGetUserAttemptsRequest: ElearningModH5pactivityGetUserAttemptsRequest): Call<ElearningModH5pactivityGetUserAttempts200Response>

    /**
     * POST mod_h5pactivity_log_report_viewed
     * Log that the h5pactivity was viewed.
     * Log that the h5pactivity was viewed.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityLogReportViewedRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_h5pactivity_log_report_viewed")
    fun modH5pactivityLogReportViewed(@Body elearningModH5pactivityLogReportViewedRequest: ElearningModH5pactivityLogReportViewedRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_h5pactivity_view_h5pactivity
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModH5pactivityViewH5pactivityRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_h5pactivity_view_h5pactivity")
    fun modH5pactivityViewH5pactivity(@Body elearningModH5pactivityViewH5pactivityRequest: ElearningModH5pactivityViewH5pactivityRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
