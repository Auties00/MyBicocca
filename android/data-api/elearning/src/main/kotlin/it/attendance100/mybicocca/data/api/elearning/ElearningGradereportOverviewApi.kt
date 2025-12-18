package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportOverviewGetCourseGrades200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportOverviewGetCourseGradesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportOverviewViewGradeReportRequest

interface GradereportOverviewApi {
    /**
     * POST gradereport_overview_get_course_grades
     * Get the given user courses final grades
     * Get the given user courses final grades
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportOverviewGetCourseGradesRequest 
     * @return [Call]<[ElearningGradereportOverviewGetCourseGrades200Response]>
     */
    @POST("gradereport_overview_get_course_grades")
    fun gradereportOverviewGetCourseGrades(@Body elearningGradereportOverviewGetCourseGradesRequest: ElearningGradereportOverviewGetCourseGradesRequest): Call<ElearningGradereportOverviewGetCourseGrades200Response>

    /**
     * POST gradereport_overview_view_grade_report
     * Trigger the report view event
     * Trigger the report view event
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportOverviewViewGradeReportRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("gradereport_overview_view_grade_report")
    fun gradereportOverviewViewGradeReport(@Body elearningGradereportOverviewViewGradeReportRequest: ElearningGradereportOverviewViewGradeReportRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
