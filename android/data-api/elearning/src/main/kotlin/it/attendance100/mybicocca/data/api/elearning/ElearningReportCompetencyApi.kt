package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportCompetencyDataForReport200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningReportCompetencyDataForReportRequest

interface ReportCompetencyApi {
    /**
     * POST report_competency_data_for_report
     * Load the data for the competency report in a course.
     * Load the data for the competency report in a course.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningReportCompetencyDataForReportRequest 
     * @return [Call]<[ElearningReportCompetencyDataForReport200Response]>
     */
    @POST("report_competency_data_for_report")
    fun reportCompetencyDataForReport(@Body elearningReportCompetencyDataForReportRequest: ElearningReportCompetencyDataForReportRequest): Call<ElearningReportCompetencyDataForReport200Response>

}
