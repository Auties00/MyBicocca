package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningQuizaccessSebValidateQuizKeys200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningQuizaccessSebValidateQuizKeysRequest

interface QuizaccessSebApi {
    /**
     * POST quizaccess_seb_validate_quiz_keys
     * Validate a Safe Exam Browser config key or a browser exam key.
     * Validate a Safe Exam Browser config key or a browser exam key.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQuizaccessSebValidateQuizKeysRequest 
     * @return [Call]<[ElearningQuizaccessSebValidateQuizKeys200Response]>
     */
    @POST("quizaccess_seb_validate_quiz_keys")
    fun quizaccessSebValidateQuizKeys(@Body elearningQuizaccessSebValidateQuizKeysRequest: ElearningQuizaccessSebValidateQuizKeysRequest): Call<ElearningQuizaccessSebValidateQuizKeys200Response>

}
