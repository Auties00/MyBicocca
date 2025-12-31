package it.attendance100.mybicocca.data.api.esse3

import okhttp3.ResponseBody
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Multipart
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Part
import de.jensklingenberg.ktorfit.http.Query

/**
 * # Esse3 Career API
 *
 * Manages the student's academic career, including enrollment, study plans,
 * degree progress, certificates, and fee payments.
 *
 * ## Key Features
 *
 * - **Booklet (Libretto):** View grades and passed exams.
 * - **Enrollment:** Manage annual enrollment and matriculation.
 * - **Study Plans:** Create and modify study plans.
 * - **Fees (Tasse):** View invoices and payment status (PagoPA).
 * - **Certificates:** Request and download career certificates.
 * - **Degrees:** Manage graduation application and title registry.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get digital booklet
 * val booklet = careerApi.getBooklet()
 *
 * // List fee invoices
 * val invoices = careerApi.getInvoices()
 * ```
 */
interface Esse3CareerApi {

    /**
     * Accesses the student's digital booklet (Libretto).
     *
     * Displays a list of all educational activities, grades, and credits.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML booklet page.
     */
    @GET("auth/studente/Libretto/LibrettoHome.do")
    suspend fun getBooklet(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Accesses the Career Certificates page.
     *
     * Allows the student to request self-certifications or official certificates.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/Carriera/AttiCarriera.do")
    suspend fun getCareerCertificates(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Submits a request for a career certificate.
     *
     * @param year Academic year.
     * @param btnSubmit Button action.
     * @param includeAttachment Flag to include attachments.
     * @param status Status filter.
     * @param type Certificate type.
     * @param segment Career segment.
     * @return A [Response] containing the HTML result.
     */
    @FormUrlEncoded
    @POST("auth/studente/Carriera/AttiCarriera.do")
    suspend fun submitCareerCertificateRequest(
        @Field("anno") year: String? = null,
        @Field("btnSubmit") btnSubmit: String? = null,
        @Field("includiAllegato") includeAttachment: String? = null,
        @Field("stato") status: String? = null,
        @Field("tipo") type: String? = null,
        @Field("tratto") segment: String? = null
    ): Response<String>

    /**
     * Retrieves the list of annual enrollments.
     *
     * Shows history of enrollments for each academic year.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/ListaIscrizioni.do")
    suspend fun getEnrollments(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Initiates a new matriculation process.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/Enrollment/EImmatricolazioneNewAction.do")
    suspend fun startNewMatriculation(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the list of deadline extension requests.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/AdministrativeFunctions/DomProrogaElencoDomandeAction.do")
    suspend fun getExtensionRequests(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Accesses the Study Plans home page.
     *
     * Allows viewing and modifying the student's study plan.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/Piani/PianiHome.do")
    suspend fun getStudyPlans(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Generates a printable version of the study plan.
     *
     * @param planId The ID of the study plan.
     * @param btnSubmit Button action.
     * @return A [Response] containing the PDF/document [okhttp3.ResponseBody].
     */
    @FormUrlEncoded
    @POST("auth/studente/Piani/PianiStampaPiano.do")
    suspend fun printStudyPlan(
        @Query("PIANO_ID") planId: String,
        @Field("btnSubmit") btnSubmit: String? = null
    ): Response<ResponseBody>

    /**
     * Retrieves the list of invoices (Tasse).
     *
     * Displays paid and pending fees.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/Tasse/ListaFatture.do")
    suspend fun getInvoices(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Triggers a payment check or update.
     *
     * @param formId Form identifier, defaults to "formCtrlPagamenti".
     * @param checkPayments Action parameter to check payments.
     * @return A [Response] containing the updated HTML page.
     */
    @FormUrlEncoded
    @POST("auth/studente/Tasse/ListaFatture.do")
    suspend fun checkPayments(
        @Field("form_id_formCtrlPagamenti") formId: String? = "formCtrlPagamenti",
        @Field("bCtrlPagamenti") checkPayments: String? = null
    ): Response<String>

    /**
     * Retrieves details for a specific invoice.
     *
     * @param invoiceId The ID of the invoice (fattura).
     * @return A [Response] containing the HTML detail page.
     */
    @GET("auth/studente/Tasse/FatturaDettaglio.do")
    suspend fun getInvoiceDetail(@Query("fatt_id") invoiceId: String): Response<String>

    /**
     * Downloads the PagoPA payment receipt (Quietanza).
     *
     * @param invoiceId The invoice ID.
     * @param rptId The payment report ID.
     * @return A [Response] containing the receipt PDF.
     */
    @GET("auth/studente/Tasse/StampaQuietanzaPagoPA.do")
    suspend fun printPagoPaReceipt(
        @Query("fatt_id") invoiceId: String,
        @Query("rpt_id") rptId: String
    ): Response<ResponseBody>

    /**
     * Lists internal transfer requests (Passaggi di corso).
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/studente/Carriera/DomPassLista.do")
    suspend fun getInternalTransferRequests(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Retrieves the action page for transfer requests.
     *
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/Enrollment/EDomPassElencoAction.do")
    suspend fun getTransferRequestsAction(): Response<String>

    /**
     * Lists external transfer requests (Trasferimenti in uscita).
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/Carriera/DomTrasfLista.do")
    suspend fun getExternalTransferRequests(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Starts the process for managing academic titles/degrees.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/Degrees/DGAnagaficaTitoliStartProcesso.do")
    suspend fun startTitleRegistryProcess(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Retrieves the form for managing academic titles.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/Degrees/DGAnagraficaTitoliForm.do")
    suspend fun getTitleRegistryForm(): Response<String>

    /**
     * Submits data for an Italian university title.
     *
     * @param year Year of graduation.
     * @param description Course description.
     * @param path Study path/curriculum.
     * @param code Title code.
     * @param btnSubmit Button action.
     * @param formId Form identifier, defaults to "formDatiTit".
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/Degrees/DatiTitoloUnvSubmit.do")
    suspend fun submitUniversityTitleData(
        @Field("/WS/DataSet[@LocalEntityName='TIT_IT_WEB']/Row[@Num='1']/aa_conseg_titolo") year: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_IT_WEB']/Row[@Num='1']/des_cds") description: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_IT_WEB']/Row[@Num='1']/percorso_di_studio") path: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_IT_WEB']/Row[@Num='1']/titit_cod") code: String? = null,
        @Field("btnSubmit") btnSubmit: String? = null,
        @Field("form_id_formDatiTit") formId: String? = "formDatiTit"
    ): Response<Unit>

    /**
     * Submits data for a foreign university title.
     *
     * @param foreignUniversityId Foreign university ID.
     * @param foreignCourse Name of the foreign course.
     * @param date Date of graduation.
     * @param durationYears Duration in years.
     * @param nationId Nation ID.
     * @param typeCode Title type code.
     * @param grade Grade/Mark.
     * @param gradeAlpha Alphanumeric grade.
     * @param refresh Refresh parameter.
     * @param proceed Action parameter.
     * @param formId Form identifier, defaults to "formDatiTitolo".
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("auth/Degrees/DatiTitoloUnvStraSubmit.do")
    suspend fun submitForeignUniversityTitleData(
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/ateneo_straniero_id") foreignUniversityId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/cds_straniero") foreignCourse: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/data_conseg_titolo") date: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/durata_anni") durationYears: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/nazione_id") nationId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/tipo_titst_cod") typeCode: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/voto") grade: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_STRA_WEB']/Row[@Num='1']/voto_alfanumerico") gradeAlpha: String? = null,
        @Field("_fw_refresh-form.x") refresh: String? = null,
        @Field("btnProcedi") proceed: String? = null,
        @Field("form_id_formDatiTitolo") formId: String? = "formDatiTitolo"
    ): Response<String>

    /**
     * Retrieves the list of available certificates.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/Certificati/ListaCertificati.do")
    suspend fun getCertificatesList(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Accesses the page for requesting duplicate documents.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/RichiestaDuplicati.do")
    suspend fun getDuplicateRequests(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>
}
