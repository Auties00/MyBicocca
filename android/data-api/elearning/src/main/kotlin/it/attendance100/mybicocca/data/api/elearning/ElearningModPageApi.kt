package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModPageGetPagesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModPageViewPageRequest

interface ModPageApi {
    /**
     * POST mod_page_get_pages_by_courses
     * Returns a list of pages in a provided list of courses, if no list is provided all pages that the user                             can view will be returned.
     * Returns a list of pages in a provided list of courses, if no list is provided all pages that the user                             can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModPageGetPagesByCourses200Response]>
     */
    @POST("mod_page_get_pages_by_courses")
    fun modPageGetPagesByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModPageGetPagesByCourses200Response>

    /**
     * POST mod_page_view_page
     * Simulate the view.php web interface page: trigger events, completion, etc...
     * Simulate the view.php web interface page: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModPageViewPageRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_page_view_page")
    fun modPageViewPage(@Body elearningModPageViewPageRequest: ElearningModPageViewPageRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
