package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreGradesGetGroupsForSearchWidgetRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGradereportSingleviewGetGradeItemsForSearchWidget200Response

interface GradereportSingleviewApi {
    /**
     * POST gradereport_singleview_get_grade_items_for_search_widget
     * Get the gradeitem/(s) for a course
     * Get the gradeitem/(s) for a course
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreGradesGetGroupsForSearchWidgetRequest 
     * @return [Call]<[ElearningGradereportSingleviewGetGradeItemsForSearchWidget200Response]>
     */
    @POST("gradereport_singleview_get_grade_items_for_search_widget")
    fun gradereportSingleviewGetGradeItemsForSearchWidget(@Body elearningCoreGradesGetGroupsForSearchWidgetRequest: ElearningCoreGradesGetGroupsForSearchWidgetRequest): Call<ElearningGradereportSingleviewGetGradeItemsForSearchWidget200Response>

}
