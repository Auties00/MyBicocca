package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursCompleteTourRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursFetchAndStartTour200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursFetchAndStartTourRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursResetTour200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursResetTourRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolUsertoursStepShownRequest

interface ToolUsertoursApi {
    /**
     * POST tool_usertours_complete_tour
     * Mark the specified tour as completed for the current user
     * Mark the specified tour as completed for the current user
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolUsertoursCompleteTourRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_usertours_complete_tour")
    fun toolUsertoursCompleteTour(@Body elearningToolUsertoursCompleteTourRequest: ElearningToolUsertoursCompleteTourRequest): Call<kotlin.Any>

    /**
     * POST tool_usertours_fetch_and_start_tour
     * Fetch the specified tour
     * Fetch the specified tour
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolUsertoursFetchAndStartTourRequest 
     * @return [Call]<[ElearningToolUsertoursFetchAndStartTour200Response]>
     */
    @POST("tool_usertours_fetch_and_start_tour")
    fun toolUsertoursFetchAndStartTour(@Body elearningToolUsertoursFetchAndStartTourRequest: ElearningToolUsertoursFetchAndStartTourRequest): Call<ElearningToolUsertoursFetchAndStartTour200Response>

    /**
     * POST tool_usertours_reset_tour
     * Remove the specified tour
     * Remove the specified tour
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolUsertoursResetTourRequest 
     * @return [Call]<[ElearningToolUsertoursResetTour200Response]>
     */
    @POST("tool_usertours_reset_tour")
    fun toolUsertoursResetTour(@Body elearningToolUsertoursResetTourRequest: ElearningToolUsertoursResetTourRequest): Call<ElearningToolUsertoursResetTour200Response>

    /**
     * POST tool_usertours_step_shown
     * Mark the specified step as completed for the current user
     * Mark the specified step as completed for the current user
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolUsertoursStepShownRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_usertours_step_shown")
    fun toolUsertoursStepShown(@Body elearningToolUsertoursStepShownRequest: ElearningToolUsertoursStepShownRequest): Call<kotlin.Any>

}
