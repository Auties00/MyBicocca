package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolManualEnrolUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolManualUnenrolUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface EnrolManualApi {
    /**
     * POST enrol_manual_enrol_users
     * Manual enrol users
     * Manual enrol users
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolManualEnrolUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("enrol_manual_enrol_users")
    fun enrolManualEnrolUsers(@Body elearningEnrolManualEnrolUsersRequest: ElearningEnrolManualEnrolUsersRequest): Call<kotlin.Any>

    /**
     * POST enrol_manual_unenrol_users
     * Manual unenrol users
     * Manual unenrol users
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolManualUnenrolUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("enrol_manual_unenrol_users")
    fun enrolManualUnenrolUsers(@Body elearningEnrolManualUnenrolUsersRequest: ElearningEnrolManualUnenrolUsersRequest): Call<kotlin.Any>

}
