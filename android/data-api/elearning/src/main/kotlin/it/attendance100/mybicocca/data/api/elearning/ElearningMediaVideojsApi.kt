package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningMediaVideojsGetLanguageRequest

interface MediaVideojsApi {
    /**
     * POST media_videojs_get_language
     * get language.
     * get language.
     * Responses:
     *  - 200: language pack json
     *  - 400: Invalid parameter value detected
     *
     * @param elearningMediaVideojsGetLanguageRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("media_videojs_get_language")
    fun mediaVideojsGetLanguage(@Body elearningMediaVideojsGetLanguageRequest: ElearningMediaVideojsGetLanguageRequest): Call<kotlin.Any>

}
