package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankViewquestiontextSetQuestionTextFormatRequest

interface QbankViewquestiontextApi {
    /**
     * POST qbank_viewquestiontext_set_question_text_format
     * Sets the preference for displaying and formatting the question text
     * Sets the preference for displaying and formatting the question text
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankViewquestiontextSetQuestionTextFormatRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("qbank_viewquestiontext_set_question_text_format")
    fun qbankViewquestiontextSetQuestionTextFormat(@Body elearningQbankViewquestiontextSetQuestionTextFormatRequest: ElearningQbankViewquestiontextSetQuestionTextFormatRequest): Call<kotlin.Any>

}
