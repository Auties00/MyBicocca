package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyAutosaveResetSessionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyAutosaveResumeSession200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyAutosaveResumeSessionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyAutosaveUpdateSessionRequest

interface TinyAutosaveApi {
    /**
     * POST tiny_autosave_reset_session
     * Reset an autosave session
     * Reset an autosave session
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningTinyAutosaveResetSessionRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tiny_autosave_reset_session")
    fun tinyAutosaveResetSession(@Body elearningTinyAutosaveResetSessionRequest: ElearningTinyAutosaveResetSessionRequest): Call<kotlin.Any>

    /**
     * POST tiny_autosave_resume_session
     * Resume an autosave session
     * Resume an autosave session
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningTinyAutosaveResumeSessionRequest 
     * @return [Call]<[ElearningTinyAutosaveResumeSession200Response]>
     */
    @POST("tiny_autosave_resume_session")
    fun tinyAutosaveResumeSession(@Body elearningTinyAutosaveResumeSessionRequest: ElearningTinyAutosaveResumeSessionRequest): Call<ElearningTinyAutosaveResumeSession200Response>

    /**
     * POST tiny_autosave_update_session
     * Update an autosave session
     * Update an autosave session
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningTinyAutosaveUpdateSessionRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tiny_autosave_update_session")
    fun tinyAutosaveUpdateSession(@Body elearningTinyAutosaveUpdateSessionRequest: ElearningTinyAutosaveUpdateSessionRequest): Call<kotlin.Any>

}
