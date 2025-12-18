package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolLicenseEnrolUser200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolLicenseEnrolUserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolLicenseGetInstanceInfo200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolLicenseGetInstanceInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface EnrolLicenseApi {
    /**
     * POST enrol_license_enrol_user
     * License enrol the current user in the given course.
     * License enrol the current user in the given course.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolLicenseEnrolUserRequest 
     * @return [Call]<[ElearningEnrolLicenseEnrolUser200Response]>
     */
    @POST("enrol_license_enrol_user")
    fun enrolLicenseEnrolUser(@Body elearningEnrolLicenseEnrolUserRequest: ElearningEnrolLicenseEnrolUserRequest): Call<ElearningEnrolLicenseEnrolUser200Response>

    /**
     * POST enrol_license_get_instance_info
     * License enrolment instance information.
     * License enrolment instance information.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolLicenseGetInstanceInfoRequest 
     * @return [Call]<[ElearningEnrolLicenseGetInstanceInfo200Response]>
     */
    @POST("enrol_license_get_instance_info")
    fun enrolLicenseGetInstanceInfo(@Body elearningEnrolLicenseGetInstanceInfoRequest: ElearningEnrolLicenseGetInstanceInfoRequest): Call<ElearningEnrolLicenseGetInstanceInfo200Response>

}
