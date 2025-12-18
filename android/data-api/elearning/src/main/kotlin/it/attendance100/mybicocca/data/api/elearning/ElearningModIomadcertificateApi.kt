package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModIomadcertificateGetIomadcertificatesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModIomadcertificateGetIssuedIomadcertificates200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModIomadcertificateGetIssuedIomadcertificatesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModIomadcertificateIssueIomadcertificate200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModIomadcertificateIssueIomadcertificateRequest

interface ModIomadcertificateApi {
    /**
     * POST mod_iomadcertificate_get_iomadcertificates_by_courses
     * Returns a list of iomadcertificate instances in a provided set of courses, if                             no courses are provided then all the iomadcertificate instances the user has access to will be returned.
     * Returns a list of iomadcertificate instances in a provided set of courses, if                             no courses are provided then all the iomadcertificate instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModIomadcertificateGetIomadcertificatesByCourses200Response]>
     */
    @POST("mod_iomadcertificate_get_iomadcertificates_by_courses")
    fun modIomadcertificateGetIomadcertificatesByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModIomadcertificateGetIomadcertificatesByCourses200Response>

    /**
     * POST mod_iomadcertificate_get_issued_iomadcertificates
     * Get the list of issued iomadcertificates for the current user.
     * Get the list of issued iomadcertificates for the current user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModIomadcertificateGetIssuedIomadcertificatesRequest 
     * @return [Call]<[ElearningModIomadcertificateGetIssuedIomadcertificates200Response]>
     */
    @POST("mod_iomadcertificate_get_issued_iomadcertificates")
    fun modIomadcertificateGetIssuedIomadcertificates(@Body elearningModIomadcertificateGetIssuedIomadcertificatesRequest: ElearningModIomadcertificateGetIssuedIomadcertificatesRequest): Call<ElearningModIomadcertificateGetIssuedIomadcertificates200Response>

    /**
     * POST mod_iomadcertificate_issue_iomadcertificate
     * Create new iomadcertificate record, or return existing record for the current user.
     * Create new iomadcertificate record, or return existing record for the current user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModIomadcertificateIssueIomadcertificateRequest 
     * @return [Call]<[ElearningModIomadcertificateIssueIomadcertificate200Response]>
     */
    @POST("mod_iomadcertificate_issue_iomadcertificate")
    fun modIomadcertificateIssueIomadcertificate(@Body elearningModIomadcertificateIssueIomadcertificateRequest: ElearningModIomadcertificateIssueIomadcertificateRequest): Call<ElearningModIomadcertificateIssueIomadcertificate200Response>

    /**
     * POST mod_iomadcertificate_view_iomadcertificate
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModIomadcertificateIssueIomadcertificateRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_iomadcertificate_view_iomadcertificate")
    fun modIomadcertificateViewIomadcertificate(@Body elearningModIomadcertificateIssueIomadcertificateRequest: ElearningModIomadcertificateIssueIomadcertificateRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
