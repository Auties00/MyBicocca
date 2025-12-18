package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModImscpGetImscpsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModImscpViewImscpRequest

interface ModImscpApi {
    /**
     * POST mod_imscp_get_imscps_by_courses
     * Returns a list of IMSCP instances in a provided set of courses,                             if no courses are provided then all the IMSCP instances the user has access to will be returned.
     * Returns a list of IMSCP instances in a provided set of courses,                             if no courses are provided then all the IMSCP instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModImscpGetImscpsByCourses200Response]>
     */
    @POST("mod_imscp_get_imscps_by_courses")
    fun modImscpGetImscpsByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModImscpGetImscpsByCourses200Response>

    /**
     * POST mod_imscp_view_imscp
     * Simulate the view.php web interface imscp: trigger events, completion, etc...
     * Simulate the view.php web interface imscp: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModImscpViewImscpRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_imscp_view_imscp")
    fun modImscpViewImscp(@Body elearningModImscpViewImscpRequest: ElearningModImscpViewImscpRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
