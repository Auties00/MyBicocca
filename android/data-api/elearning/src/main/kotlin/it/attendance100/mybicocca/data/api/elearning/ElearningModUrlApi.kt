package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModUrlGetUrlsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModUrlViewUrlRequest

interface ModUrlApi {
    /**
     * POST mod_url_get_urls_by_courses
     * Returns a list of urls in a provided list of courses, if no list is provided all urls that the user                             can view will be returned.
     * Returns a list of urls in a provided list of courses, if no list is provided all urls that the user                             can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModUrlGetUrlsByCourses200Response]>
     */
    @POST("mod_url_get_urls_by_courses")
    fun modUrlGetUrlsByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModUrlGetUrlsByCourses200Response>

    /**
     * POST mod_url_view_url
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModUrlViewUrlRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_url_view_url")
    fun modUrlViewUrl(@Body elearningModUrlViewUrlRequest: ElearningModUrlViewUrlRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
