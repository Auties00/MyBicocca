package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLabelGetLabelsByCourses200Response

interface ModLabelApi {
    /**
     * POST mod_label_get_labels_by_courses
     * Returns a list of labels in a provided list of courses, if no list is provided all labels that the user                             can view will be returned.
     * Returns a list of labels in a provided list of courses, if no list is provided all labels that the user                             can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModLabelGetLabelsByCourses200Response]>
     */
    @POST("mod_label_get_labels_by_courses")
    fun modLabelGetLabelsByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModLabelGetLabelsByCourses200Response>

}
