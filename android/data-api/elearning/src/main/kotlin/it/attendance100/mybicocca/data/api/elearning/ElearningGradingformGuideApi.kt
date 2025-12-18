package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreGradesGraderGradingpanelScaleFetchRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreGradesGraderGradingpanelScaleStoreRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradingformGuideGraderGradingpanelFetch200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradingformGuideGraderGradingpanelStore200Response

interface GradingformGuideApi {
    /**
     * POST gradingform_guide_grader_gradingpanel_fetch
     * Fetch the data required to display the grader grading panel, creating the grade item if required
     * Fetch the data required to display the grader grading panel, creating the grade item if required
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreGradesGraderGradingpanelScaleFetchRequest 
     * @return [Call]<[ElearningGradingformGuideGraderGradingpanelFetch200Response]>
     */
    @POST("gradingform_guide_grader_gradingpanel_fetch")
    fun gradingformGuideGraderGradingpanelFetch(@Body elearningCoreGradesGraderGradingpanelScaleFetchRequest: ElearningCoreGradesGraderGradingpanelScaleFetchRequest): Call<ElearningGradingformGuideGraderGradingpanelFetch200Response>

    /**
     * POST gradingform_guide_grader_gradingpanel_store
     * Store the grading data for a user from the grader grading panel.
     * Store the grading data for a user from the grader grading panel.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreGradesGraderGradingpanelScaleStoreRequest 
     * @return [Call]<[ElearningGradingformGuideGraderGradingpanelStore200Response]>
     */
    @POST("gradingform_guide_grader_gradingpanel_store")
    fun gradingformGuideGraderGradingpanelStore(@Body elearningCoreGradesGraderGradingpanelScaleStoreRequest: ElearningCoreGradesGraderGradingpanelScaleStoreRequest): Call<ElearningGradingformGuideGraderGradingpanelStore200Response>

}
