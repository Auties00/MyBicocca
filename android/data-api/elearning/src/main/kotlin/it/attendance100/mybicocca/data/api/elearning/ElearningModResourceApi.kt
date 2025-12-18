package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModResourceGetResourcesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModResourceViewResourceRequest

interface ModResourceApi {
    /**
     * POST mod_resource_get_resources_by_courses
     * Returns a list of files in a provided list of courses, if no list is provided all files that                             the user can view will be returned.
     * Returns a list of files in a provided list of courses, if no list is provided all files that                             the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModResourceGetResourcesByCourses200Response]>
     */
    @POST("mod_resource_get_resources_by_courses")
    fun modResourceGetResourcesByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModResourceGetResourcesByCourses200Response>

    /**
     * POST mod_resource_view_resource
     * Simulate the view.php web interface resource: trigger events, completion, etc...
     * Simulate the view.php web interface resource: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModResourceViewResourceRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_resource_view_resource")
    fun modResourceViewResource(@Body elearningModResourceViewResourceRequest: ElearningModResourceViewResourceRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
