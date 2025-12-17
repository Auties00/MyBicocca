package it.attendance100.mybicocca.data.api.esse3

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # Esse3 Internship API
 *
 * Manages Internships (Tirocini) and Stages.
 * Allows searching for companies and opportunities, and managing candidacies.
 *
 * ## Key Features
 *
 * - **Home:** Dashboard for internships.
 * - **Search:** Search for companies and internship opportunities.
 * - **Candidacy:** Apply for opportunities and track status.
 * - **My Internships:** View active and past internships.
 * - **Favorites:** Save interesting opportunities.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Search for companies in the IT sector
 * internshipApi.filterCompanies(
 *     sector = "IT",
 *     businessName = "Tech Corp"
 * )
 *
 * // View details of an opportunity
 * val details = internshipApi.getOpportunityDetail(
 *     opportunityId = "998877"
 * )
 * ```
 */
interface Esse3InternshipApi {

    /**
     * Accesses the Internship Student Home page.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/tirocini/TiroHomeStudente.do")
    suspend fun getInternshipHome(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the "My Internships" page.
     *
     * Lists current and historical internship activities.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/tirocini/MieiStage.do")
    suspend fun getMyInternships(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Accesses the Company Search page.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML search form.
     */
    @GET("auth/studente/tirocini/RicercaAzienda.do")
    suspend fun searchCompanies(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Filters and lists companies based on search criteria.
     *
     * @param businessName Name of the company.
     * @param sector Business sector.
     * @param cdsFlag Course of study flag.
     * @param advancedSearch Enable advanced search.
     * @param areaOldId Old area ID.
     * @param formId Form identifier, defaults to "tiro-Aziende-formSearch".
     * @param btnFilter Filter button action.
     * @param employeesRange Range of employees code.
     * @param areaId Geographic area ID.
     * @param areaDes Area description.
     * @param dateFrom Opportunity date from.
     * @param dateTo Opportunity date to.
     * @param dipFlag Department flag.
     * @return A [Response] containing the HTML results.
     */
    @FormUrlEncoded
    @POST("auth/studente/tirocini/ElencoAziende.do")
    suspend fun filterCompanies(
        @Field("ragione_sociale") businessName: String? = null,
        @Field("settore") sector: String? = null,
        @Field("cds_flg") cdsFlag: String? = null,
        @Field("advanced_search") advancedSearch: String? = null,
        @Field("area_old_id") areaOldId: String? = null,
        @Field("form_id_tiro-Aziende-formSearch") formId: String? = "tiro-Aziende-formSearch",
        @Field("sbmFiltra") btnFilter: String? = null,
        @Field("fascia_dip_cod") employeesRange: String? = null,
        @Field("area_id") areaId: String? = null,
        @Field("area_des") areaDes: String? = null,
        @Field("data_opp_da") dateFrom: String? = null,
        @Field("data_opp_a") dateTo: String? = null,
        @Field("dip_flg") dipFlag: String? = null
    ): Response<String>

    /**
     * Accesses the Opportunity Search page.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML search form.
     */
    @GET("auth/studente/tirocini/RicercaOpportunita.do")
    suspend fun searchOpportunities(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Submits a search for internship opportunities.
     *
     * @param searchText Text to search.
     * @param opportunityType Type of opportunity.
     * @param btnFilter Filter button action.
     * @param advancedSearch Advanced search flag.
     * @param saveDescription Description to save search.
     * @param searchId ID of saved search.
     * @param userInsId Insert user ID.
     * @param campaignId Campaign ID.
     * @param discAreaId Disciplinary area ID.
     * @param discAreaOldId Old disciplinary area ID.
     * @param formId Form identifier, defaults to "searchForm".
     * @param title Opportunity title.
     * @param description Opportunity description.
     * @param objectives Objectives.
     * @param dateStartFrom Start date range (from).
     * @param dateStartTo Start date range (to).
     * @param dateEndFrom End date range (from).
     * @param dateEndTo End date range (to).
     * @param sectorAreaId Sector area ID.
     * @param area Geographic area.
     * @param sector Business sector.
     * @param company Company name.
     * @param nation Nation.
     * @param protectedCategory Protected category flag.
     * @param btnSave Save button action.
     * @return A [Response] containing the HTML results.
     */
    @FormUrlEncoded
    @POST("auth/tirocini/TiroSearchOpportunita.do")
    suspend fun submitOpportunitySearch(
        @Field("search_testo") searchText: String? = null,
        @Field("tipoOpportunita") opportunityType: String? = null,
        @Field("sbmFiltra") btnFilter: String? = null,
        @Field("advanced_search") advancedSearch: String? = null,
        @Field("saveDescription") saveDescription: String? = null,
        @Field("search_id") searchId: String? = null,
        @Field("userInsId") userInsId: String? = null,
        @Field("campagna_id") campaignId: String? = null,
        @Field("area_disc_id") discAreaId: String? = null,
        @Field("area_disc_old_id") discAreaOldId: String? = null,
        @Field("form_id_searchForm") formId: String? = "searchForm",
        @Field("title") title: String? = null,
        @Field("description") description: String? = null,
        @Field("req_obiett") objectives: String? = null,
        @Field("data_ini_iscr_da") dateStartFrom: String? = null,
        @Field("data_ini_iscr_a") dateStartTo: String? = null,
        @Field("data_fin_iscr_da") dateEndFrom: String? = null,
        @Field("data_fin_iscr_a") dateEndTo: String? = null,
        @Field("sett_area_disc_id") sectorAreaId: String? = null,
        @Field("area") area: String? = null,
        @Field("settore") sector: String? = null,
        @Field("azienda") company: String? = null,
        @Field("sedeNazione") nation: String? = null,
        @Field("categoriaProtetta") protectedCategory: String? = null,
        @Field("sbmSave") btnSave: String? = null
    ): Response<String>

    /**
     * Retrieves the details of a specific opportunity.
     *
     * @param opportunityId The ID of the opportunity (convenzione offerta ID).
     * @param fromPage Context of the previous page.
     * @return A [Response] containing the HTML detail page.
     */
    @GET("auth/studente/tirocini/DettaglioOpportunita.do")
    suspend fun getOpportunityDetail(
        @Query("cnvz_off_id") opportunityId: String,
        @Query("from_page") fromPage: String? = null
    ): Response<String>

    /**
     * Saves an opportunity to the favorites list.
     *
     * @param opportunityId The opportunity ID.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/tirocini/SalvaOpportunitaFav.do")
    suspend fun saveOpportunity(@Query("cnvz_off_id") opportunityId: String): Response<Unit>

    /**
     * Removes an opportunity from the favorites list.
     *
     * @param opportunityId The opportunity ID.
     * @param goToList Flag to redirect to list.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/tirocini/RimuoviOpportunitaFav.do")
    suspend fun removeOpportunity(
        @Query("cnvz_off_id") opportunityId: String,
        @Query("GO_TO_LISTA_OPP_SAVED") goToList: String? = null
    ): Response<Unit>

    /**
     * Retrieves the list of saved opportunities.
     *
     * @param menuOpenedCod Optional menu context.
     * @param goToList Flag to view list.
     * @param opportunityId Optional ID context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/tirocini/OpportunitaSalvate.do")
    suspend fun getSavedOpportunities(
        @Query("menu_opened_cod") menuOpenedCod: String? = null,
        @Query("GO_TO_LISTA_OPP_SAVED") goToList: String? = null,
        @Query("cnvz_off_id") opportunityId: String? = null
    ): Response<String>

    /**
     * Retrieves the summary of student candidacies.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML summary.
     */
    @GET("auth/studente/tirocini/RiepilogoCandidature.do")
    suspend fun getCandidaciesSummary(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Initiates the candidacy process for an opportunity.
     *
     * @param opportunityId The opportunity ID.
     * @param onlyEnrolled Filter for only enrolled students.
     * @param receiptMode Receipt mode flag.
     * @param fromPage Context of previous page.
     * @return A [Response] containing the HTML candidacy form.
     */
    @GET("auth/studente/tirocini/TiroCandidaturaOpportunita.do")
    suspend fun startCandidacy(
        @Query("cnvz_off_id") opportunityId: String,
        @Query("soloIscr") onlyEnrolled: String? = null,
        @Query("mod_ricez_candidat") receiptMode: String? = null,
        @Query("from_page") fromPage: String? = null
    ): Response<String>

    /**
     * Submits the candidacy for an opportunity.
     *
     * @return A [Response] containing [Unit].
     */
    @POST("auth/studente/tirocini/CandidaturaOpportunitaSubmit.do")
    suspend fun submitCandidacy(): Response<Unit>

    /**
     * Downloads the logo of a company.
     *
     * @param attachmentId The ID of the logo attachment.
     * @return A [Response] containing [Unit] (image data).
     */
    @GET("auth/tirocini/DownloadLogoAzienda.do")
    suspend fun downloadCompanyLogo(@Query("allegato_id") attachmentId: String): Response<Unit>

    /**
     * Views the company presentation/profile.
     *
     * @param companyId The ID of the company/entity.
     * @param fromPage Context of previous page.
     * @return A [Response] containing the HTML profile.
     */
    @GET("auth/studente/tirocini/VisualizzaPresentazioneAzienda.do")
    suspend fun viewCompanyPresentation(
        @Query("ente_id") companyId: String,
        @Query("from_page") fromPage: String? = null
    ): Response<String>
    
    /**
     * Saves the current search criteria for future use.
     *
     * @param advancedSearch Flag for advanced search.
     * @param description Description for the saved search.
     * @param formId Form identifier, defaults to "formSave".
     * @param btnSave Save button action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/tirocini/TiroSaveSearchSubmit.do")
    suspend fun saveSearch(
         @Query("advanced_search") advancedSearch: String? = null,
         @Field("saveDescription") description: String? = null,
         @Field("form_id_formSave") formId: String? = "formSave",
         @Field("sbmSave") btnSave: String? = null
    ): Response<Unit>

    /**
     * Lists the saved searches.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/tirocini/TiroListaSavedSearch.do")
    suspend fun getSavedSearches(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Deletes a saved search.
     *
     * @param searchId The ID of the search to delete.
     * @return A [Response] containing the HTML confirmation.
     */
    @GET("auth/tirocini/TiroDeleteSearch.do")
    suspend fun deleteSavedSearch(@Query("search_id") searchId: String): Response<String>
}