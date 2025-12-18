package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCompletionMarkCourseSelfCompletedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreGradesGetGradableUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface GradereportGraderApi {
    /**
     * POST gradereport_grader_get_users_in_report
     * Returns the dataset of users within the report
     * Returns the dataset of users within the report
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreCompletionMarkCourseSelfCompletedRequest 
     * @return [Call]<[ElearningCoreGradesGetGradableUsers200Response]>
     */
    @POST("gradereport_grader_get_users_in_report")
    fun gradereportGraderGetUsersInReport(@Body elearningCoreCompletionMarkCourseSelfCompletedRequest: ElearningCoreCompletionMarkCourseSelfCompletedRequest): Call<ElearningCoreGradesGetGradableUsers200Response>

}
