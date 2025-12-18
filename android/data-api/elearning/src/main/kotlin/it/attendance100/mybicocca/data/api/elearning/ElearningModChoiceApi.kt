package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceDeleteChoiceResponses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceDeleteChoiceResponsesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceGetChoiceOptions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceGetChoiceOptionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceGetChoiceResults200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceGetChoicesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceSubmitChoiceResponse200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChoiceSubmitChoiceResponseRequest

interface ModChoiceApi {
    /**
     * POST mod_choice_delete_choice_responses
     * Delete the given submitted responses in a choice
     * Delete the given submitted responses in a choice
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChoiceDeleteChoiceResponsesRequest 
     * @return [Call]<[ElearningModChoiceDeleteChoiceResponses200Response]>
     */
    @POST("mod_choice_delete_choice_responses")
    fun modChoiceDeleteChoiceResponses(@Body elearningModChoiceDeleteChoiceResponsesRequest: ElearningModChoiceDeleteChoiceResponsesRequest): Call<ElearningModChoiceDeleteChoiceResponses200Response>

    /**
     * POST mod_choice_get_choice_options
     * Retrieve options for a specific choice.
     * Retrieve options for a specific choice.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChoiceGetChoiceOptionsRequest 
     * @return [Call]<[ElearningModChoiceGetChoiceOptions200Response]>
     */
    @POST("mod_choice_get_choice_options")
    fun modChoiceGetChoiceOptions(@Body elearningModChoiceGetChoiceOptionsRequest: ElearningModChoiceGetChoiceOptionsRequest): Call<ElearningModChoiceGetChoiceOptions200Response>

    /**
     * POST mod_choice_get_choice_results
     * Retrieve users results for a given choice.
     * Retrieve users results for a given choice.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChoiceGetChoiceOptionsRequest 
     * @return [Call]<[ElearningModChoiceGetChoiceResults200Response]>
     */
    @POST("mod_choice_get_choice_results")
    fun modChoiceGetChoiceResults(@Body elearningModChoiceGetChoiceOptionsRequest: ElearningModChoiceGetChoiceOptionsRequest): Call<ElearningModChoiceGetChoiceResults200Response>

    /**
     * POST mod_choice_get_choices_by_courses
     * Returns a list of choice instances in a provided set of courses,                             if no courses are provided then all the choice instances the user has access to will be returned.
     * Returns a list of choice instances in a provided set of courses,                             if no courses are provided then all the choice instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModChoiceGetChoicesByCourses200Response]>
     */
    @POST("mod_choice_get_choices_by_courses")
    fun modChoiceGetChoicesByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModChoiceGetChoicesByCourses200Response>

    /**
     * POST mod_choice_submit_choice_response
     * Submit responses to a specific choice item.
     * Submit responses to a specific choice item.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChoiceSubmitChoiceResponseRequest 
     * @return [Call]<[ElearningModChoiceSubmitChoiceResponse200Response]>
     */
    @POST("mod_choice_submit_choice_response")
    fun modChoiceSubmitChoiceResponse(@Body elearningModChoiceSubmitChoiceResponseRequest: ElearningModChoiceSubmitChoiceResponseRequest): Call<ElearningModChoiceSubmitChoiceResponse200Response>

    /**
     * POST mod_choice_view_choice
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChoiceGetChoiceOptionsRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_choice_view_choice")
    fun modChoiceViewChoice(@Body elearningModChoiceGetChoiceOptionsRequest: ElearningModChoiceGetChoiceOptionsRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
