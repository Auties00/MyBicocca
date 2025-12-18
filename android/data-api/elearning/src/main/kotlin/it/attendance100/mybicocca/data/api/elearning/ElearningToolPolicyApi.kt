package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolPolicyGetPolicyVersion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolPolicyGetPolicyVersionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolPolicySubmitAcceptOnBehalfRequest

interface ToolPolicyApi {
    /**
     * POST tool_policy_get_policy_version
     * Fetch the details of a policy version
     * Fetch the details of a policy version
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolPolicyGetPolicyVersionRequest 
     * @return [Call]<[ElearningToolPolicyGetPolicyVersion200Response]>
     */
    @POST("tool_policy_get_policy_version")
    fun toolPolicyGetPolicyVersion(@Body elearningToolPolicyGetPolicyVersionRequest: ElearningToolPolicyGetPolicyVersionRequest): Call<ElearningToolPolicyGetPolicyVersion200Response>

    /**
     * POST tool_policy_submit_accept_on_behalf
     * Accept policies on behalf of other users
     * Accept policies on behalf of other users
     * Responses:
     *  - 200: success
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolPolicySubmitAcceptOnBehalfRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_policy_submit_accept_on_behalf")
    fun toolPolicySubmitAcceptOnBehalf(@Body elearningToolPolicySubmitAcceptOnBehalfRequest: ElearningToolPolicySubmitAcceptOnBehalfRequest): Call<kotlin.Any>

}
