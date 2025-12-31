package it.attendance100.mybicocca.data.api.esse3

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/**
 * # Esse3 User API
 *
 * Provides access to the student's personal profile, address book, identity documents,
 * and related administrative processes.
 *
 * ## Key Features
 *
 * - **Personal Data:** View and update personal information (Anagrafica).
 * - **Address Book:** Manage residence and domicile addresses.
 * - **Contacts:** Update phone numbers and email addresses.
 * - **Documents:** Upload and manage identity documents.
 * - **Privacy:** Manage privacy consents.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Load personal data page
 * val profile = userApi.getPersonalData()
 *
 * // Update contact info
 * userApi.submitContactDetails(
 *     mobile = "3331234567",
 *     email = "student@example.com"
 * )
 * ```
 */
interface Esse3UserApi {

    /**
     * Retrieves the Student Home Page.
     *
     * This is the main dashboard for the authenticated student, containing
     * summaries of their academic status.
     *
     * @return A [Response] containing the HTML of the home page.
     */
    @GET("auth/studente/HomePageStudente.do")
    suspend fun getStudentHome(): Response<String>

    /**
     * Accesses the Personal Data (Anagrafica) page.
     *
     * Displays the student's personal details such as name, birth date,
     * and tax code.
     *
     * @param menuOpenedCod Optional menu code context.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/studente/Anagrafica/Anagrafica.do")
    suspend fun getPersonalData(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Handles the "Loop Anagrafica" redirect.
     *
     * Used internally by Esse3 to manage navigation flows within the
     * personal data section.
     *
     * @return A [Response] containing the HTML content.
     */
    @GET("auth/AddressBook/LoopAnagrafica.do")
    suspend fun getPersonalDataLoop(): Response<String>

    /**
     * Starts the Personal Data editing process.
     *
     * Initiates the workflow to modify personal information.
     *
     * @param menuOpenedCod Optional menu code context.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/AddressBook/ABInizioAnagraficaInProcesso.do")
    suspend fun startPersonalDataEdit(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<Unit>

    /**
     * Completes the Personal Data editing process.
     *
     * Finalizes the changes made during the editing session.
     *
     * @param formId The form identifier, typically "summaryForm".
     * @param proceed The action parameter (e.g., to confirm).
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/EndAnagrafica.do")
    suspend fun endPersonalDataEdit(
        @Field("form_id_summaryForm") formId: String? = "summaryForm",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Retrieves the pre-form message for address editing.
     *
     * Often displays instructions or warnings before modifying addresses.
     *
     * @return A [Response] containing the HTML content.
     */
    @GET("auth/AddressBook/ABMsgAnaPreForm.do")
    suspend fun getAddressMessagePreForm(): Response<String>

