package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiCreateToolProxy200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiCreateToolProxyRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiCreateToolType200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiCreateToolTypeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiDeleteCourseToolTypeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiDeleteToolProxy200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiDeleteToolProxyRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiDeleteToolTypeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetLtisByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolLaunchData200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolLaunchDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolProxiesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolProxyRegistrationRequest200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolTypesAndProxies200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolTypesAndProxiesCount200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolTypesAndProxiesCountRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolTypesAndProxiesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiGetToolTypesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiIsCartridge200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiIsCartridgeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiToggleShowinactivitychooserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiUpdateToolType200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiUpdateToolTypeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLtiViewLtiRequest

interface ModLtiApi {
    /**
     * POST mod_lti_create_tool_proxy
     * Create a tool proxy
     * Create a tool proxy
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiCreateToolProxyRequest 
     * @return [Call]<[ElearningModLtiCreateToolProxy200Response]>
     */
    @POST("mod_lti_create_tool_proxy")
    fun modLtiCreateToolProxy(@Body elearningModLtiCreateToolProxyRequest: ElearningModLtiCreateToolProxyRequest): Call<ElearningModLtiCreateToolProxy200Response>

    /**
     * POST mod_lti_create_tool_type
     * Create a tool type
     * Create a tool type
     * Responses:
     *  - 200: Tool
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiCreateToolTypeRequest 
     * @return [Call]<[ElearningModLtiCreateToolType200Response]>
     */
    @POST("mod_lti_create_tool_type")
    fun modLtiCreateToolType(@Body elearningModLtiCreateToolTypeRequest: ElearningModLtiCreateToolTypeRequest): Call<ElearningModLtiCreateToolType200Response>

    /**
     * POST mod_lti_delete_course_tool_type
     * Delete a course tool type
     * Delete a course tool type
     * Responses:
     *  - 200: Success
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiDeleteCourseToolTypeRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_lti_delete_course_tool_type")
    fun modLtiDeleteCourseToolType(@Body elearningModLtiDeleteCourseToolTypeRequest: ElearningModLtiDeleteCourseToolTypeRequest): Call<kotlin.Any>

    /**
     * POST mod_lti_delete_tool_proxy
     * Delete a tool proxy
     * Delete a tool proxy
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiDeleteToolProxyRequest 
     * @return [Call]<[ElearningModLtiDeleteToolProxy200Response]>
     */
    @POST("mod_lti_delete_tool_proxy")
    fun modLtiDeleteToolProxy(@Body elearningModLtiDeleteToolProxyRequest: ElearningModLtiDeleteToolProxyRequest): Call<ElearningModLtiDeleteToolProxy200Response>

    /**
     * POST mod_lti_delete_tool_type
     * Delete a tool type
     * Delete a tool type
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiDeleteToolTypeRequest 
     * @return [Call]<[ElearningModLtiDeleteToolTypeRequest]>
     */
    @POST("mod_lti_delete_tool_type")
    fun modLtiDeleteToolType(@Body elearningModLtiDeleteToolTypeRequest: ElearningModLtiDeleteToolTypeRequest): Call<ElearningModLtiDeleteToolTypeRequest>

    /**
     * POST mod_lti_get_ltis_by_courses
     * Returns a list of external tool instances in a provided set of courses, if                             no courses are provided then all the external tool instances the user has access to will be returned.
     * Returns a list of external tool instances in a provided set of courses, if                             no courses are provided then all the external tool instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModLtiGetLtisByCourses200Response]>
     */
    @POST("mod_lti_get_ltis_by_courses")
    fun modLtiGetLtisByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModLtiGetLtisByCourses200Response>

    /**
     * POST mod_lti_get_tool_launch_data
     * Return the launch data for a given external tool.
     * Return the launch data for a given external tool.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiGetToolLaunchDataRequest 
     * @return [Call]<[ElearningModLtiGetToolLaunchData200Response]>
     */
    @POST("mod_lti_get_tool_launch_data")
    fun modLtiGetToolLaunchData(@Body elearningModLtiGetToolLaunchDataRequest: ElearningModLtiGetToolLaunchDataRequest): Call<ElearningModLtiGetToolLaunchData200Response>

