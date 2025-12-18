package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolSelfEnrolUser200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolSelfEnrolUserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolSelfGetInstanceInfo200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolSelfGetInstanceInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface EnrolSelfApi {
    /**
     * POST enrol_self_enrol_user
     * Self enrol the current user in the given course.
     * Self enrol the current user in the given course.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolSelfEnrolUserRequest 
     * @return [Call]<[ElearningEnrolSelfEnrolUser200Response]>
     */
    @POST("enrol_self_enrol_user")
    fun enrolSelfEnrolUser(@Body elearningEnrolSelfEnrolUserRequest: ElearningEnrolSelfEnrolUserRequest): Call<ElearningEnrolSelfEnrolUser200Response>

    /**
     * POST enrol_self_get_instance_info
     * self enrolment instance information.
     * self enrolment instance information.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolSelfGetInstanceInfoRequest 
     * @return [Call]<[ElearningEnrolSelfGetInstanceInfo200Response]>
     */
    @POST("enrol_self_get_instance_info")
    fun enrolSelfGetInstanceInfo(@Body elearningEnrolSelfGetInstanceInfoRequest: ElearningEnrolSelfGetInstanceInfoRequest): Call<ElearningEnrolSelfGetInstanceInfo200Response>

}
