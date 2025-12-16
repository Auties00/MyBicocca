package it.attendance100.mybicocca.data.remote.api.esse3

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # Esse3 Questionnaire API
 *
 * Manages the filling and submission of Questionnaires, including Didactic Evaluation
 * (Valutazione della Didattica) and other generic questionnaires.
 *
 * ## Key Features
 *
 * - **Didactic Evaluation:** Mandatory questionnaires for exams.
 * - **Generic:** Optional surveys.
 * - **Engine:** Supports the complex, multi-page questionnaire engine of Esse3.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Start filling a didactic questionnaire
 * val wrapper = questionnaireApi.getDidacticQuestionnaireWrapper(
 *     eventCode = "EV_VAL_DID",
 *     adsceId = "123456"
 * )
 *
 * // Navigate to next page
 * questionnaireApi.submitQuestionnairePage(
 *     questionnaireId = "99",
 *     pageId = "101",
 *     answers = mapOf("DOM_1" to "A")
 * )
 * ```
 */
interface Esse3QuestionnaireApi {

    /**
     * Retrieves the wrapper page for Generic Questionnaires.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/questionari/QuestionarioGenWrapper.do")
    suspend fun getGenericQuestionnaireWrapper(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the list of Didactic Evaluation questionnaires for the booklet.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/QuestAdLibrettoValDid.do")
    suspend fun getDidacticEvaluationQuestionnaires(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the wrapper for a specific didactic questionnaire.
     *
     * @param eventCode Event code (e.g., "EV_VAL_DID").
     * @param adsceId Student Career Activity ID.
     * @return A [Response] containing the HTML wrapper.
     */
    @GET("auth/questionari/QuestionariWrapperAdLibrettoValDid.do")
    suspend fun getDidacticQuestionnaireWrapper(
        @Query("evento_comp_cod") eventCode: String? = null,
        @Query("adsce_id") adsceId: String? = null
    ): Response<String>

    /**
     * Retrieves the New Questionnaire Wrapper page.
     *
     * Initializes the questionnaire session.
     *
     * @param eventCode Event code.
     * @param questionnaireId Questionnaire ID.
     * @param configId Configuration ID.
     * @param adsceId Student Career Activity ID.
     * @param offYear Offer Year.
     * @param ordYear Ordinance Year.
     * @param activityId Activity ID.
     * @param courseId Course ID.
     * @param docId Document ID.
     * @param partCode Partition Code.
     * @param domPartCode Dom Partition Code.
     * @param fatPartCode Fat Partition Code.
     * @param planId Plan ID.
     * @param udId UD ID.
     * @param creditType Credit type.
     * @param buttonMsgType Button message type.
     * @param courseCompCode Course component code.
     * @param skipList Skip list page flag.
     * @return A [Response] containing the HTML page.
     */
    @GET("questionari/QuestionariWrapperNew.do")
    suspend fun getQuestionnaireWrapper(
        @Query("evento_comp_cod") eventCode: String? = null,
        @Query("quest_id") questionnaireId: String? = null,
        @Query("quest_config_id") configId: String? = null,
        @Query("adsce_id") adsceId: String? = null,
        @Query("AA_OFF_AD_ID_VAL") offYear: String? = null,
        @Query("AA_ORD_AD_ID_VAL") ordYear: String? = null,
        @Query("AD_ID_VAL") activityId: String? = null,
        @Query("CDS_AD_ID_VAL") courseId: String? = null,
        @Query("DOC_AD_ID_VAL") docId: String? = null,
        @Query("PART_AD_COD_VAL") partCode: String? = null,
        @Query("DOM_PART_AD_COD_VAL") domPartCode: String? = null,
        @Query("FAT_PART_AD_COD_VAL") fatPartCode: String? = null,
        @Query("PDS_AD_ID_VAL") planId: String? = null,
        @Query("UD_ID_VAL") udId: String? = null,
        @Query("TIPO_CRE_AD_COD_VAL") creditType: String? = null,
        @Query("button_msg_type") buttonMsgType: String? = null,
        @Query("CDS_COD_COMP") courseCompCode: String? = null,
        @Query("skip_list_page_flg") skipList: String? = null
    ): Response<String>

    /**
     * Submits the initial action to compile a questionnaire.
     *
     * @param eventCode Event code.
     * @param skipList Skip list flag.
     * @param buttonMsgType Button message type.
     * @param questionnaireId Questionnaire ID.
     * @param configId Config ID.
     * @param expiredFlag Expired flag.
     * @param anonymousFlag Anonymous flag.
     * @param offYear Offer year.
     * @param courseId Course ID.
     * @param ordYear Ordinance year.
     * @param planId Plan ID.
     * @param activityId Activity ID.
     * @param courseCompCode Course component code.
     * @param udId UD ID.
     * @param fatPartCode Fat partition code.
     * @param domPartCode Dom partition code.
     * @param partCode Partition code.
     * @param docId Doc ID.
     * @param creditType Credit type.
     * @param tagDataQuery Tag data query string.
     * @param tagData Tag data.
     * @param pageRedirect Redirect URL.
     * @param adsceId Adsce ID.
     * @param formId Form identifier, defaults to "quest_form_compilazioni1".
     * @param btnNewQuest Button "New Quest" action.
     * @param btnExit Button "Exit" action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("questionari/QuestionariWrapperCompilaNew.do")
    suspend fun compileQuestionnaire(
        @Field("evento_comp_cod") eventCode: String? = null,
        @Field("skip_list_page_flg") skipList: String? = null,
        @Field("button_msg_type") buttonMsgType: String? = null,
        @Field("quest_id") questionnaireId: String? = null,
        @Field("quest_config_id") configId: String? = null,
        @Field("scaduto_flg") expiredFlag: String? = null,
        @Field("anonimo_flg") anonymousFlag: String? = null,
        @Field("AA_OFF_AD_ID_VAL") offYear: String? = null,
        @Field("CDS_AD_ID_VAL") courseId: String? = null,
        @Field("AA_ORD_AD_ID_VAL") ordYear: String? = null,
        @Field("PDS_AD_ID_VAL") planId: String? = null,
        @Field("AD_ID_VAL") activityId: String? = null,
        @Field("CDS_COD_COMP") courseCompCode: String? = null,
        @Field("UD_ID_VAL") udId: String? = null,
        @Field("FAT_PART_AD_COD_VAL") fatPartCode: String? = null,
        @Field("DOM_PART_AD_COD_VAL") domPartCode: String? = null,
        @Field("PART_AD_COD_VAL") partCode: String? = null,
        @Field("DOC_AD_ID_VAL") docId: String? = null,
        @Field("TIPO_CRE_AD_COD_VAL") creditType: String? = null,
        @Field("lista_tag_dati_querystring") tagDataQuery: String? = null,
        @Field("lista_tag_dati") tagData: String? = null,
        @Field("page_redirect") pageRedirect: String? = null,
        @Field("adsce_id") adsceId: String? = null,
        @Field("form_id_quest_form_compilazioni1") formId: String? = "quest_form_compilazioni1",
        @Field("sbmNewQuest") btnNewQuest: String? = null,
        @Field("sbmEsci") btnExit: String? = null
    ): Response<Unit>

    /**
     * Retrieves a specific page of the questionnaire.
     *
     * @param pageId Page ID.
     * @param questCompId Questionnaire Compilation ID.
     * @param pageRedirect Redirect URL.
     * @param questionnaireId Questionnaire ID.
     * @param noEscapeFlag No escape flag.
     * @param eventCode Event code.
     * @param userCompId User Compilation ID.
     * @param adsceId Adsce ID.
     * @param configId Config ID.
     * @return A [Response] containing the HTML page.
     */
    @GET("questionari/QuestionariPaginaNew.do")
    suspend fun getQuestionnairePage(
        @Query("p_pagina_id") pageId: String? = null,
        @Query("p_quest_comp_id") questCompId: String? = null,
        @Query("page_redirect") pageRedirect: String? = null,
        @Query("p_quest_id") questionnaireId: String? = null,
        @Query("noEscapeFlg") noEscapeFlag: String? = null,
        @Query("p_evento_comp_cod") eventCode: String? = null,
        @Query("p_user_comp_id") userCompId: String? = null,
        @Query("adsce_id") adsceId: String? = null,
        @Query("p_quest_config_id") configId: String? = null
    ): Response<String>

    /**
     * Submits the answers for a questionnaire page.
     *
     * @param questionnaireId Questionnaire ID.
     * @param questCompId Questionnaire Compilation ID.
     * @param pageId Page ID.
     * @param userCompId User Compilation ID.
     * @param configId Config ID.
     * @param noEscapeFlag No escape flag.
     * @param mandatoryList List of mandatory questions.
     * @param pageRedirect Redirect URL.
     * @param eventCode Event code.
     * @param concYearId Concurrent Year ID.
     * @param concTestId Concurrent Test ID.
     * @param formId Form identifier, defaults to "quest_form_questionario_pagina".
     * @param btnNext Button "Next" action.
     * @param btnPrev Button "Previous" action.
     * @param answers Dynamic map of question answers.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("questionari/QuestionariPaginaSubmitNew.do")
    suspend fun submitQuestionnairePage(
        @Field("p_quest_id") questionnaireId: String? = null,
        @Field("p_quest_comp_id") questCompId: String? = null,
        @Field("p_pagina_id") pageId: String? = null,
        @Field("p_user_comp_id") userCompId: String? = null,
        @Field("p_quest_config_id") configId: String? = null,
        @Field("noEscapeFlg") noEscapeFlag: String? = null,
        @Field("lista_obbligatorie") mandatoryList: String? = null,
        @Field("page_redirect") pageRedirect: String? = null,
        @Field("p_evento_comp_cod") eventCode: String? = null,
        @Field("conc_aa_id") concYearId: String? = null,
        @Field("conc_test_id") concTestId: String? = null,
        @Field("form_id_quest_form_questionario_pagina") formId: String? = "quest_form_questionario_pagina",
        @Field("sbmSuccessivo") btnNext: String? = null,
        @Field("sbmPrecedente") btnPrev: String? = null,
        @FieldMap answers: Map<String, String> = emptyMap()
    ): Response<Unit>

    /**
     * Retrieves the summary page of a completed questionnaire.
     *
     * @param noEscapeFlag No escape flag.
     * @param pageId Page ID.
     * @param questCompId Questionnaire Compilation ID.
     * @param pageRedirect Redirect URL.
     * @param questionnaireId Questionnaire ID.
     * @param eventCode Event code.
     * @param expiredFlag Expired flag.
     * @param activityId Activity ID.
     * @param adsceId Adsce ID.
     * @param partCode Partition code.
     * @param docId Doc ID.
     * @param courseId Course ID.
     * @param courseCompCode Course component code.
     * @param domPartCode Dom partition code.
     * @param userCompId User compilation ID.
     * @param udId UD ID.
     * @param creditType Credit type.
     * @param configId Config ID.
     * @param fatPartCode Fat partition code.
     * @param ordYear Ordinance year.
     * @param planId Plan ID.
     * @param offYear Offer year.
     * @return A [Response] containing the HTML summary.
     */
    @GET("questionari/QuestionariRiepilogo.do")
    suspend fun getQuestionnaireSummary(
        @Query("noEscapeFlg") noEscapeFlag: String? = null,
        @Query("p_pagina_id") pageId: String? = null,
        @Query("p_quest_comp_id") questCompId: String? = null,
        @Query("page_redirect") pageRedirect: String? = null,
        @Query("p_quest_id") questionnaireId: String? = null,
        @Query("p_evento_comp_cod") eventCode: String? = null,
        @Query("scaduto_flg") expiredFlag: String? = null,
        @Query("AD_ID_VAL") activityId: String? = null,
        @Query("adsce_id") adsceId: String? = null,
        @Query("PART_AD_COD_VAL") partCode: String? = null,
        @Query("DOC_AD_ID_VAL") docId: String? = null,
        @Query("CDS_AD_ID_VAL") courseId: String? = null,
        @Query("CDS_COD_COMP") courseCompCode: String? = null,
        @Query("DOM_PART_AD_COD_VAL") domPartCode: String? = null,
        @Query("p_user_comp_id") userCompId: String? = null,
        @Query("UD_ID_VAL") udId: String? = null,
        @Query("TIPO_CRE_AD_COD_VAL") creditType: String? = null,
        @Query("p_quest_config_id") configId: String? = null,
        @Query("FAT_PART_AD_COD_VAL") fatPartCode: String? = null,
        @Query("AA_ORD_AD_ID_VAL") ordYear: String? = null,
        @Query("PDS_AD_ID_VAL") planId: String? = null,
        @Query("AA_OFF_AD_ID_VAL") offYear: String? = null
    ): Response<String>

    /**
     * Submits the confirmation of the questionnaire summary.
     *
     * @param questionnaireId Questionnaire ID.
     * @param questCompId Compilation ID.
     * @param pageId Page ID.
     * @param userCompId User Compilation ID.
     * @param configId Config ID.
     * @param noEscapeFlag No escape flag.
     * @param pageRedirect Redirect URL.
     * @param eventCode Event code.
     * @param adsceId Adsce ID.
     * @param formIdConfirm Confirm form identifier, defaults to "quest_form_modifica_paginaNA000F".
     * @param btnConfirm Confirm button action.
     * @param formIdExit Exit form identifier, defaults to "quest_form_modifica_paginaNA000D".
     * @param btnExit Exit button action.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("questionari/QuestionariRiepilogoSubmitNew.do")
    suspend fun submitQuestionnaireSummary(
        @Field("p_quest_id") questionnaireId: String? = null,
        @Field("p_quest_comp_id") questCompId: String? = null,
        @Field("p_pagina_id") pageId: String? = null,
        @Field("p_user_comp_id") userCompId: String? = null,
        @Field("p_quest_config_id") configId: String? = null,
        @Field("noEscapeFlg") noEscapeFlag: String? = null,
        @Field("page_redirect") pageRedirect: String? = null,
        @Field("p_evento_comp_cod") eventCode: String? = null,
        @Field("adsce_id") adsceId: String? = null,
        @Field("form_id_quest_form_modifica_paginaNA000F") formIdConfirm: String? = null,
        @Field("sbmConferma") btnConfirm: String? = null,
        @Field("form_id_quest_form_modifica_paginaNA000D") formIdExit: String? = null,
        @Field("sbmEsci") btnExit: String? = null
    ): Response<Unit>
}