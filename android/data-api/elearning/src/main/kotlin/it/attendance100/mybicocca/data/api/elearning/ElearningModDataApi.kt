package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreContentbankRenameContent200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataAddEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataAddEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataApproveEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataDeleteEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataDeleteEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataDeleteSavedPresetRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetDataAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetDataAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetDatabasesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetEntries200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetEntriesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetFields200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetFieldsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetMappingInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataGetMappingInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataSearchEntries200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataSearchEntriesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataUpdateEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataUpdateEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModDataViewDatabaseRequest

interface ModDataApi {
    /**
     * POST mod_data_add_entry
     * Adds a new entry.
     * Adds a new entry.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataAddEntryRequest 
     * @return [Call]<[ElearningModDataAddEntry200Response]>
     */
    @POST("mod_data_add_entry")
    fun modDataAddEntry(@Body elearningModDataAddEntryRequest: ElearningModDataAddEntryRequest): Call<ElearningModDataAddEntry200Response>

    /**
     * POST mod_data_approve_entry
     * Approves or unapproves an entry.
     * Approves or unapproves an entry.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataApproveEntryRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_data_approve_entry")
    fun modDataApproveEntry(@Body elearningModDataApproveEntryRequest: ElearningModDataApproveEntryRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_data_delete_entry
     * Deletes an entry.
     * Deletes an entry.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataDeleteEntryRequest 
     * @return [Call]<[ElearningModDataDeleteEntry200Response]>
     */
    @POST("mod_data_delete_entry")
    fun modDataDeleteEntry(@Body elearningModDataDeleteEntryRequest: ElearningModDataDeleteEntryRequest): Call<ElearningModDataDeleteEntry200Response>

    /**
     * POST mod_data_delete_saved_preset
     * Delete site user preset.
     * Delete site user preset.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataDeleteSavedPresetRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("mod_data_delete_saved_preset")
    fun modDataDeleteSavedPreset(@Body elearningModDataDeleteSavedPresetRequest: ElearningModDataDeleteSavedPresetRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST mod_data_get_data_access_information
     * Return access information for a given database.
     * Return access information for a given database.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataGetDataAccessInformationRequest 
     * @return [Call]<[ElearningModDataGetDataAccessInformation200Response]>
     */
    @POST("mod_data_get_data_access_information")
    fun modDataGetDataAccessInformation(@Body elearningModDataGetDataAccessInformationRequest: ElearningModDataGetDataAccessInformationRequest): Call<ElearningModDataGetDataAccessInformation200Response>

    /**
     * POST mod_data_get_databases_by_courses
     * Returns a list of database instances in a provided set of courses, if             no courses are provided then all the database instances the user has access to will be returned.
     * Returns a list of database instances in a provided set of courses, if             no courses are provided then all the database instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModDataGetDatabasesByCourses200Response]>
     */
    @POST("mod_data_get_databases_by_courses")
    fun modDataGetDatabasesByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModDataGetDatabasesByCourses200Response>

    /**
     * POST mod_data_get_entries
     * Return the complete list of entries of the given database.
     * Return the complete list of entries of the given database.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataGetEntriesRequest 
     * @return [Call]<[ElearningModDataGetEntries200Response]>
     */
    @POST("mod_data_get_entries")
    fun modDataGetEntries(@Body elearningModDataGetEntriesRequest: ElearningModDataGetEntriesRequest): Call<ElearningModDataGetEntries200Response>

    /**
     * POST mod_data_get_entry
     * Return one entry record from the database, including contents optionally.
     * Return one entry record from the database, including contents optionally.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataGetEntryRequest 
     * @return [Call]<[ElearningModDataGetEntry200Response]>
     */
    @POST("mod_data_get_entry")
    fun modDataGetEntry(@Body elearningModDataGetEntryRequest: ElearningModDataGetEntryRequest): Call<ElearningModDataGetEntry200Response>

    /**
     * POST mod_data_get_fields
     * Return the list of configured fields for the given database.
     * Return the list of configured fields for the given database.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataGetFieldsRequest 
     * @return [Call]<[ElearningModDataGetFields200Response]>
     */
    @POST("mod_data_get_fields")
    fun modDataGetFields(@Body elearningModDataGetFieldsRequest: ElearningModDataGetFieldsRequest): Call<ElearningModDataGetFields200Response>

    /**
     * POST mod_data_get_mapping_information
     * Get importing information
     * Get importing information
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataGetMappingInformationRequest 
     * @return [Call]<[ElearningModDataGetMappingInformation200Response]>
     */
    @POST("mod_data_get_mapping_information")
    fun modDataGetMappingInformation(@Body elearningModDataGetMappingInformationRequest: ElearningModDataGetMappingInformationRequest): Call<ElearningModDataGetMappingInformation200Response>

    /**
     * POST mod_data_search_entries
     * Search for entries in the given database.
     * Search for entries in the given database.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataSearchEntriesRequest 
     * @return [Call]<[ElearningModDataSearchEntries200Response]>
     */
    @POST("mod_data_search_entries")
    fun modDataSearchEntries(@Body elearningModDataSearchEntriesRequest: ElearningModDataSearchEntriesRequest): Call<ElearningModDataSearchEntries200Response>

    /**
     * POST mod_data_update_entry
     * Updates an existing entry.
     * Updates an existing entry.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataUpdateEntryRequest 
     * @return [Call]<[ElearningModDataUpdateEntry200Response]>
     */
    @POST("mod_data_update_entry")
    fun modDataUpdateEntry(@Body elearningModDataUpdateEntryRequest: ElearningModDataUpdateEntryRequest): Call<ElearningModDataUpdateEntry200Response>

    /**
     * POST mod_data_view_database
     * Simulate the view.php web interface data: trigger events, completion, etc...
     * Simulate the view.php web interface data: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModDataViewDatabaseRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_data_view_database")
    fun modDataViewDatabase(@Body elearningModDataViewDatabaseRequest: ElearningModDataViewDatabaseRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
