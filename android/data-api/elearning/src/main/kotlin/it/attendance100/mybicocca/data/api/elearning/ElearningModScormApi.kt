package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormAttemptCount200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormAttemptCountRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormScoTracks200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormScoTracksRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormScoes200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormScoesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormUserData200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormUserDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormGetScormsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormInsertScormTracks200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormInsertScormTracksRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormLaunchScoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModScormViewScormRequest

interface ModScormApi {
    /**
     * POST mod_scorm_get_scorm_access_information
     * Return capabilities information for a given scorm.
     * Return capabilities information for a given scorm.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormGetScormAccessInformationRequest 
     * @return [Call]<[ElearningModScormGetScormAccessInformation200Response]>
     */
    @POST("mod_scorm_get_scorm_access_information")
    fun modScormGetScormAccessInformation(@Body elearningModScormGetScormAccessInformationRequest: ElearningModScormGetScormAccessInformationRequest): Call<ElearningModScormGetScormAccessInformation200Response>

    /**
     * POST mod_scorm_get_scorm_attempt_count
     * Return the number of attempts done by a user in the given SCORM.
     * Return the number of attempts done by a user in the given SCORM.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormGetScormAttemptCountRequest 
     * @return [Call]<[ElearningModScormGetScormAttemptCount200Response]>
     */
    @POST("mod_scorm_get_scorm_attempt_count")
    fun modScormGetScormAttemptCount(@Body elearningModScormGetScormAttemptCountRequest: ElearningModScormGetScormAttemptCountRequest): Call<ElearningModScormGetScormAttemptCount200Response>

    /**
     * POST mod_scorm_get_scorm_sco_tracks
     * Retrieves SCO tracking data for the given user id and attempt number
     * Retrieves SCO tracking data for the given user id and attempt number
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormGetScormScoTracksRequest 
     * @return [Call]<[ElearningModScormGetScormScoTracks200Response]>
     */
    @POST("mod_scorm_get_scorm_sco_tracks")
    fun modScormGetScormScoTracks(@Body elearningModScormGetScormScoTracksRequest: ElearningModScormGetScormScoTracksRequest): Call<ElearningModScormGetScormScoTracks200Response>

    /**
     * POST mod_scorm_get_scorm_scoes
     * Returns a list containing all the scoes data related to the given scorm id
     * Returns a list containing all the scoes data related to the given scorm id
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormGetScormScoesRequest 
     * @return [Call]<[ElearningModScormGetScormScoes200Response]>
     */
    @POST("mod_scorm_get_scorm_scoes")
    fun modScormGetScormScoes(@Body elearningModScormGetScormScoesRequest: ElearningModScormGetScormScoesRequest): Call<ElearningModScormGetScormScoes200Response>

    /**
     * POST mod_scorm_get_scorm_user_data
     * Retrieves user tracking and SCO data and default SCORM values
     * Retrieves user tracking and SCO data and default SCORM values
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormGetScormUserDataRequest 
     * @return [Call]<[ElearningModScormGetScormUserData200Response]>
     */
    @POST("mod_scorm_get_scorm_user_data")
    fun modScormGetScormUserData(@Body elearningModScormGetScormUserDataRequest: ElearningModScormGetScormUserDataRequest): Call<ElearningModScormGetScormUserData200Response>

    /**
     * POST mod_scorm_get_scorms_by_courses
     * Returns a list of scorm instances in a provided set of courses, if                             no courses are provided then all the scorm instances the user has access to will be returned.
     * Returns a list of scorm instances in a provided set of courses, if                             no courses are provided then all the scorm instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModScormGetScormsByCourses200Response]>
     */
    @POST("mod_scorm_get_scorms_by_courses")
    fun modScormGetScormsByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModScormGetScormsByCourses200Response>

    /**
     * POST mod_scorm_insert_scorm_tracks
     * Saves a scorm tracking record.                           It will overwrite any existing tracking data for this attempt.                           Validation should be performed before running the function to ensure the user will not lose any existing                           attempt data.
     * Saves a scorm tracking record.                           It will overwrite any existing tracking data for this attempt.                           Validation should be performed before running the function to ensure the user will not lose any existing                           attempt data.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormInsertScormTracksRequest 
     * @return [Call]<[ElearningModScormInsertScormTracks200Response]>
     */
    @POST("mod_scorm_insert_scorm_tracks")
    fun modScormInsertScormTracks(@Body elearningModScormInsertScormTracksRequest: ElearningModScormInsertScormTracksRequest): Call<ElearningModScormInsertScormTracks200Response>

    /**
     * POST mod_scorm_launch_sco
     * Trigger the SCO launched event.
     * Trigger the SCO launched event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormLaunchScoRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_scorm_launch_sco")
    fun modScormLaunchSco(@Body elearningModScormLaunchScoRequest: ElearningModScormLaunchScoRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_scorm_view_scorm
     * Trigger the course module viewed event.
     * Trigger the course module viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModScormViewScormRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_scorm_view_scorm")
    fun modScormViewScorm(@Body elearningModScormViewScormRequest: ElearningModScormViewScormRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
