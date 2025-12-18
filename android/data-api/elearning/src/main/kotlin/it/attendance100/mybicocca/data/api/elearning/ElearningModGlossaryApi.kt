package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreContentbankRenameContent200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryAddEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryAddEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryDeleteEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetAuthors200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetAuthorsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetCategories200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetCategoriesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByAuthor200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByAuthorId200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByAuthorIdRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByAuthorRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByCategory200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByCategoryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByDateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByLetterRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesBySearchRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesByTermRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntriesToApproveRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntryById200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetEntryByIdRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryGetGlossariesByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryPrepareEntryForEdition200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryPrepareEntryForEditionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryUpdateEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryUpdateEntryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryViewEntry200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryViewGlossary200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModGlossaryViewGlossaryRequest

interface ModGlossaryApi {
    /**
     * POST mod_glossary_add_entry
     * Add a new entry to a given glossary
     * Add a new entry to a given glossary
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryAddEntryRequest 
     * @return [Call]<[ElearningModGlossaryAddEntry200Response]>
     */
    @POST("mod_glossary_add_entry")
    fun modGlossaryAddEntry(@Body elearningModGlossaryAddEntryRequest: ElearningModGlossaryAddEntryRequest): Call<ElearningModGlossaryAddEntry200Response>

    /**
     * POST mod_glossary_delete_entry
     * Delete the given entry from the glossary.
     * Delete the given entry from the glossary.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryDeleteEntryRequest 
     * @return [Call]<[ElearningCoreContentbankRenameContent200Response]>
     */
    @POST("mod_glossary_delete_entry")
    fun modGlossaryDeleteEntry(@Body elearningModGlossaryDeleteEntryRequest: ElearningModGlossaryDeleteEntryRequest): Call<ElearningCoreContentbankRenameContent200Response>

    /**
     * POST mod_glossary_get_authors
     * Get the authors.
     * Get the authors.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetAuthorsRequest 
     * @return [Call]<[ElearningModGlossaryGetAuthors200Response]>
     */
    @POST("mod_glossary_get_authors")
    fun modGlossaryGetAuthors(@Body elearningModGlossaryGetAuthorsRequest: ElearningModGlossaryGetAuthorsRequest): Call<ElearningModGlossaryGetAuthors200Response>

    /**
     * POST mod_glossary_get_categories
     * Get the categories.
     * Get the categories.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetCategoriesRequest 
     * @return [Call]<[ElearningModGlossaryGetCategories200Response]>
     */
    @POST("mod_glossary_get_categories")
    fun modGlossaryGetCategories(@Body elearningModGlossaryGetCategoriesRequest: ElearningModGlossaryGetCategoriesRequest): Call<ElearningModGlossaryGetCategories200Response>

    /**
     * POST mod_glossary_get_entries_by_author
     * Browse entries by author.
     * Browse entries by author.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByAuthorRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthor200Response]>
     */
    @POST("mod_glossary_get_entries_by_author")
    fun modGlossaryGetEntriesByAuthor(@Body elearningModGlossaryGetEntriesByAuthorRequest: ElearningModGlossaryGetEntriesByAuthorRequest): Call<ElearningModGlossaryGetEntriesByAuthor200Response>

    /**
     * POST mod_glossary_get_entries_by_author_id
     * Browse entries by author ID.
     * Browse entries by author ID.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByAuthorIdRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_by_author_id")
    fun modGlossaryGetEntriesByAuthorId(@Body elearningModGlossaryGetEntriesByAuthorIdRequest: ElearningModGlossaryGetEntriesByAuthorIdRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entries_by_category
     * Browse entries by category.
     * Browse entries by category.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByCategoryRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByCategory200Response]>
     */
    @POST("mod_glossary_get_entries_by_category")
    fun modGlossaryGetEntriesByCategory(@Body elearningModGlossaryGetEntriesByCategoryRequest: ElearningModGlossaryGetEntriesByCategoryRequest): Call<ElearningModGlossaryGetEntriesByCategory200Response>

