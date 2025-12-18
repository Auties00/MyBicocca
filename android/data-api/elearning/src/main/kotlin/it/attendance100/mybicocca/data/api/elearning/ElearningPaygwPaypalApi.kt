package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningPaygwPaypalCreateTransactionComplete200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningPaygwPaypalCreateTransactionCompleteRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningPaygwPaypalGetConfigForJs200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningPaygwPaypalGetConfigForJsRequest

interface PaygwPaypalApi {
    /**
     * POST paygw_paypal_create_transaction_complete
     * Takes care of what needs to be done when a PayPal transaction comes back as complete.
     * Takes care of what needs to be done when a PayPal transaction comes back as complete.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningPaygwPaypalCreateTransactionCompleteRequest 
     * @return [Call]<[ElearningPaygwPaypalCreateTransactionComplete200Response]>
     */
    @POST("paygw_paypal_create_transaction_complete")
    fun paygwPaypalCreateTransactionComplete(@Body elearningPaygwPaypalCreateTransactionCompleteRequest: ElearningPaygwPaypalCreateTransactionCompleteRequest): Call<ElearningPaygwPaypalCreateTransactionComplete200Response>

    /**
     * POST paygw_paypal_get_config_for_js
     * Returns the configuration settings to be used in js
     * Returns the configuration settings to be used in js
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningPaygwPaypalGetConfigForJsRequest 
     * @return [Call]<[ElearningPaygwPaypalGetConfigForJs200Response]>
     */
    @POST("paygw_paypal_get_config_for_js")
    fun paygwPaypalGetConfigForJs(@Body elearningPaygwPaypalGetConfigForJsRequest: ElearningPaygwPaypalGetConfigForJsRequest): Call<ElearningPaygwPaypalGetConfigForJs200Response>

}
