package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreContentbankRenameContent200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyApproveDataRequestRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyBulkApproveDataRequestsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyBulkDenyDataRequestsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyCancelDataRequestRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyConfirmContextsForDeletion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyConfirmContextsForDeletionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyContactDpoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyCreateCategoryForm200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyCreateCategoryFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyCreatePurposeForm200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyCreatePurposeFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyDeleteCategoryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyDeletePurposeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetActivityOptions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetActivityOptionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetCategoryOptions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetCategoryOptionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetDataRequest200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetPurposeOptions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyGetUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextDefaults200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextDefaultsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextForm200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextlevelForm200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySetContextlevelFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacySubmitSelectedCoursesFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyTreeExtraBranches200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolDataprivacyTreeExtraBranchesRequest

interface ToolDataprivacyApi {
    /**
     * POST tool_dataprivacy_approve_data_request
     * Approve a data request
     * Approve a data request
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyApproveDataRequestRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_approve_data_request")
    fun toolDataprivacyApproveDataRequest(@Body elearningToolDataprivacyApproveDataRequestRequest: ElearningToolDataprivacyApproveDataRequestRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_bulk_approve_data_requests
     * Bulk approve data requests
     * Bulk approve data requests
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyBulkApproveDataRequestsRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_bulk_approve_data_requests")
    fun toolDataprivacyBulkApproveDataRequests(@Body elearningToolDataprivacyBulkApproveDataRequestsRequest: ElearningToolDataprivacyBulkApproveDataRequestsRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_bulk_deny_data_requests
     * Bulk deny data requests
     * Bulk deny data requests
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyBulkDenyDataRequestsRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_bulk_deny_data_requests")
    fun toolDataprivacyBulkDenyDataRequests(@Body elearningToolDataprivacyBulkDenyDataRequestsRequest: ElearningToolDataprivacyBulkDenyDataRequestsRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_cancel_data_request
     * Cancel the data request made by the user
     * Cancel the data request made by the user
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCancelDataRequestRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_cancel_data_request")
    fun toolDataprivacyCancelDataRequest(@Body elearningToolDataprivacyCancelDataRequestRequest: ElearningToolDataprivacyCancelDataRequestRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_confirm_contexts_for_deletion
     * Mark the selected expired contexts as confirmed for deletion
     * Mark the selected expired contexts as confirmed for deletion
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyConfirmContextsForDeletionRequest 
     * @return [Call]<[ElearningToolDataprivacyConfirmContextsForDeletion200Response]>
     */
    @POST("tool_dataprivacy_confirm_contexts_for_deletion")
    fun toolDataprivacyConfirmContextsForDeletion(@Body elearningToolDataprivacyConfirmContextsForDeletionRequest: ElearningToolDataprivacyConfirmContextsForDeletionRequest): Call<ElearningToolDataprivacyConfirmContextsForDeletion200Response>

    /**
     * POST tool_dataprivacy_contact_dpo
     * Contact the site Data Protection Officer(s)
     * Contact the site Data Protection Officer(s)
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyContactDpoRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_contact_dpo")
    fun toolDataprivacyContactDpo(@Body elearningToolDataprivacyContactDpoRequest: ElearningToolDataprivacyContactDpoRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_create_category_form
     * Adds a data category
     * Adds a data category
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCreateCategoryFormRequest 
     * @return [Call]<[ElearningToolDataprivacyCreateCategoryForm200Response]>
     */
    @POST("tool_dataprivacy_create_category_form")
    fun toolDataprivacyCreateCategoryForm(@Body elearningToolDataprivacyCreateCategoryFormRequest: ElearningToolDataprivacyCreateCategoryFormRequest): Call<ElearningToolDataprivacyCreateCategoryForm200Response>

    /**
     * POST tool_dataprivacy_create_purpose_form
     * Adds a data purpose
     * Adds a data purpose
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCreatePurposeFormRequest 
     * @return [Call]<[ElearningToolDataprivacyCreatePurposeForm200Response]>
     */
    @POST("tool_dataprivacy_create_purpose_form")
    fun toolDataprivacyCreatePurposeForm(@Body elearningToolDataprivacyCreatePurposeFormRequest: ElearningToolDataprivacyCreatePurposeFormRequest): Call<ElearningToolDataprivacyCreatePurposeForm200Response>

    /**
     * POST tool_dataprivacy_delete_category
     * Deletes an existing data category
     * Deletes an existing data category
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyDeleteCategoryRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_delete_category")
    fun toolDataprivacyDeleteCategory(@Body elearningToolDataprivacyDeleteCategoryRequest: ElearningToolDataprivacyDeleteCategoryRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_delete_purpose
     * Deletes an existing data purpose
     * Deletes an existing data purpose
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyDeletePurposeRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_delete_purpose")
    fun toolDataprivacyDeletePurpose(@Body elearningToolDataprivacyDeletePurposeRequest: ElearningToolDataprivacyDeletePurposeRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_deny_data_request
     * Deny a data request
     * Deny a data request
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCancelDataRequestRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_deny_data_request")
    fun toolDataprivacyDenyDataRequest(@Body elearningToolDataprivacyCancelDataRequestRequest: ElearningToolDataprivacyCancelDataRequestRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_get_activity_options
     * Fetches a list of activity options
     * Fetches a list of activity options
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyGetActivityOptionsRequest 
     * @return [Call]<[ElearningToolDataprivacyGetActivityOptions200Response]>
     */
    @POST("tool_dataprivacy_get_activity_options")
    fun toolDataprivacyGetActivityOptions(@Body elearningToolDataprivacyGetActivityOptionsRequest: ElearningToolDataprivacyGetActivityOptionsRequest): Call<ElearningToolDataprivacyGetActivityOptions200Response>