    /**
     * POST mod_lti_get_tool_proxies
     * Get a list of the tool proxies
     * Get a list of the tool proxies
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiGetToolProxiesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_lti_get_tool_proxies")
    fun modLtiGetToolProxies(@Body elearningModLtiGetToolProxiesRequest: ElearningModLtiGetToolProxiesRequest): Call<kotlin.Any>

    /**
     * POST mod_lti_get_tool_proxy_registration_request
     * Get a registration request for a tool proxy
     * Get a registration request for a tool proxy
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiDeleteToolProxyRequest 
     * @return [Call]<[ElearningModLtiGetToolProxyRegistrationRequest200Response]>
     */
    @POST("mod_lti_get_tool_proxy_registration_request")
    fun modLtiGetToolProxyRegistrationRequest(@Body elearningModLtiDeleteToolProxyRequest: ElearningModLtiDeleteToolProxyRequest): Call<ElearningModLtiGetToolProxyRegistrationRequest200Response>

    /**
     * POST mod_lti_get_tool_types
     * Get a list of the tool types
     * Get a list of the tool types
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiGetToolTypesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_lti_get_tool_types")
    fun modLtiGetToolTypes(@Body elearningModLtiGetToolTypesRequest: ElearningModLtiGetToolTypesRequest): Call<kotlin.Any>

    /**
     * POST mod_lti_get_tool_types_and_proxies
     * Get a list of the tool types and tool proxies
     * Get a list of the tool types and tool proxies
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiGetToolTypesAndProxiesRequest 
     * @return [Call]<[ElearningModLtiGetToolTypesAndProxies200Response]>
     */
    @POST("mod_lti_get_tool_types_and_proxies")
    fun modLtiGetToolTypesAndProxies(@Body elearningModLtiGetToolTypesAndProxiesRequest: ElearningModLtiGetToolTypesAndProxiesRequest): Call<ElearningModLtiGetToolTypesAndProxies200Response>

    /**
     * POST mod_lti_get_tool_types_and_proxies_count
     * Get total number of the tool types and tool proxies
     * Get total number of the tool types and tool proxies
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiGetToolTypesAndProxiesCountRequest 
     * @return [Call]<[ElearningModLtiGetToolTypesAndProxiesCount200Response]>
     */
    @POST("mod_lti_get_tool_types_and_proxies_count")
    fun modLtiGetToolTypesAndProxiesCount(@Body elearningModLtiGetToolTypesAndProxiesCountRequest: ElearningModLtiGetToolTypesAndProxiesCountRequest): Call<ElearningModLtiGetToolTypesAndProxiesCount200Response>

    /**
     * POST mod_lti_is_cartridge
     * Determine if the given url is for a cartridge
     * Determine if the given url is for a cartridge
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiIsCartridgeRequest 
     * @return [Call]<[ElearningModLtiIsCartridge200Response]>
     */
    @POST("mod_lti_is_cartridge")
    fun modLtiIsCartridge(@Body elearningModLtiIsCartridgeRequest: ElearningModLtiIsCartridgeRequest): Call<ElearningModLtiIsCartridge200Response>

    /**
     * POST mod_lti_toggle_showinactivitychooser
     * Toggle showinactivitychooser for a tool type in a course
     * Toggle showinactivitychooser for a tool type in a course
     * Responses:
     *  - 200: Success
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiToggleShowinactivitychooserRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_lti_toggle_showinactivitychooser")
    fun modLtiToggleShowinactivitychooser(@Body elearningModLtiToggleShowinactivitychooserRequest: ElearningModLtiToggleShowinactivitychooserRequest): Call<kotlin.Any>

    /**
     * POST mod_lti_update_tool_type
     * Update a tool type
     * Update a tool type
     * Responses:
     *  - 200: Tool
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiUpdateToolTypeRequest 
     * @return [Call]<[ElearningModLtiUpdateToolType200Response]>
     */
    @POST("mod_lti_update_tool_type")
    fun modLtiUpdateToolType(@Body elearningModLtiUpdateToolTypeRequest: ElearningModLtiUpdateToolTypeRequest): Call<ElearningModLtiUpdateToolType200Response>

    /**
     * POST mod_lti_view_lti
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLtiViewLtiRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_lti_view_lti")
    fun modLtiViewLti(@Body elearningModLtiViewLtiRequest: ElearningModLtiViewLtiRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
