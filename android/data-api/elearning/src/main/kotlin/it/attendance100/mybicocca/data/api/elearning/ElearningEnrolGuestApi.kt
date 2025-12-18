package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolGuestGetInstanceInfo200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolGuestGetInstanceInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolGuestValidatePassword200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolGuestValidatePasswordRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface EnrolGuestApi {
    /**
     * POST enrol_guest_get_instance_info
     * Return guest enrolment instance information.
     * Return guest enrolment instance information.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolGuestGetInstanceInfoRequest 
     * @return [Call]<[ElearningEnrolGuestGetInstanceInfo200Response]>
     */
    @POST("enrol_guest_get_instance_info")
    fun enrolGuestGetInstanceInfo(@Body elearningEnrolGuestGetInstanceInfoRequest: ElearningEnrolGuestGetInstanceInfoRequest): Call<ElearningEnrolGuestGetInstanceInfo200Response>

    /**
     * POST enrol_guest_validate_password
     * Perform password validation.
     * Perform password validation.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolGuestValidatePasswordRequest 
     * @return [Call]<[ElearningEnrolGuestValidatePassword200Response]>
     */
    @POST("enrol_guest_validate_password")
    fun enrolGuestValidatePassword(@Body elearningEnrolGuestValidatePasswordRequest: ElearningEnrolGuestValidatePasswordRequest): Call<ElearningEnrolGuestValidatePassword200Response>

}
