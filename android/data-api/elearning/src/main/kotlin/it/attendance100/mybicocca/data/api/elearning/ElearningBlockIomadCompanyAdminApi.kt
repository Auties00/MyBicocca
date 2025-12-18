package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminAllocateLicensesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminAssignCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminAssignUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminAssignUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminCapabilityDeleteTemplateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminCheckToken200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminCheckTokenRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminCreateCompaniesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminCreateLicensesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminDeleteLicensesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminEditCompaniesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminEditLicensesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminEnrolUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetCompanies200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetCompaniesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetCompanyCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetCompanyCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetCourseInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetDepartmentUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetDepartmentUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetDepartments200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetDepartmentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetLicenseFromId200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetLicenseFromIdRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetLicenseInfo200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminGetLicenseInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminMoveUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminRestrictCapabilityRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminSyncUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminSyncUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminUnallocateLicensesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminUnassignCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminUnassignUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockIomadCompanyAdminUpdateCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface BlockIomadCompanyAdminApi {
    /**
     * POST block_iomad_company_admin_allocate_licenses
     * Allocate course licenses to a user
     * Allocate course licenses to a user
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminAllocateLicensesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_allocate_licenses")
    fun blockIomadCompanyAdminAllocateLicenses(@Body elearningBlockIomadCompanyAdminAllocateLicensesRequest: ElearningBlockIomadCompanyAdminAllocateLicensesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_assign_courses
     * Assign a course to a company
     * Assign a course to a company
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminAssignCoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_assign_courses")
    fun blockIomadCompanyAdminAssignCourses(@Body elearningBlockIomadCompanyAdminAssignCoursesRequest: ElearningBlockIomadCompanyAdminAssignCoursesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_assign_users
     * Assign users to a company
     * Assign users to a company
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminAssignUsersRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminAssignUsers200Response]>
     */
    @POST("block_iomad_company_admin_assign_users")
    fun blockIomadCompanyAdminAssignUsers(@Body elearningBlockIomadCompanyAdminAssignUsersRequest: ElearningBlockIomadCompanyAdminAssignUsersRequest): Call<ElearningBlockIomadCompanyAdminAssignUsers200Response>

    /**
     * POST block_iomad_company_admin_capability_delete_template
     * Delete Iomad capabilities template
     * Delete Iomad capabilities template
     * Responses:
     *  - 200: True capability update succeeds
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminCapabilityDeleteTemplateRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_capability_delete_template")
    fun blockIomadCompanyAdminCapabilityDeleteTemplate(@Body elearningBlockIomadCompanyAdminCapabilityDeleteTemplateRequest: ElearningBlockIomadCompanyAdminCapabilityDeleteTemplateRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_check_token
     * Check SSO token
     * Check SSO token
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminCheckTokenRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminCheckToken200Response]>
     */
    @POST("block_iomad_company_admin_check_token")
    fun blockIomadCompanyAdminCheckToken(@Body elearningBlockIomadCompanyAdminCheckTokenRequest: ElearningBlockIomadCompanyAdminCheckTokenRequest): Call<ElearningBlockIomadCompanyAdminCheckToken200Response>

    /**
     * POST block_iomad_company_admin_create_companies
     * Create new Iomad companies
     * Create new Iomad companies
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminCreateCompaniesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_create_companies")
    fun blockIomadCompanyAdminCreateCompanies(@Body elearningBlockIomadCompanyAdminCreateCompaniesRequest: ElearningBlockIomadCompanyAdminCreateCompaniesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_create_licenses
     * Create company licenses
     * Create company licenses
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminCreateLicensesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_create_licenses")
    fun blockIomadCompanyAdminCreateLicenses(@Body elearningBlockIomadCompanyAdminCreateLicensesRequest: ElearningBlockIomadCompanyAdminCreateLicensesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_delete_licenses
     * Delete company licenses
     * Delete company licenses
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminDeleteLicensesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_delete_licenses")
    fun blockIomadCompanyAdminDeleteLicenses(@Body elearningBlockIomadCompanyAdminDeleteLicensesRequest: ElearningBlockIomadCompanyAdminDeleteLicensesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_edit_companies
     * Edit Iomad companies
     * Edit Iomad companies
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminEditCompaniesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_edit_companies")
    fun blockIomadCompanyAdminEditCompanies(@Body elearningBlockIomadCompanyAdminEditCompaniesRequest: ElearningBlockIomadCompanyAdminEditCompaniesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_edit_licenses
     * Edit company license settings
     * Edit company license settings
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminEditLicensesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_edit_licenses")
    fun blockIomadCompanyAdminEditLicenses(@Body elearningBlockIomadCompanyAdminEditLicensesRequest: ElearningBlockIomadCompanyAdminEditLicensesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_enrol_users
     * Assign users onto courses
     * Assign users onto courses
     * Responses:
     *  - 200: True user enrolments succeeds
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminEnrolUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_enrol_users")
    fun blockIomadCompanyAdminEnrolUsers(@Body elearningBlockIomadCompanyAdminEnrolUsersRequest: ElearningBlockIomadCompanyAdminEnrolUsersRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_get_companies
     * Get all Iomad companies
     * Get all Iomad companies
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetCompaniesRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetCompanies200Response]>
     */
    @POST("block_iomad_company_admin_get_companies")
    fun blockIomadCompanyAdminGetCompanies(@Body elearningBlockIomadCompanyAdminGetCompaniesRequest: ElearningBlockIomadCompanyAdminGetCompaniesRequest): Call<ElearningBlockIomadCompanyAdminGetCompanies200Response>

    /**
     * POST block_iomad_company_admin_get_company_courses
     * Get Iomad company course allocations
     * Get Iomad company course allocations
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetCompanyCoursesRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetCompanyCourses200Response]>
     */
    @POST("block_iomad_company_admin_get_company_courses")
    fun blockIomadCompanyAdminGetCompanyCourses(@Body elearningBlockIomadCompanyAdminGetCompanyCoursesRequest: ElearningBlockIomadCompanyAdminGetCompanyCoursesRequest): Call<ElearningBlockIomadCompanyAdminGetCompanyCourses200Response>

    /**
     * POST block_iomad_company_admin_get_course_info
     * Get Iomad course settings
     * Get Iomad course settings
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetCourseInfoRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_get_course_info")
    fun blockIomadCompanyAdminGetCourseInfo(@Body elearningBlockIomadCompanyAdminGetCourseInfoRequest: ElearningBlockIomadCompanyAdminGetCourseInfoRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_get_department_users
     * Get users within a department
     * Get users within a department
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetDepartmentUsersRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetDepartmentUsers200Response]>
     */
    @POST("block_iomad_company_admin_get_department_users")
    fun blockIomadCompanyAdminGetDepartmentUsers(@Body elearningBlockIomadCompanyAdminGetDepartmentUsersRequest: ElearningBlockIomadCompanyAdminGetDepartmentUsersRequest): Call<ElearningBlockIomadCompanyAdminGetDepartmentUsers200Response>

    /**
     * POST block_iomad_company_admin_get_departments
     * Get all company departments
     * Get all company departments
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetDepartmentsRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetDepartments200Response]>
     */
    @POST("block_iomad_company_admin_get_departments")
    fun blockIomadCompanyAdminGetDepartments(@Body elearningBlockIomadCompanyAdminGetDepartmentsRequest: ElearningBlockIomadCompanyAdminGetDepartmentsRequest): Call<ElearningBlockIomadCompanyAdminGetDepartments200Response>

    /**
     * POST block_iomad_company_admin_get_license_from_id
     * Get licence data give the ID
     * Get licence data give the ID
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetLicenseFromIdRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetLicenseFromId200Response]>
     */
    @POST("block_iomad_company_admin_get_license_from_id")
    fun blockIomadCompanyAdminGetLicenseFromId(@Body elearningBlockIomadCompanyAdminGetLicenseFromIdRequest: ElearningBlockIomadCompanyAdminGetLicenseFromIdRequest): Call<ElearningBlockIomadCompanyAdminGetLicenseFromId200Response>

    /**
     * POST block_iomad_company_admin_get_license_info
     * Get company license information
     * Get company license information
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminGetLicenseInfoRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminGetLicenseInfo200Response]>
     */
    @POST("block_iomad_company_admin_get_license_info")
    fun blockIomadCompanyAdminGetLicenseInfo(@Body elearningBlockIomadCompanyAdminGetLicenseInfoRequest: ElearningBlockIomadCompanyAdminGetLicenseInfoRequest): Call<ElearningBlockIomadCompanyAdminGetLicenseInfo200Response>

    /**
     * POST block_iomad_company_admin_move_users
     * Move users between departments
     * Move users between departments
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminMoveUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_move_users")
    fun blockIomadCompanyAdminMoveUsers(@Body elearningBlockIomadCompanyAdminMoveUsersRequest: ElearningBlockIomadCompanyAdminMoveUsersRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_restrict_capability
     * set/reset Iomad capability
     * set/reset Iomad capability
     * Responses:
     *  - 200: True capability update succeeds
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminRestrictCapabilityRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_restrict_capability")
    fun blockIomadCompanyAdminRestrictCapability(@Body elearningBlockIomadCompanyAdminRestrictCapabilityRequest: ElearningBlockIomadCompanyAdminRestrictCapabilityRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_sync_users
     * Call update users to sync to external system
     * Call update users to sync to external system
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminSyncUsersRequest 
     * @return [Call]<[ElearningBlockIomadCompanyAdminSyncUsers200Response]>
     */
    @POST("block_iomad_company_admin_sync_users")
    fun blockIomadCompanyAdminSyncUsers(@Body elearningBlockIomadCompanyAdminSyncUsersRequest: ElearningBlockIomadCompanyAdminSyncUsersRequest): Call<ElearningBlockIomadCompanyAdminSyncUsers200Response>

    /**
     * POST block_iomad_company_admin_unallocate_licenses
     * Remove course licenses from users
     * Remove course licenses from users
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminUnallocateLicensesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_unallocate_licenses")
    fun blockIomadCompanyAdminUnallocateLicenses(@Body elearningBlockIomadCompanyAdminUnallocateLicensesRequest: ElearningBlockIomadCompanyAdminUnallocateLicensesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_unassign_courses
     * Unassign a course from a company
     * Unassign a course from a company
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminUnassignCoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_unassign_courses")
    fun blockIomadCompanyAdminUnassignCourses(@Body elearningBlockIomadCompanyAdminUnassignCoursesRequest: ElearningBlockIomadCompanyAdminUnassignCoursesRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_unassign_users
     * Unassign users from a company
     * Unassign users from a company
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminUnassignUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_unassign_users")
    fun blockIomadCompanyAdminUnassignUsers(@Body elearningBlockIomadCompanyAdminUnassignUsersRequest: ElearningBlockIomadCompanyAdminUnassignUsersRequest): Call<kotlin.Any>

    /**
     * POST block_iomad_company_admin_update_courses
     * Update Iomad course settings
     * Update Iomad course settings
     * Responses:
     *  - 200: Success or failure
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockIomadCompanyAdminUpdateCoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_iomad_company_admin_update_courses")
    fun blockIomadCompanyAdminUpdateCourses(@Body elearningBlockIomadCompanyAdminUpdateCoursesRequest: ElearningBlockIomadCompanyAdminUpdateCoursesRequest): Call<kotlin.Any>

}
