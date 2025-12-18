package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreQuestionSubmitTagsForm200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankTagquestionSubmitTagsFormRequest

interface QbankTagquestionApi {
    /**
     * POST qbank_tagquestion_submit_tags_form
     * Update the question tags.
     * Update the question tags.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankTagquestionSubmitTagsFormRequest 
     * @return [Call]<[ElearningCoreQuestionSubmitTagsForm200Response]>
     */
    @POST("qbank_tagquestion_submit_tags_form")
    fun qbankTagquestionSubmitTagsForm(@Body elearningQbankTagquestionSubmitTagsFormRequest: ElearningQbankTagquestionSubmitTagsFormRequest): Call<ElearningCoreQuestionSubmitTagsForm200Response>

}
