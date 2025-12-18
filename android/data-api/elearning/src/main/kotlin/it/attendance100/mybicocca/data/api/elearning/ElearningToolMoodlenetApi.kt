package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMoodlenetSearchCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMoodlenetSearchCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMoodlenetVerifyWebfinger200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMoodlenetVerifyWebfingerRequest

interface ToolMoodlenetApi {
    /**
     * POST tool_moodlenet_search_courses
     * For some given input search for a course that matches
     * For some given input search for a course that matches
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMoodlenetSearchCoursesRequest 
     * @return [Call]<[ElearningToolMoodlenetSearchCourses200Response]>
     */
    @POST("tool_moodlenet_search_courses")
    fun toolMoodlenetSearchCourses(@Body elearningToolMoodlenetSearchCoursesRequest: ElearningToolMoodlenetSearchCoursesRequest): Call<ElearningToolMoodlenetSearchCourses200Response>

    /**
     * POST tool_moodlenet_verify_webfinger
     * Verify if the passed information resolves into a WebFinger profile URL
     * Verify if the passed information resolves into a WebFinger profile URL
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMoodlenetVerifyWebfingerRequest 
     * @return [Call]<[ElearningToolMoodlenetVerifyWebfinger200Response]>
     */
    @POST("tool_moodlenet_verify_webfinger")
    fun toolMoodlenetVerifyWebfinger(@Body elearningToolMoodlenetVerifyWebfingerRequest: ElearningToolMoodlenetVerifyWebfingerRequest): Call<ElearningToolMoodlenetVerifyWebfinger200Response>

}
