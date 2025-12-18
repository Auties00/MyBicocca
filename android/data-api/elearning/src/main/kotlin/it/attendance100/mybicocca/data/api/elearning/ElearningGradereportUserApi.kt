package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportOverviewViewGradeReportRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportUserGetAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportUserGetAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportUserGetGradeItems200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportUserGetGradeItemsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportUserGetGradesTable200Response

interface GradereportUserApi {
    /**
     * POST gradereport_user_get_access_information
     * Returns user access information for the user grade report.
     * Returns user access information for the user grade report.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportUserGetAccessInformationRequest 
     * @return [Call]<[ElearningGradereportUserGetAccessInformation200Response]>
     */
    @POST("gradereport_user_get_access_information")
    fun gradereportUserGetAccessInformation(@Body elearningGradereportUserGetAccessInformationRequest: ElearningGradereportUserGetAccessInformationRequest): Call<ElearningGradereportUserGetAccessInformation200Response>

    /**
     * POST gradereport_user_get_grade_items
     * Returns the complete list of grade items for users in a course
     * Returns the complete list of grade items for users in a course
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportUserGetGradeItemsRequest 
     * @return [Call]<[ElearningGradereportUserGetGradeItems200Response]>
     */
    @POST("gradereport_user_get_grade_items")
    fun gradereportUserGetGradeItems(@Body elearningGradereportUserGetGradeItemsRequest: ElearningGradereportUserGetGradeItemsRequest): Call<ElearningGradereportUserGetGradeItems200Response>

    /**
     * POST gradereport_user_get_grades_table
     * Get the user/s report grades table for a course
     * Get the user/s report grades table for a course
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportUserGetGradeItemsRequest 
     * @return [Call]<[ElearningGradereportUserGetGradesTable200Response]>
     */
    @POST("gradereport_user_get_grades_table")
    fun gradereportUserGetGradesTable(@Body elearningGradereportUserGetGradeItemsRequest: ElearningGradereportUserGetGradeItemsRequest): Call<ElearningGradereportUserGetGradesTable200Response>

    /**
     * POST gradereport_user_view_grade_report
     * Trigger the report view event
     * Trigger the report view event
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningGradereportOverviewViewGradeReportRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("gradereport_user_view_grade_report")
    fun gradereportUserViewGradeReport(@Body elearningGradereportOverviewViewGradeReportRequest: ElearningGradereportOverviewViewGradeReportRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
