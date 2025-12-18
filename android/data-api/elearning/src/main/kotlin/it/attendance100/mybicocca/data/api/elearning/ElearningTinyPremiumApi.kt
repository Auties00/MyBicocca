package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyPremiumGetApiKey200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyPremiumGetApiKeyRequest

interface TinyPremiumApi {
    /**
     * POST tiny_premium_get_api_key
     * Get the Tiny Premium API key from Moodle
     * Get the Tiny Premium API key from Moodle
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningTinyPremiumGetApiKeyRequest 
     * @return [Call]<[ElearningTinyPremiumGetApiKey200Response]>
     */
    @POST("tiny_premium_get_api_key")
    fun tinyPremiumGetApiKey(@Body elearningTinyPremiumGetApiKeyRequest: ElearningTinyPremiumGetApiKeyRequest): Call<ElearningTinyPremiumGetApiKey200Response>

}
