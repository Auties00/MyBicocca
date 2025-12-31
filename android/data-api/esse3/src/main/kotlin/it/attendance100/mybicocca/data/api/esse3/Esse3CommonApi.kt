package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentUploadResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3PageLoadedResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/**
 * # Esse3 Common API
 *
 * Provides shared system functionality, utilities, file uploads, and
 * process management workflows that cut across multiple domains.
 *
 * ## Key Features
 *
 * - **System:** Cookie config, Page load checks, Manifest.
 * - **Processes:** Workflow management (ProcessManagementWF) and Checklists.
 * - **Uploads:** Chunked file upload handling.
 * - **Utils:** Self-certification models.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Check system status
 * val status = commonApi.pageLoaded()
 *
 * // Upload a file
 * val start = commonApi.startChunkUpload(raw = "...")
 * // ... loop chunk uploads ...
 * val end = commonApi.endChunkUpload(raw = "...")
 * ```
 */
interface Esse3CommonApi {

    /**
     * Retrieves the cookie configuration script.
     *
     * @return A [Response] containing the script string.
     */
    @GET("CookieConfig.do")
    suspend fun getCookieConfig(): Response<String>

    /**
     * Checks if the page/system is loaded correctly.
     *
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.esse3.Esse3PageLoadedResponse] JSON.
     */
    @GET("PageLoaded.do")
    suspend fun pageLoaded(): Response<Esse3PageLoadedResponse>

    /**
     * Submits tracking data to Matomo.
     *
     * @param actionName Name of the action.
     * @param siteId Site ID.
     * @param rec Record flag.
     * @param url URL being tracked.
     * @param id Tracking ID.
     * @return A [Response] containing [Unit].
     */
    @POST("matomo.php")
    suspend fun trackMatomo(
        @Query("action_name") actionName: String? = null,
        @Query("idsite") siteId: String? = null,
        @Query("rec") rec: String? = null,
        @Query("url") url: String? = null,
        @Query("_id") id: String? = null
        // Add other params as needed
    ): Response<Unit>

    /**
     * Manages generic process workflows via POST.
     *
     * Used for stepping through wizards and multi-step processes.
     *
     * @param menuOpenedCod Optional menu context.
     * @param process Process code.
     * @param processId Process ID.
     * @param formId Form identifier.
     * @param btnProceed Proceed button action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("ProcessManagementWF.do")
    suspend fun processManagementWorkflow(
        @Query("menu_opened_cod") menuOpenedCod: String? = null,
        @Field("gp_processo") process: String? = null,
        @Field("gp_id_processo") processId: String? = null,
        @Field("form_id_form_processo") formId: String? = null,
        @Field("btnProcedi") btnProceed: String? = null
    ): Response<Unit>

    /**
     * Retrieves the Checklist Process page.
     *
     * @return A [Response] containing the HTML page.
     */
    @GET("checklist/CheckListProcesso.do")
    suspend fun getChecklistProcess(): Response<String>

    /**
     * Submits action for pending processes checklist.
     *
     * @param menuOpenedCod Optional menu context.
     * @param processIdCode Process ID code.
     * @param formId Form identifier.
     * @param cancel Cancel action flag.
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("checklist/ListaProcessiPendenti.do")
    suspend fun submitPendingProcessList(
        @Query("menu_opened_cod") menuOpenedCod: String? = null,
        @Field("id_proc_cod_proc") processIdCode: String? = null,
        @Field("form_id_form_processo") formId: String? = null,
        @Field("annulla") cancel: String? = null
    ): Response<String>

    /**
     * Cancels a mandatory process.
     *
     * @param processIdCode Process ID code.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/studente/CancelMandatoryProcess.do")
    suspend fun cancelMandatoryProcess(@Query("id_proc_cod_proc") processIdCode: String? = null): Response<Unit>

    /**
     * Retrieves the Self-Certification page.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/Autocertificazioni/Autocertificazione.do")
    suspend fun getSelfCertification(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves model types for self-certification.
     *
     * @param dataCode Data code filter.
     * @return A [Response] containing the HTML content.
     */
    @GET("auth/Autocertificazioni/TipiModelli.do")
    suspend fun getModelTypes(@Query("cod_dato") dataCode: String? = null): Response<String>

    /**
     * Initiates a chunked upload.
     *
     * @param raw Raw request data (JSON).
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.esse3.Esse3PageLoadedResponse].
     */
    @FormUrlEncoded
    @POST("auth/ChunkUploadStart.do")
    suspend fun startChunkUpload(@Field("raw") raw: String? = null): Response<Esse3PageLoadedResponse>

    /**
     * Uploads a chunk of data.
     *
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.esse3.Esse3PageLoadedResponse].
     */
    @POST("auth/ChunkUpload.do")
    suspend fun chunkUpload(): Response<Esse3PageLoadedResponse>

    /**
     * Finalizes the chunked upload.
     *
     * @param raw Raw request data (JSON).
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentUploadResponse].
     */
    @FormUrlEncoded
    @POST("auth/ChunkUploadEnd.do")
    suspend fun endChunkUpload(@Field("raw") raw: String? = null): Response<Esse3AttachmentUploadResponse>
}