    /**
     * POST mod_glossary_get_entries_by_date
     * Browse entries by date.
     * Browse entries by date.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByDateRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_by_date")
    fun modGlossaryGetEntriesByDate(@Body elearningModGlossaryGetEntriesByDateRequest: ElearningModGlossaryGetEntriesByDateRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entries_by_letter
     * Browse entries by letter.
     * Browse entries by letter.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByLetterRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_by_letter")
    fun modGlossaryGetEntriesByLetter(@Body elearningModGlossaryGetEntriesByLetterRequest: ElearningModGlossaryGetEntriesByLetterRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entries_by_search
     * Browse entries by search query.
     * Browse entries by search query.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesBySearchRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_by_search")
    fun modGlossaryGetEntriesBySearch(@Body elearningModGlossaryGetEntriesBySearchRequest: ElearningModGlossaryGetEntriesBySearchRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entries_by_term
     * Browse entries by term (concept or alias).
     * Browse entries by term (concept or alias).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesByTermRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_by_term")
    fun modGlossaryGetEntriesByTerm(@Body elearningModGlossaryGetEntriesByTermRequest: ElearningModGlossaryGetEntriesByTermRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entries_to_approve
     * Browse entries to be approved.
     * Browse entries to be approved.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntriesToApproveRequest 
     * @return [Call]<[ElearningModGlossaryGetEntriesByAuthorId200Response]>
     */
    @POST("mod_glossary_get_entries_to_approve")
    fun modGlossaryGetEntriesToApprove(@Body elearningModGlossaryGetEntriesToApproveRequest: ElearningModGlossaryGetEntriesToApproveRequest): Call<ElearningModGlossaryGetEntriesByAuthorId200Response>

    /**
     * POST mod_glossary_get_entry_by_id
     * Get an entry by ID
     * Get an entry by ID
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntryByIdRequest 
     * @return [Call]<[ElearningModGlossaryGetEntryById200Response]>
     */
    @POST("mod_glossary_get_entry_by_id")
    fun modGlossaryGetEntryById(@Body elearningModGlossaryGetEntryByIdRequest: ElearningModGlossaryGetEntryByIdRequest): Call<ElearningModGlossaryGetEntryById200Response>

    /**
     * POST mod_glossary_get_glossaries_by_courses
     * Retrieve a list of glossaries from several courses.
     * Retrieve a list of glossaries from several courses.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModGlossaryGetGlossariesByCourses200Response]>
     */
    @POST("mod_glossary_get_glossaries_by_courses")
    fun modGlossaryGetGlossariesByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModGlossaryGetGlossariesByCourses200Response>

    /**
     * POST mod_glossary_prepare_entry_for_edition
     * Prepares the given entry for edition returning draft item areas and file areas information.
     * Prepares the given entry for edition returning draft item areas and file areas information.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryPrepareEntryForEditionRequest 
     * @return [Call]<[ElearningModGlossaryPrepareEntryForEdition200Response]>
     */
    @POST("mod_glossary_prepare_entry_for_edition")
    fun modGlossaryPrepareEntryForEdition(@Body elearningModGlossaryPrepareEntryForEditionRequest: ElearningModGlossaryPrepareEntryForEditionRequest): Call<ElearningModGlossaryPrepareEntryForEdition200Response>

    /**
     * POST mod_glossary_update_entry
     * Updates the given glossary entry.
     * Updates the given glossary entry.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryUpdateEntryRequest 
     * @return [Call]<[ElearningModGlossaryUpdateEntry200Response]>
     */
    @POST("mod_glossary_update_entry")
    fun modGlossaryUpdateEntry(@Body elearningModGlossaryUpdateEntryRequest: ElearningModGlossaryUpdateEntryRequest): Call<ElearningModGlossaryUpdateEntry200Response>

    /**
     * POST mod_glossary_view_entry
     * Notify a glossary entry as being viewed.
     * Notify a glossary entry as being viewed.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryGetEntryByIdRequest 
     * @return [Call]<[ElearningModGlossaryViewEntry200Response]>
     */
    @POST("mod_glossary_view_entry")
    fun modGlossaryViewEntry(@Body elearningModGlossaryGetEntryByIdRequest: ElearningModGlossaryGetEntryByIdRequest): Call<ElearningModGlossaryViewEntry200Response>

    /**
     * POST mod_glossary_view_glossary
     * Notify the glossary as being viewed.
     * Notify the glossary as being viewed.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModGlossaryViewGlossaryRequest 
     * @return [Call]<[ElearningModGlossaryViewGlossary200Response]>
     */
    @POST("mod_glossary_view_glossary")
    fun modGlossaryViewGlossary(@Body elearningModGlossaryViewGlossaryRequest: ElearningModGlossaryViewGlossaryRequest): Call<ElearningModGlossaryViewGlossary200Response>

}
