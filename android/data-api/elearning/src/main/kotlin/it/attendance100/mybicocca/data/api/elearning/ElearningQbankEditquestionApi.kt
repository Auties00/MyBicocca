package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankEditquestionSetStatus200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankEditquestionSetStatusRequest

interface QbankEditquestionApi {
    /**
     * POST qbank_editquestion_set_status
     * Update the question status.
     * Update the question status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankEditquestionSetStatusRequest 
     * @return [Call]<[ElearningQbankEditquestionSetStatus200Response]>
     */
    @POST("qbank_editquestion_set_status")
    fun qbankEditquestionSetStatus(@Body elearningQbankEditquestionSetStatusRequest: ElearningQbankEditquestionSetStatusRequest): Call<ElearningQbankEditquestionSetStatus200Response>

}
