package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolBehatGetEntityGenerator200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolBehatGetEntityGeneratorRequest

interface ToolBehatApi {
    /**
     * POST tool_behat_get_entity_generator
     * Get the generator details for an entity
     * Get the generator details for an entity
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolBehatGetEntityGeneratorRequest 
     * @return [Call]<[ElearningToolBehatGetEntityGenerator200Response]>
     */
    @POST("tool_behat_get_entity_generator")
    fun toolBehatGetEntityGenerator(@Body elearningToolBehatGetEntityGeneratorRequest: ElearningToolBehatGetEntityGeneratorRequest): Call<ElearningToolBehatGetEntityGenerator200Response>

}
