package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreH5pGetTrustedH5pFile200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiEditPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiEditPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetPageContents200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetPageContentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetPageForEditing200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetPageForEditingRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetSubwikiFilesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetSubwikiPages200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetSubwikiPagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetSubwikis200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetSubwikisRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetWikisByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiGetWikisByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiNewPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiNewPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiViewPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiViewPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWikiViewWiki200Response

interface ModWikiApi {
    /**
     * POST mod_wiki_edit_page
     * Save the contents of a page.
     * Save the contents of a page.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiEditPageRequest 
     * @return [Call]<[ElearningModWikiEditPage200Response]>
     */
    @POST("mod_wiki_edit_page")
    fun modWikiEditPage(@Body elearningModWikiEditPageRequest: ElearningModWikiEditPageRequest): Call<ElearningModWikiEditPage200Response>

    /**
     * POST mod_wiki_get_page_contents
     * Returns the contents of a page.
     * Returns the contents of a page.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetPageContentsRequest 
     * @return [Call]<[ElearningModWikiGetPageContents200Response]>
     */
    @POST("mod_wiki_get_page_contents")
    fun modWikiGetPageContents(@Body elearningModWikiGetPageContentsRequest: ElearningModWikiGetPageContentsRequest): Call<ElearningModWikiGetPageContents200Response>

    /**
     * POST mod_wiki_get_page_for_editing
     * Locks and retrieves info of page-section to be edited.
     * Locks and retrieves info of page-section to be edited.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetPageForEditingRequest 
     * @return [Call]<[ElearningModWikiGetPageForEditing200Response]>
     */
    @POST("mod_wiki_get_page_for_editing")
    fun modWikiGetPageForEditing(@Body elearningModWikiGetPageForEditingRequest: ElearningModWikiGetPageForEditingRequest): Call<ElearningModWikiGetPageForEditing200Response>

    /**
     * POST mod_wiki_get_subwiki_files
     * Returns the list of files for a specific subwiki.
     * Returns the list of files for a specific subwiki.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetSubwikiFilesRequest 
     * @return [Call]<[ElearningCoreH5pGetTrustedH5pFile200Response]>
     */
    @POST("mod_wiki_get_subwiki_files")
    fun modWikiGetSubwikiFiles(@Body elearningModWikiGetSubwikiFilesRequest: ElearningModWikiGetSubwikiFilesRequest): Call<ElearningCoreH5pGetTrustedH5pFile200Response>

    /**
     * POST mod_wiki_get_subwiki_pages
     * Returns the list of pages for a specific subwiki.
     * Returns the list of pages for a specific subwiki.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetSubwikiPagesRequest 
     * @return [Call]<[ElearningModWikiGetSubwikiPages200Response]>
     */
    @POST("mod_wiki_get_subwiki_pages")
    fun modWikiGetSubwikiPages(@Body elearningModWikiGetSubwikiPagesRequest: ElearningModWikiGetSubwikiPagesRequest): Call<ElearningModWikiGetSubwikiPages200Response>

    /**
     * POST mod_wiki_get_subwikis
     * Returns the list of subwikis the user can see in a specific wiki.
     * Returns the list of subwikis the user can see in a specific wiki.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetSubwikisRequest 
     * @return [Call]<[ElearningModWikiGetSubwikis200Response]>
     */
    @POST("mod_wiki_get_subwikis")
    fun modWikiGetSubwikis(@Body elearningModWikiGetSubwikisRequest: ElearningModWikiGetSubwikisRequest): Call<ElearningModWikiGetSubwikis200Response>

    /**
     * POST mod_wiki_get_wikis_by_courses
     * Returns a list of wiki instances in a provided set of courses, if no courses are provided then all the wiki instances the user has access to will be returned.
     * Returns a list of wiki instances in a provided set of courses, if no courses are provided then all the wiki instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetWikisByCoursesRequest 
     * @return [Call]<[ElearningModWikiGetWikisByCourses200Response]>
     */
    @POST("mod_wiki_get_wikis_by_courses")
    fun modWikiGetWikisByCourses(@Body elearningModWikiGetWikisByCoursesRequest: ElearningModWikiGetWikisByCoursesRequest): Call<ElearningModWikiGetWikisByCourses200Response>

    /**
     * POST mod_wiki_new_page
     * Create a new page in a subwiki.
     * Create a new page in a subwiki.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiNewPageRequest 
     * @return [Call]<[ElearningModWikiNewPage200Response]>
     */
    @POST("mod_wiki_new_page")
    fun modWikiNewPage(@Body elearningModWikiNewPageRequest: ElearningModWikiNewPageRequest): Call<ElearningModWikiNewPage200Response>

    /**
     * POST mod_wiki_view_page
     * Trigger the page viewed event and update the module completion status.
     * Trigger the page viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiViewPageRequest 
     * @return [Call]<[ElearningModWikiViewPage200Response]>
     */
    @POST("mod_wiki_view_page")
    fun modWikiViewPage(@Body elearningModWikiViewPageRequest: ElearningModWikiViewPageRequest): Call<ElearningModWikiViewPage200Response>

    /**
     * POST mod_wiki_view_wiki
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWikiGetSubwikisRequest 
     * @return [Call]<[ElearningModWikiViewWiki200Response]>
     */
    @POST("mod_wiki_view_wiki")
    fun modWikiViewWiki(@Body elearningModWikiGetSubwikisRequest: ElearningModWikiGetSubwikisRequest): Call<ElearningModWikiViewWiki200Response>

}
