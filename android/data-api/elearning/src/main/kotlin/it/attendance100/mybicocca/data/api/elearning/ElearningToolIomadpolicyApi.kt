package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolIomadpolicyGetIomadpolicyVersion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolIomadpolicyGetIomadpolicyVersionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolIomadpolicySubmitAcceptOnBehalfRequest

interface ToolIomadpolicyApi {
    /**
     * POST tool_iomadpolicy_get_iomadpolicy_version
     * Fetch the details of a iomadpolicy version
     * Fetch the details of a iomadpolicy version
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolIomadpolicyGetIomadpolicyVersionRequest 
     * @return [Call]<[ElearningToolIomadpolicyGetIomadpolicyVersion200Response]>
     */
    @POST("tool_iomadpolicy_get_iomadpolicy_version")
    fun toolIomadpolicyGetIomadpolicyVersion(@Body elearningToolIomadpolicyGetIomadpolicyVersionRequest: ElearningToolIomadpolicyGetIomadpolicyVersionRequest): Call<ElearningToolIomadpolicyGetIomadpolicyVersion200Response>

    /**
     * POST tool_iomadpolicy_submit_accept_on_behalf
     * Accept policies on behalf of other users
     * Accept policies on behalf of other users
     * Responses:
     *  - 200: success
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolIomadpolicySubmitAcceptOnBehalfRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_iomadpolicy_submit_accept_on_behalf")
    fun toolIomadpolicySubmitAcceptOnBehalf(@Body elearningToolIomadpolicySubmitAcceptOnBehalfRequest: ElearningToolIomadpolicySubmitAcceptOnBehalfRequest): Call<kotlin.Any>

}
