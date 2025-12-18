package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCohortAddCohortMembers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportInsightsActionExecutedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportInsightsSetFixedPrediction200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportInsightsSetFixedPredictionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportInsightsSetNotusefulPrediction200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportInsightsSetNotusefulPredictionRequest

interface ReportInsightsApi {
    /**
     * POST report_insights_action_executed
     * Stores an action executed over a group of predictions.
     * Stores an action executed over a group of predictions.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningReportInsightsActionExecutedRequest 
     * @return [Call]<[ElearningCoreCohortAddCohortMembers200Response]>
     */
    @POST("report_insights_action_executed")
    fun reportInsightsActionExecuted(@Body elearningReportInsightsActionExecutedRequest: ElearningReportInsightsActionExecutedRequest): Call<ElearningCoreCohortAddCohortMembers200Response>

    /**
     * POST report_insights_set_fixed_prediction
     * Flags a prediction as fixed.
     * Flags a prediction as fixed.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningReportInsightsSetFixedPredictionRequest 
     * @return [Call]<[ElearningReportInsightsSetFixedPrediction200Response]>
     */
    @POST("report_insights_set_fixed_prediction")
    fun reportInsightsSetFixedPrediction(@Body elearningReportInsightsSetFixedPredictionRequest: ElearningReportInsightsSetFixedPredictionRequest): Call<ElearningReportInsightsSetFixedPrediction200Response>

    /**
     * POST report_insights_set_notuseful_prediction
     * Flags the prediction as not useful.
     * Flags the prediction as not useful.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningReportInsightsSetNotusefulPredictionRequest 
     * @return [Call]<[ElearningReportInsightsSetNotusefulPrediction200Response]>
     */
    @POST("report_insights_set_notuseful_prediction")
    fun reportInsightsSetNotusefulPrediction(@Body elearningReportInsightsSetNotusefulPredictionRequest: ElearningReportInsightsSetNotusefulPredictionRequest): Call<ElearningReportInsightsSetNotusefulPrediction200Response>

}
