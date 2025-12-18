package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCohortAddCohortMembers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnCanJoin200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnCanJoinRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnCompletionValidateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnEndMeetingRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetJoinUrl200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetRecordings200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetRecordingsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetRecordingsToImport200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetRecordingsToImportRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnMeetingInfo200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnMeetingInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnUpdateRecordingRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnViewBigbluebuttonbnRequest

interface ModBigbluebuttonbnApi {
    /**
     * POST mod_bigbluebuttonbn_can_join
     * Returns information if the current user can join or not.
     * Returns information if the current user can join or not.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnCanJoinRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnCanJoin200Response]>
     */
    @POST("mod_bigbluebuttonbn_can_join")
    fun modBigbluebuttonbnCanJoin(@Body elearningModBigbluebuttonbnCanJoinRequest: ElearningModBigbluebuttonbnCanJoinRequest): Call<ElearningModBigbluebuttonbnCanJoin200Response>

    /**
     * POST mod_bigbluebuttonbn_completion_validate
     * Validate completion
     * Validate completion
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnCompletionValidateRequest 
     * @return [Call]<[ElearningCoreCohortAddCohortMembers200Response]>
     */
    @POST("mod_bigbluebuttonbn_completion_validate")
    fun modBigbluebuttonbnCompletionValidate(@Body elearningModBigbluebuttonbnCompletionValidateRequest: ElearningModBigbluebuttonbnCompletionValidateRequest): Call<ElearningCoreCohortAddCohortMembers200Response>

    /**
     * POST mod_bigbluebuttonbn_end_meeting
     * End a meeting
     * End a meeting
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnEndMeetingRequest 
     * @return [Call]<[ElearningCoreCohortAddCohortMembers200Response]>
     */
    @POST("mod_bigbluebuttonbn_end_meeting")
    fun modBigbluebuttonbnEndMeeting(@Body elearningModBigbluebuttonbnEndMeetingRequest: ElearningModBigbluebuttonbnEndMeetingRequest): Call<ElearningCoreCohortAddCohortMembers200Response>

    /**
     * POST mod_bigbluebuttonbn_get_bigbluebuttonbns_by_courses
     * Returns a list of bigbluebuttonbns in a provided list of courses, if no list is provided                             all bigbluebuttonbns that the user can view will be returned.
     * Returns a list of bigbluebuttonbns in a provided list of courses, if no list is provided                             all bigbluebuttonbns that the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCourses200Response]>
     */
    @POST("mod_bigbluebuttonbn_get_bigbluebuttonbns_by_courses")
    fun modBigbluebuttonbnGetBigbluebuttonbnsByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCourses200Response>

    /**
     * POST mod_bigbluebuttonbn_get_join_url
     * Get the join URL for the meeting and create if it does not exist.
     * Get the join URL for the meeting and create if it does not exist.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnCanJoinRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnGetJoinUrl200Response]>
     */
    @POST("mod_bigbluebuttonbn_get_join_url")
    fun modBigbluebuttonbnGetJoinUrl(@Body elearningModBigbluebuttonbnCanJoinRequest: ElearningModBigbluebuttonbnCanJoinRequest): Call<ElearningModBigbluebuttonbnGetJoinUrl200Response>

    /**
     * POST mod_bigbluebuttonbn_get_recordings
     * Returns a list of recordings ready to be processed by a datatable.
     * Returns a list of recordings ready to be processed by a datatable.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetRecordingsRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnGetRecordings200Response]>
     */
    @POST("mod_bigbluebuttonbn_get_recordings")
    fun modBigbluebuttonbnGetRecordings(@Body elearningModBigbluebuttonbnGetRecordingsRequest: ElearningModBigbluebuttonbnGetRecordingsRequest): Call<ElearningModBigbluebuttonbnGetRecordings200Response>

    /**
     * POST mod_bigbluebuttonbn_get_recordings_to_import
     * Returns a list of recordings ready to import to be processed by a datatable.
     * Returns a list of recordings ready to import to be processed by a datatable.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetRecordingsToImportRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnGetRecordingsToImport200Response]>
     */
    @POST("mod_bigbluebuttonbn_get_recordings_to_import")
    fun modBigbluebuttonbnGetRecordingsToImport(@Body elearningModBigbluebuttonbnGetRecordingsToImportRequest: ElearningModBigbluebuttonbnGetRecordingsToImportRequest): Call<ElearningModBigbluebuttonbnGetRecordingsToImport200Response>

    /**
     * POST mod_bigbluebuttonbn_meeting_info
     * Get displayable information on the meeting
     * Get displayable information on the meeting
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnMeetingInfoRequest 
     * @return [Call]<[ElearningModBigbluebuttonbnMeetingInfo200Response]>
     */
    @POST("mod_bigbluebuttonbn_meeting_info")
    fun modBigbluebuttonbnMeetingInfo(@Body elearningModBigbluebuttonbnMeetingInfoRequest: ElearningModBigbluebuttonbnMeetingInfoRequest): Call<ElearningModBigbluebuttonbnMeetingInfo200Response>

    /**
     * POST mod_bigbluebuttonbn_update_recording
     * Update a single recording
     * Update a single recording
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnUpdateRecordingRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_bigbluebuttonbn_update_recording")
    fun modBigbluebuttonbnUpdateRecording(@Body elearningModBigbluebuttonbnUpdateRecordingRequest: ElearningModBigbluebuttonbnUpdateRecordingRequest): Call<kotlin.Any>

    /**
     * POST mod_bigbluebuttonbn_view_bigbluebuttonbn
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnViewBigbluebuttonbnRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_bigbluebuttonbn_view_bigbluebuttonbn")
    fun modBigbluebuttonbnViewBigbluebuttonbn(@Body elearningModBigbluebuttonbnViewBigbluebuttonbnRequest: ElearningModBigbluebuttonbnViewBigbluebuttonbnRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
