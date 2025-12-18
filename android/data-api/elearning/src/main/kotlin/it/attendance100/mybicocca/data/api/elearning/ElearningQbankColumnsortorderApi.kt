package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankColumnsortorderSetColumnSizeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankColumnsortorderSetColumnbankOrderRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningQbankColumnsortorderSetHiddenColumnsRequest

interface QbankColumnsortorderApi {
    /**
     * POST qbank_columnsortorder_set_column_size
     * Column size
     * Column size
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankColumnsortorderSetColumnSizeRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("qbank_columnsortorder_set_column_size")
    fun qbankColumnsortorderSetColumnSize(@Body elearningQbankColumnsortorderSetColumnSizeRequest: ElearningQbankColumnsortorderSetColumnSizeRequest): Call<kotlin.Any>

    /**
     * POST qbank_columnsortorder_set_columnbank_order
     * Sets question columns order in database
     * Sets question columns order in database
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankColumnsortorderSetColumnbankOrderRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("qbank_columnsortorder_set_columnbank_order")
    fun qbankColumnsortorderSetColumnbankOrder(@Body elearningQbankColumnsortorderSetColumnbankOrderRequest: ElearningQbankColumnsortorderSetColumnbankOrderRequest): Call<kotlin.Any>

    /**
     * POST qbank_columnsortorder_set_hidden_columns
     * Hidden Columns
     * Hidden Columns
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningQbankColumnsortorderSetHiddenColumnsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("qbank_columnsortorder_set_hidden_columns")
    fun qbankColumnsortorderSetHiddenColumns(@Body elearningQbankColumnsortorderSetHiddenColumnsRequest: ElearningQbankColumnsortorderSetHiddenColumnsRequest): Call<kotlin.Any>

}
