package it.attendance100.mybicocca.data.api.esse3

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # Esse3 Admission API
 *
 * Handles Admissions, Competitive Exams (Concorsi), and Right to Study (Diritto allo Studio).
 *
 * ## Key Features
 *
 * - **Admission Boards:** View notices for admissions and competitions.
 * - **Competitions:** Register for entrance exams and view rankings.
 * - **Scholarships:** Apply for and view scholarships and student aid.
 * - **Mobility:** International mobility and Erasmus programs.
 * - **Collaborations:** Student 150-hour collaborations.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // View scholarship calls
 * val scholarships = admissionApi.getScholarships()
 *
 * // View mobility options
 * val mobility = admissionApi.getMobilityMenu()
 * ```
 */
interface Esse3AdmissionApi {

    /**
     * Retrieves the general Admission Board.
     *
     * @param testTypeCode Filter by test type.
     * @return A [Response] containing the HTML board.
     */
    @GET("auth/studente/Admission/Bacheca.do")
    suspend fun getAdmissionBoard(@Query("tipoTestCod") testTypeCode: String? = null): Response<String>

    /**
     * Retrieves the Admission Notice Board.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML board.
     */
    @GET("auth/studente/Admission/BachecaAmmissione.do")
    suspend fun getAdmissionNoticeBoard(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the National Admission Board.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML board.
     */
    @GET("auth/studente/Admission/BachecaNazionali.do")
    suspend fun getNationalAdmissionBoard(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the Evaluation Board.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML board.
     */
    @GET("auth/studente/Admission/BachecaValutazione.do")
    suspend fun getEvaluationBoard(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Prints the admission/competition slip (Domanda di ammissione).
     *
     * @param testId Test ID.
     * @param invoiceId Invoice ID associated.
     * @param posId Position ID.
     * @param year Academic year.
     * @param testTypeCode Test type code.
     * @param courseTypeCode Course type code.
     * @param profCode Profile code.
     * @param specTypeCode Specialization code.
     * @param posTypeCode Position type code.
     * @return A [Response] containing the PDF [ResponseBody].
     */
    @GET("auth/studente/Admission/ConcStampa.do")
    suspend fun printAdmissionSlip(
        @Query("test_id") testId: String? = null,
        @Query("FATT_ID") invoiceId: String? = null,
        @Query("pos_id") posId: String? = null,
        @Query("aa_id") year: String? = null,
        @Query("tipoTestCod") testTypeCode: String? = null,
        @Query("TIPO_CORSO_COD") courseTypeCode: String? = null,
        @Query("AB_PROF_COD") profCode: String? = null,
        @Query("tipo_spec_cod") specTypeCode: String? = null,
        @Query("tipo_pos_cod") posTypeCode: String? = null
    ): Response<ResponseBody>

    /**
     * Retrieves enrollment details for a specific admission test.
     *
     * @param testTypeCode Test type code.
     * @param posId Position ID.
     * @return A [Response] containing the HTML details.
     */
    @GET("auth/studente/Admission/DettagliIscrizione.do")
    suspend fun getEnrollmentDetails(
        @Query("tipoTestCod") testTypeCode: String? = null,
        @Query("POS_ID") posId: String? = null
    ): Response<String>

    /**
     * Navigates to fill a competition questionnaire.
     *
     * @param questionnaireId Questionnaire ID.
     * @param posId Position ID.
     * @param competitionQuestId Competition Questionnaire ID.
     * @param pageRedirect Redirect URL.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/studente/Admission/CompilaQuestionarioConcorsi.do")
    suspend fun fillCompetitionQuestionnaire(
        @Query("questionario_id") questionnaireId: String? = null,
        @Query("pos_id") posId: String? = null,
        @Query("concorsi_quest_id") competitionQuestId: String? = null,
        @Query("pageRedirect") pageRedirect: String? = null
    ): Response<Unit>

    /**
     * Retrieves the Pre-Competition Questionnaires page.
     *
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/Admission/ConcQuestionariPre.do")
    suspend fun getPreCompetitionQuestionnaires(): Response<String>

    /**
     * Confirms the choice of a competition.
     *
     * @param formId Form identifier, defaults to "formPrincipale".
     * @param btnSubmit Button action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/studente/Admission/ConfermaSceltaConcorsoSubmit.do")
    suspend fun confirmCompetitionChoice(
        @Field("form_id_formPrincipale") formId: String? = "formPrincipale",
        @Field("btnSubmit") btnSubmit: String? = null
    ): Response<Unit>

     /**
     * Confirms the admission application submission.
     *
     * @param eventCode Event code.
     * @param courseTypeCode Course type code (from Dataset).
     * @param formId Form identifier, defaults to "formPrincipale".
     * @param btnSubmit Button action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/studente/Admission/ConfermaSubmit.do")
    suspend fun confirmAdmissionSubmit(
        @Field("EVENTO_COMP_COD") eventCode: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='CONC_SCELTA']/Row[@Num='1']/tipo_corso_cod") courseTypeCode: String? = null,
        @Field("form_id_formPrincipale") formId: String? = "formPrincipale",
        @Field("btnSubmit") btnSubmit: String? = null
    ): Response<Unit>

    /**
     * Retrieves the detail page for Evaluation Titles.
     *
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/studente/Admission/DettaglioTitoliValutazione.do")
    suspend fun getEvaluationTitlesDetail(): Response<String>

    /**
     * Submits the Evaluation Titles detail form.
     *
     * @param attachDoc Attach document flag.
     * @param formId Form identifier, defaults to "formPrincipale".
     * @param btnSubmit Button action.
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("auth/studente/Admission/DettaglioTitoliValutazioneSubmit.do")
    suspend fun submitEvaluationTitlesDetail(
        @Field("allega_doc") attachDoc: String? = null,
        @Field("form_id_formPrincipale") formId: String? = "formPrincipale",
        @Field("btnSubmit") btnSubmit: String? = null
    ): Response<String>

    /**
     * Retrieves the form for declaring an evaluation title.
     *
     * @param id Title ID.
     * @param isDelete Delete flag.
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/studente/Admission/DichiarazioneTitoloValutazione.do")
    suspend fun getEvaluationTitleDeclaration(
        @Query("id") id: String? = null,
        @Query("is_delete") isDelete: String? = null
    ): Response<String>

    /**
     * Submits an evaluation title declaration.
     *
     * @param typeCode Title type code.
     * @param description Description.
     * @param note Notes.
     * @param titleId Title ID.
     * @param id Record ID.
     * @param titleIdParam Title ID parameter.
     * @param attachmentChunk Chunk for attachment.
     * @param extensions File extensions.
     * @param attachmentId Attachment ID.
     * @param formId Form identifier, defaults to "formPrincipale".
     * @param refresh Refresh parameter.
     * @param btnSubmit Button action.
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("auth/studente/Admission/DichiarazioneTitoloValutazioneSubmit.do")
    suspend fun submitEvaluationTitleDeclaration(
        @Field("/WS/DataSet[@LocalEntityName='TIT_RIC_PRE_WEB']/Row[@Num='1']/tipo_tit_ric_cod") typeCode: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_RIC_PRE_WEB']/Row[@Num='1']/des") description: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_RIC_PRE_WEB']/Row[@Num='1']/nota") note: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_RIC_PRE_WEB']/Row[@Num='1']/tit_ric_id") titleId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='TIT_RIC_PRE_WEB']/Row[@Num='1']/id") id: String? = null,
        @Field("tit_ric_id") titleIdParam: String? = null,
        @Field("allegato_chunk") attachmentChunk: String? = null,
        @Field("estensioni") extensions: String? = null,
        @Field("allegato_id") attachmentId: String? = null,
        @Field("form_id_formPrincipale") formId: String? = "formPrincipale",
        @Field("_fw_refresh-form.x") refresh: String? = null,
        @Field("btnSubmit") btnSubmit: String? = null
    ): Response<String>

    /**
     * Deletes an evaluation title.
     *
     * @param titleId Title ID.
     * @param formId Form identifier, defaults to "form1".
     * @param btnOk Confirmation button.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/studente/Admission/TitoloValutazioneDelete.do")
    suspend fun deleteEvaluationTitle(
        @Field("tit_ric_id") titleId: String? = null,
        @Field("form_id_form1") formId: String? = "form1",
        @Field("btnOk") btnOk: String? = null
    ): Response<Unit>

    /**
     * Retrieves the list of available scholarships (Borse di Studio).
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/dirittoAlloStudio/ListaBorseStudio.do")
    suspend fun getScholarships(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the list of Student Collaborations (150 hours).
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/dirittoAlloStudio/CollaborazioneStudenti.do")
    suspend fun getStudentCollaborations(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the list of generic initiatives/calls.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/dirittoAlloStudio/ListaBandiGenerici.do")
    suspend fun getGenericCalls(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>
    
    /**
     * Retrieves the list of ADECA (Diritto allo Studio) calls.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/dirittoAlloStudio/ListaBandiADECA.do")
    suspend fun getAdecaCalls(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the detail of a specific ADECA call.
     *
     * @param bandId The call/band ID.
     * @return A [Response] containing the HTML detail.
     */
    @GET("auth/studente/dirittoAlloStudio/DettaglioADECA.do")
    suspend fun getAdecaDetail(@Query("BANDO_ID") bandId: String): Response<String>
    
    /**
     * Retrieves the enrollment mask for an ADECA call.
     *
     * @param bandId The call/band ID.
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/studente/dirittoAlloStudio/MaskIscrizioneADECA.do")
    suspend fun getAdecaEnrollmentMask(@Query("BANDO_ID") bandId: String): Response<String>

    /**
     * Retrieves the International Mobility menu.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML menu.
     */
    @GET("auth/studente/dirittoAlloStudio/MobilitaFromMenu.do")
    suspend fun getMobilityMenu(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Submits a mobility scope selection.
     *
     * @param ambit Ambit code.
     * @param searchStatus Status filter.
     * @param formId Form identifier, defaults to "ds-formSelezionaAmbito".
     * @param refresh Refresh parameter.
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("auth/studente/dirittoAlloStudio/Mobilita.do")
    suspend fun submitMobility(
        @Field("AMB") ambit: String? = null,
        @Field("statoRicerca") searchStatus: String? = null,
        @Field("form_id_ds-formSelezionaAmbito") formId: String? = "ds-formSelezionaAmbito",
        @Field("_fw_refresh-form.x") refresh: String? = null
    ): Response<String>
}