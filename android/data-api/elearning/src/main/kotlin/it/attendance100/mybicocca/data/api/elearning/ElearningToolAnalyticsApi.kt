package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolAnalyticsPotentialContextsRequest

interface ToolAnalyticsApi {
    /**
     * POST tool_analytics_potential_contexts
     * Retrieve the list of potential contexts for a model.
     * Retrieve the list of potential contexts for a model.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolAnalyticsPotentialContextsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_analytics_potential_contexts")
    fun toolAnalyticsPotentialContexts(@Body elearningToolAnalyticsPotentialContextsRequest: ElearningToolAnalyticsPotentialContextsRequest): Call<kotlin.Any>

}