    /**
     * Retrieves the form for editing residence/domicile addresses.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/AddressBook/ABSubWizIndirizziResForm.do")
    suspend fun getResidenceAddressForm(): Response<String>

    /**
     * Submits changes to the residence or domicile address.
     *
     * @param nationId ID of the nation (e.g., for Italy).
     * @param provinceId Province abbreviation (e.g., "MI").
     * @param municipalityId ID of the municipality.
     * @param zipCode Postal code.
     * @param hamlet Hamlet/Locality (Frazione).
     * @param street Street name.
     * @param streetNum Street number.
     * @param phone Landline phone number.
     * @param domicileSameAsResidence Flag ("1" if true) to set domicile equal to residence.
     * @param formId The form identifier, defaults to "formResDom".
     * @param proceed Action parameter to proceed.
     * @return A [Response] containing the HTML response (next step or confirmation).
     */
    @FormUrlEncoded
    @POST("AddressBook/IndirizziSubmit.do")
    suspend fun submitAddress(
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/naz_res_id") nationId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/p01_comu_comu_res_sigla") provinceId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/com_res_id") municipalityId: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cap_res") zipCode: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fraz_res") hamlet: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/via_res") street: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/num_civ_res") streetNum: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tel_res") phone: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/dom_come_res_flg") domicileSameAsResidence: String? = null,
        @Field("form_id_formResDom") formId: String? = "formResDom",
        @Field("procedi") proceed: String? = null
    ): Response<String>

    /**
     * Retrieves the form for editing contact details.
     *
     * Allows modification of email, mobile phone, etc.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/AddressBook/ABSubWizRecapitoForm.do")
    suspend fun getContactDetailsForm(): Response<String>

    /**
     * Submits changes to contact details.
     *
     * @param addressType Type of address code.
     * @param taxAddress Tax address.
     * @param email Email address.
     * @param fax Fax number.
     * @param mobile Mobile phone number.
     * @param mobilePrefix International prefix for mobile.
     * @param mobilePrefixTxt Text description of mobile prefix.
     * @param consentFlg Consent flag (e.g., "CONS_675_FLG").
     * @param formId The form identifier, defaults to "formRecapito".
     * @param proceed Action parameter to proceed.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("AddressBook/RecapitoSubmit.do")
    suspend fun submitContactDetails(
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tipo_indiriz_cod") addressType: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/recapito_tasse") taxAddress: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/email") email: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fax") fax: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cellulare") mobile: String? = null,
        @Field("INTL_PREFIX_CELLULARE") mobilePrefix: String? = null,
        @Field("INTL_PREFIX_TXT_CELLULARE") mobilePrefixTxt: String? = null,
        @Field("CONS_675_FLG") consentFlg: String? = null,
        @Field("form_id_formRecapito") formId: String? = "formRecapito",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Retrieves the list of registered identity documents.
     *
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/AddressBook/ABDocIdentitaElenco.do")
    suspend fun getIdentityDocuments(): Response<String>

    /**
     * Submits an action from the identity documents list.
     *
     * @param formId The form identifier, defaults to "formProsegui".
     * @param proceed Action parameter.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaElencoSubmit.do")
    suspend fun submitIdentityDocumentsList(
        @Field("form_id_formProsegui") formId: String? = "formProsegui",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Starts the process to insert a new identity document.
     *
     * @param formId The form identifier, defaults to "formInsDocIdent".
     * @param btnInsert Button parameter to trigger insertion.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaStartInsertInProc.do")
    suspend fun startIdentityDocumentInsertion(
        @Field("form_id_formInsDocIdent") formId: String? = "formInsDocIdent",
        @Field("btnInsert") btnInsert: String? = null
    ): Response<Unit>

    /**
     * Retrieves the form for entering new identity document details.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/AddressBook/ABDocIdentitaFormInsert.do")
    suspend fun getIdentityDocumentInsertForm(): Response<String>

    /**
     * Submits the details of a new identity document.
     *
     * @param type Document type code (e.g., "CI" for Carta d'Identità).
     * @param number Document number.
     * @param issuer Issuing authority.
     * @param issueDate Date of issue.
     * @param expiryDate Expiry date.
     * @param docId Internal document ID (if updating).
     * @param formId The form identifier, defaults to "formDocIdent".
     * @param proceed Action parameter to proceed.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaSubmitInsert.do")
    suspend fun submitIdentityDocument(
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/doc_ident_tipo_cod") type: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/num") number: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/ente_rilascio") issuer: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/data_rilascio") issueDate: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/data_scadenza") expiryDate: String? = null,
        @Field("/WS/DataSet[@LocalEntityName='ANAPER_DOC_IDENTITA']/Row[@Num='1']/doc_pers_id") docId: String? = null,
        @Field("form_id_formDocIdent") formId: String? = "formDocIdent",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Retrieves the confirmation page for identity document insertion.
     *
     * @return A [Response] containing the HTML confirmation page.
     */
    @GET("auth/AddressBook/ABDocIdentitaConferma.do")
    suspend fun getIdentityDocumentConfirmation(): Response<String>

    /**
     * Confirms the insertion of the identity document.
     *
     * @param formId The form identifier, defaults to "formProsegui".
     * @param proceed Action parameter to proceed.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaConfermaInsertSubmit.do")
    suspend fun submitIdentityDocumentConfirmation(
        @Field("form_id_formProsegui") formId: String? = "formProsegui",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Finalizes the identity document insertion wizard.
     *
     * @return A [Response] containing [Unit].
     */
    @GET("auth/AddressBook/ABDocIdentitaEndInsertWiz.do")
    suspend fun endIdentityDocumentWizard(): Response<Unit>

    /**
     * Retrieves the page listing attachments for identity documents.
     *
     * @return A [Response] containing the HTML list of attachments.
     */
    @GET("auth/AddressBook/ABDocIdentitaElAllegatiInsert.do")
    suspend fun getIdentityDocumentAttachments(): Response<String>

    /**
     * Starts the wizard to upload an attachment for an identity document.
     *
     * @param formId The form identifier, defaults to "formInsAllegati".
     * @param btnInsert Button parameter to trigger upload wizard.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaAllegStartWiz.do")
    suspend fun startAttachmentWizard(
        @Field("form_id_formInsAllegati") formId: String? = "formInsAllegati",
        @Field("btnInsert") btnInsert: String? = null
    ): Response<Unit>

    /**
     * Retrieves the form for uploading a document attachment.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/AddressBook/ABDocIdentitaAllegForm.do")
    suspend fun getAttachmentForm(): Response<String>

    /**
     * Submits the uploaded attachment.
     *
     * Note: This usually follows a chunked upload process handled by `Esse3CommonApi`.
     *
     * @return A [Response] containing [Unit].
     */
    @POST("auth/AddressBook/ABDocIdentitaAllegSubmit.do")
    suspend fun submitAttachment(): Response<Unit>

    /**
     * Downloads a specific identity document attachment.
     *
     * @param attachmentId The ID of the attachment to download.
     * @return A [Response] containing [Unit] (or the file stream).
     */
    @GET("auth/AddressBook/ABDocIdentitaDownloadAllegato.do")
    suspend fun downloadAttachment(@Query("allegato_id") attachmentId: String): Response<Unit>

    /**
     * Prepares to delete an identity document attachment.
     *
     * @param attachmentId The ID of the attachment to delete.
     * @param isDelete Flag ("1") to indicate deletion intent.
     * @return A [Response] containing the HTML confirmation page.
     */
    @GET("auth/AddressBook/ABDocIdentitaAllegElimina.do")
    suspend fun deleteAttachmentPre(
        @Query("allegato_id") attachmentId: String,
        @Query("IS_DELETE") isDelete: String = "1"
    ): Response<String>

    /**
     * Confirms the deletion of an identity document attachment.
     *
     * @param formId The form identifier, defaults to "formDel".
     * @param proceed Action parameter to proceed.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABDocIdentitaAllegEliminaSubmit.do")
    suspend fun submitDeleteAttachment(
        @Field("form_id_formDel") formId: String? = "formDel",
        @Field("procedi") proceed: String? = null
    ): Response<Unit>

    /**
     * Starts the privacy policy management process.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/AddressBook/ABPrivacyPolicyStartProcess.do")
    suspend fun startPrivacyPolicyProcess(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Triggers a specific privacy policy action.
     *
     * @param process Process code (e.g., "PR_POLICY").
     * @param processId ID of the process instance.
     * @param formId Form identifier.
     * @param btnProceed Button action.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/AddressBook/ABPrivacyPolicyStartAction.do")
    suspend fun startPrivacyPolicyAction(
        @Query("gp_processo") process: String? = null,
        @Query("gp_id_processo") processId: String? = null,
        @Query("form_id_form_processo") formId: String? = null,
        @Query("btnProcedi") btnProceed: String? = null
    ): Response<Unit>

    /**
     * Retrieves the privacy policy pre-message.
     *
     * @return A [Response] containing [Unit].
     */
    @GET("auth/AddressBook/ABPrivacyPolicyMsgPre.do")
    suspend fun getPrivacyPolicyMsgPre(): Response<Unit>

    /**
     * Retrieves the privacy consent form.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/AddressBook/ABPrivacyPolicyConsensiForm.do")
    suspend fun getPrivacyPolicyConsentForm(): Response<String>

    /**
     * Submits privacy consent choices.
     *
     * @param masterAidConsent Consent flag for master benefits/aid (e.g., "1" or "0").
     * @param forward Forward navigation parameter.
     * @param formId Form identifier, defaults to "ab_privacy_policy_form".
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/AddressBook/ABPrivacyPolicyConsensiSubmit.do")
    suspend fun submitPrivacyPolicyConsent(
        @Field("/WS/DataSet[@LocalEntityName='P01_ANAPER_CONSENSI_WEB_PROC']/Row[tipo_consenso_cod='AGEV_MASTER']/consenso_flg") masterAidConsent: String? = null,
        @Field("avanti") forward: String? = null,
        @Field("form_id_ab_privacy_policy_form") formId: String? = "ab_privacy_policy_form"
    ): Response<Unit>

    /**
     * Retrieves the privacy policy post-submission message.
     *
     * @return A [Response] containing [Unit].
     */
    @GET("auth/AddressBook/ABPrivacyPolicyMsgPost.do")
    suspend fun getPrivacyPolicyMsgPost(): Response<Unit>

    /**
     * Starts the photo management process.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML page.
     */
    @GET("auth/AddressBook/ABProcessoFotoStart.do")
    suspend fun startPhotoProcess(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Downloads the student's profile photo.
     *
     * @param random Random parameter to bypass cache.
     * @return A [Response] containing [Unit] (the image data).
     */
    @GET("auth/AddressBook/DownloadFoto.do")
    suspend fun downloadPhoto(@Query("r") random: String? = null): Response<Unit>
}
