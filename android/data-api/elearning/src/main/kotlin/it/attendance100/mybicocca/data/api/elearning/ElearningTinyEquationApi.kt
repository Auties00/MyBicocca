package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyEquationFilter200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningTinyEquationFilterRequest

interface TinyEquationApi {
    /**
     * POST tiny_equation_filter
     * Filter the equation
     * Filter the equation
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningTinyEquationFilterRequest 
     * @return [Call]<[ElearningTinyEquationFilter200Response]>
     */
    @POST("tiny_equation_filter")
    fun tinyEquationFilter(@Body elearningTinyEquationFilterRequest: ElearningTinyEquationFilterRequest): Call<ElearningTinyEquationFilter200Response>

}