    /**
     * POST tool_dataprivacy_get_category_options
     * Fetches a list of data category options
     * Fetches a list of data category options
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyGetCategoryOptionsRequest 
     * @return [Call]<[ElearningToolDataprivacyGetCategoryOptions200Response]>
     */
    @POST("tool_dataprivacy_get_category_options")
    fun toolDataprivacyGetCategoryOptions(@Body elearningToolDataprivacyGetCategoryOptionsRequest: ElearningToolDataprivacyGetCategoryOptionsRequest): Call<ElearningToolDataprivacyGetCategoryOptions200Response>

    /**
     * POST tool_dataprivacy_get_data_request
     * Fetch the details of a user&#39;s data request
     * Fetch the details of a user&#39;s data request
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCancelDataRequestRequest 
     * @return [Call]<[ElearningToolDataprivacyGetDataRequest200Response]>
     */
    @POST("tool_dataprivacy_get_data_request")
    fun toolDataprivacyGetDataRequest(@Body elearningToolDataprivacyCancelDataRequestRequest: ElearningToolDataprivacyCancelDataRequestRequest): Call<ElearningToolDataprivacyGetDataRequest200Response>

    /**
     * POST tool_dataprivacy_get_purpose_options
     * Fetches a list of data storage purpose options
     * Fetches a list of data storage purpose options
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyGetCategoryOptionsRequest 
     * @return [Call]<[ElearningToolDataprivacyGetPurposeOptions200Response]>
     */
    @POST("tool_dataprivacy_get_purpose_options")
    fun toolDataprivacyGetPurposeOptions(@Body elearningToolDataprivacyGetCategoryOptionsRequest: ElearningToolDataprivacyGetCategoryOptionsRequest): Call<ElearningToolDataprivacyGetPurposeOptions200Response>

    /**
     * POST tool_dataprivacy_get_users
     * Fetches a list of users
     * Fetches a list of users
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyGetUsersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_dataprivacy_get_users")
    fun toolDataprivacyGetUsers(@Body elearningToolDataprivacyGetUsersRequest: ElearningToolDataprivacyGetUsersRequest): Call<kotlin.Any>

    /**
     * POST tool_dataprivacy_mark_complete
     * Mark a user&#39;s general enquiry as complete
     * Mark a user&#39;s general enquiry as complete
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyCancelDataRequestRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_mark_complete")
    fun toolDataprivacyMarkComplete(@Body elearningToolDataprivacyCancelDataRequestRequest: ElearningToolDataprivacyCancelDataRequestRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_set_context_defaults
     * Updates the default category and purpose for a given context level (and optionally, a plugin)
     * Updates the default category and purpose for a given context level (and optionally, a plugin)
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacySetContextDefaultsRequest 
     * @return [Call]<[ElearningToolDataprivacySetContextDefaults200Response]>
     */
    @POST("tool_dataprivacy_set_context_defaults")
    fun toolDataprivacySetContextDefaults(@Body elearningToolDataprivacySetContextDefaultsRequest: ElearningToolDataprivacySetContextDefaultsRequest): Call<ElearningToolDataprivacySetContextDefaults200Response>

    /**
     * POST tool_dataprivacy_set_context_form
     * Sets purpose and category for a specific context
     * Sets purpose and category for a specific context
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacySetContextFormRequest 
     * @return [Call]<[ElearningToolDataprivacySetContextForm200Response]>
     */
    @POST("tool_dataprivacy_set_context_form")
    fun toolDataprivacySetContextForm(@Body elearningToolDataprivacySetContextFormRequest: ElearningToolDataprivacySetContextFormRequest): Call<ElearningToolDataprivacySetContextForm200Response>

    /**
     * POST tool_dataprivacy_set_contextlevel_form
     * Sets purpose and category across a context level
     * Sets purpose and category across a context level
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacySetContextlevelFormRequest 
     * @return [Call]<[ElearningToolDataprivacySetContextlevelForm200Response]>
     */
    @POST("tool_dataprivacy_set_contextlevel_form")
    fun toolDataprivacySetContextlevelForm(@Body elearningToolDataprivacySetContextlevelFormRequest: ElearningToolDataprivacySetContextlevelFormRequest): Call<ElearningToolDataprivacySetContextlevelForm200Response>

    /**
     * POST tool_dataprivacy_submit_selected_courses_form
     * Save list of selected courses for export
     * Save list of selected courses for export
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacySubmitSelectedCoursesFormRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("tool_dataprivacy_submit_selected_courses_form")
    fun toolDataprivacySubmitSelectedCoursesForm(@Body elearningToolDataprivacySubmitSelectedCoursesFormRequest: ElearningToolDataprivacySubmitSelectedCoursesFormRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST tool_dataprivacy_tree_extra_branches
     * Return branches for the context tree
     * Return branches for the context tree
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolDataprivacyTreeExtraBranchesRequest 
     * @return [Call]<[ElearningToolDataprivacyTreeExtraBranches200Response]>
     */
    @POST("tool_dataprivacy_tree_extra_branches")
    fun toolDataprivacyTreeExtraBranches(@Body elearningToolDataprivacyTreeExtraBranchesRequest: ElearningToolDataprivacyTreeExtraBranchesRequest): Call<ElearningToolDataprivacyTreeExtraBranches200Response>

}
