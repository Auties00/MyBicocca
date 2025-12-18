package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolXmldbInvokeMoveActionRequest

interface ToolXmldbApi {
    /**
     * POST tool_xmldb_invoke_move_action
     * moves element up/down
     * moves element up/down
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolXmldbInvokeMoveActionRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_xmldb_invoke_move_action")
    fun toolXmldbInvokeMoveAction(@Body elearningToolXmldbInvokeMoveActionRequest: ElearningToolXmldbInvokeMoveActionRequest): Call<kotlin.Any>

}
