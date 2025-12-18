package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolMetaAddInstancesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningEnrolMetaDeleteInstancesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface EnrolMetaApi {
    /**
     * POST enrol_meta_add_instances
     * Add meta enrolment instances
     * Add meta enrolment instances
     * Responses:
     *  - 200: List of course meta enrolment instances that were created.
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolMetaAddInstancesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("enrol_meta_add_instances")
    fun enrolMetaAddInstances(@Body elearningEnrolMetaAddInstancesRequest: ElearningEnrolMetaAddInstancesRequest): Call<kotlin.Any>

    /**
     * POST enrol_meta_delete_instances
     * Delete meta enrolment instances
     * Delete meta enrolment instances
     * Responses:
     *  - 200: List of course meta enrolment instances that were deleted.
     *  - 400: Invalid parameter value detected
     *
     * @param elearningEnrolMetaDeleteInstancesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("enrol_meta_delete_instances")
    fun enrolMetaDeleteInstances(@Body elearningEnrolMetaDeleteInstancesRequest: ElearningEnrolMetaDeleteInstancesRequest): Call<kotlin.Any>

}
