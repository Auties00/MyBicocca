package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AnnualEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AuthorizationAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AuthorizedPerson
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BankDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CanteenBandParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Career
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerClosureParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerGDPR
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerMinimalData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompensatoryMeasureFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompensatoryMeasures
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ConsentsParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentNumberAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentReturn
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExemptionTypeParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExternalSubject
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExternalSubjectReplica
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExternalSubjectsConsents
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignTitleAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignTitleType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignTitleValidationDeclaration
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ForeignUniversity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetAuthorizationAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetEnrollmentNumberAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetForeignTitleAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetHandicapDeclarationAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetHighSchoolDiplomaAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetIdentityDocumentAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetItalianTitleAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GetPersonalDocumentAuthorizationMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GraduationWaitingParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HandicapDeclaration
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HandicapDeclarationAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HandicapDeclarationPut
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HandicapRegulations
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HandicapTypesLookup
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HighSchoolDiplomaAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HighSchoolGradeRange
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HigherInstituteTypes
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3HigherSchoolTitleType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3IdentityDocumentAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Institute
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ItalianTitleAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MobileParameter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3NewTeachers
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Person
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonCommonRegistry
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonCompensatoryMeasures
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonGDPR
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonPhotoAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonTitles
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonalDocumentAuthorizationMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PhDProgramCareer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PhoneParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostCompensatoryMeasuresHandicapDeclarationParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PutCompensatoryMeasuresHandicapDeclarationParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PutExternalSubject
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RefreshedToken
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RelationshipTypes
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentTypeParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentsConsents
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyCourse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TitlesInsertion
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Tutor
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TutorsRulesHeader
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3University
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ValidationFlag
import kotlinx.serialization.json.Json

class Esse3PersonalDataApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/anagrafica-service-v2") {

    /**
     * refresh della data di scadenza del token dreamapply e recupero dell’url di attivazione
     *
     * @param applicationId id esterno della carriera per dreamapply
     */
    suspend fun refreshToken(
        applicationId: String
    ): Esse3RefreshedToken {
        return executeJsonPut<Esse3RefreshedToken>("/activation-url${applicationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati relativi ad un autorizzato
     *
     * @param authorizedId id dell'autorizzato
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     * @param webVisibleFlag flag che indica se l'allegato risulta visibile da web o meno
     */
    suspend fun getAuthorizedAttachmentMetadata(
        authorizedId: Long,
        attachmentTypology: String? = null,
        validFlag: Int? = null,
        webVisibleFlag: Int? = null
    ): List<Esse3GetAuthorizationAttachmentMetadata> {
        return executeJsonGetList<Esse3GetAuthorizationAttachmentMetadata>("/allegati/autorizzati/${authorizedId}/allegatiAutorizzato/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
            webVisibleFlag?.let { parameter("webVisFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato relativo ad un autorizzato
     *
     * @param authorizedId id dell'autorizzato
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postAuthorizedAttachmentMetadata(
        authorizedId: Long,
        body: Esse3AuthorizationAttachmentMetadata
    ) {
        val response = executePost("/allegati/autorizzati/${authorizedId}/allegatiAutorizzato/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati relativi ai documenti d'identità di un autorizzato
     *
     * @param authorizedId id dell'autorizzato
     * @param identityDocumentTypeCode codice tipo di documento d'identità (CI - Carta Identità, PAT - Patente, PAS - Passaporto)
     * @param personalDataDocAuthorizationId identificativo documento identità autorizzato
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     * @param webVisibleFlag flag che indica se l'allegato risulta visibile da web o meno
     */
    suspend fun getAuthorizedPersonalDocumentAttachmentMetadata(
        authorizedId: Long,
        identityDocumentTypeCode: String,
        personalDataDocAuthorizationId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null,
        webVisibleFlag: Int? = null
    ): List<Esse3GetPersonalDocumentAuthorizationMetadata> {
        return executeJsonGetList<Esse3GetPersonalDocumentAuthorizationMetadata>("/allegati/autorizzati/${authorizedId}/allegatiDocPers/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("docIdentTipoCod", identityDocumentTypeCode)
            personalDataDocAuthorizationId?.let { parameter("autDocPersId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
            webVisibleFlag?.let { parameter("webVisFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato relativo ad un documento d'identità di un autorizzato
     *
     * @param authorizedId id dell'autorizzato
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postAuthorizedPersonalDocumentAttachmentMetadata(
        authorizedId: Long,
        body: Esse3PersonalDocumentAuthorizationMetadata
    ) {
        val response = executePost("/allegati/autorizzati/${authorizedId}/allegatiDocPers/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * cancellazione allegato al tratto di carriera
     *
     * @param matId Identificativo tratto carriera studente
     * @param attachmentId identificativo allegato
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     */
    suspend fun deleteCareerAttachmentMetadata(
        matId: Long,
        attachmentId: Long,
        attachmentTypology: String? = null
    ) {
        val response = executeDelete("/allegati/carriere/${matId}") {
            parameter("allegatoId", attachmentId)
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * cancellazione allegato al documento di identità
     *
     * @param personalDocumentId identificativo documento identità
     * @param attachmentId identificativo allegato
     * @param personId identificativo della persona
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     */
    suspend fun deletePersonalDocumentAttachmentMetadata(
        personalDocumentId: Long,
        attachmentId: Long,
        personId: Long? = null,
        attachmentTypology: String? = null
    ) {
        val response = executeDelete("/allegati/docPers/${personalDocumentId}") {
            parameter("allegatoId", attachmentId)
            personId?.let { parameter("persId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati dichiarazioni handicap
     *
     * @param personId identificativo della persona
     * @param handicapType tipo di handicap
     * @param declarationDate data della dichiarazione
     * @param startDate data di inizio invalidità
     * @param endDate data di fine invalidità
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     * @param webVisibleFlag flag che indica se l'allegato risulta visibile da web o meno
     */
    suspend fun getHandicapDeclarationAttachmentMetadata(
        personId: Long,
        handicapType: String,
        declarationDate: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        handicapDeclarationId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null,
        webVisibleFlag: Int? = null
    ): List<Esse3GetHandicapDeclarationAttachmentMetadata> {
        return executeJsonGetList<Esse3GetHandicapDeclarationAttachmentMetadata>("/allegati/${personId}/allegatiDicHand/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("tipoHandicap", handicapType)
            declarationDate?.let { parameter("dataDichiar", it) }
            startDate?.let { parameter("dataIni", it) }
            endDate?.let { parameter("dataFine", it) }
            handicapDeclarationId?.let { parameter("dicHandId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
            webVisibleFlag?.let { parameter("webVisFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato dichiarazione handicap
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postHandicapDeclarationAttachmentMetadata(
        personId: Long,
        body: Esse3HandicapDeclarationAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiDicHand/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati documento identità
     *
     * @param personId identificativo della persona
     * @param identityDocumentTypeCode codice tipo di documento d'identità (CI - Carta Identità, PAT - Patente, PAS - Passaporto)
     * @param personalDocumentId identificativo documento identità
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     * @param webVisibleFlag flag che indica se l'allegato risulta visibile da web o meno
     */
    suspend fun getIdentityDocumentAttachmentMetadata(
        personId: Long,
        identityDocumentTypeCode: String,
        personalDocumentId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null,
        webVisibleFlag: Int? = null
    ): List<Esse3GetIdentityDocumentAttachmentMetadata> {
        return executeJsonGetList<Esse3GetIdentityDocumentAttachmentMetadata>("/allegati/${personId}/allegatiDocIdent/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("docIdentTipoCod", identityDocumentTypeCode)
            personalDocumentId?.let { parameter("docPersId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
            webVisibleFlag?.let { parameter("webVisFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato documento identità
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postIdentityDocumentAttachmentMetadata(
        personId: Long,
        body: Esse3IdentityDocumentAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiDocIdent/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * inserimento metadati allegato foto della persona
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postPersonPhotoAttachmentMetadata(
        personId: Long,
        body: Esse3PersonPhotoAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiFotoPersona/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati maturità
     *
     * @param personId identificativo della persona
     * @param highSchoolGraduationYear anno di maturità, coincide con l'anno solare della data di conseguimento del diploma. Per esempio, anno scolastico 2019/2020, l'anno di diploma è 2020
     * @param miurDiplomaId identificativo diploma MIUR
     * @param highSchoolGraduationDate data di maturità
     * @param highSchoolGraduationId identificativo della maturità
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     */
    suspend fun getHighSchoolGraduationAttachmentMetadata(
        personId: Long,
        highSchoolGraduationYear: Int,
        miurDiplomaId: Long? = null,
        highSchoolGraduationDate: String? = null,
        highSchoolGraduationId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null
    ): List<Esse3GetHighSchoolDiplomaAttachmentMetadata> {
        return executeJsonGetList<Esse3GetHighSchoolDiplomaAttachmentMetadata>("/allegati/${personId}/allegatiMatur/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("annoMaturita", highSchoolGraduationYear)
            miurDiplomaId?.let { parameter("idDiplomaMiur", it) }
            highSchoolGraduationDate?.let { parameter("dataMaturita", it) }
            highSchoolGraduationId?.let { parameter("maturId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato maturità
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postHighSchoolGraduationAttachmentMetadata(
        personId: Long,
        body: Esse3HighSchoolDiplomaAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiMatur/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati titoli universitari italiani
     *
     * @param personId identificativo della persona
     * @param titleCategoryCode codice tipo titolo italiano
     * @param academicYearAwardedTitle Anno Accademico di conseguimento del titolo
     * @param italianTitleId identificativo del titolo
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     */
    suspend fun getItalianTitleAttachmentMetadata(
        personId: Long,
        titleCategoryCode: String,
        academicYearAwardedTitle: Int? = null,
        italianTitleId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null
    ): List<Esse3GetItalianTitleAttachmentMetadata> {
        return executeJsonGetList<Esse3GetItalianTitleAttachmentMetadata>("/allegati/${personId}/allegatiTitIt/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("tipoTititCod", titleCategoryCode)
            academicYearAwardedTitle?.let { parameter("aaConsegTit", it) }
            italianTitleId?.let { parameter("titItId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato titolo universitario italiano
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postItalianTitleAttachmentMetadata(
        personId: Long,
        body: Esse3ItalianTitleAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiTitIt/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati titoli universitari stranieri
     *
     * @param personId identificativo della persona
     * @param academicYearAwardedTitle Anno Accademico di conseguimento del titolo
     * @param titleStatusTypeCode codice tipo titolo straniero
     * @param foreignTitleId identificativo del titolo straniero
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     */
    suspend fun getForeignTitleAttachmentMetadata(
        personId: Long,
        academicYearAwardedTitle: Int,
        titleStatusTypeCode: String? = null,
        foreignTitleId: Long? = null,
        attachmentTypology: String? = null,
        validFlag: Int? = null
    ): List<Esse3GetForeignTitleAttachmentMetadata> {
        return executeJsonGetList<Esse3GetForeignTitleAttachmentMetadata>("/allegati/${personId}/allegatiTitStra/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("aaConsegTit", academicYearAwardedTitle)
            titleStatusTypeCode?.let { parameter("tipoTitstCod", it) }
            foreignTitleId?.let { parameter("titStraId", it) }
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato titolo universitario straniero
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postForeignTitleAttachmentMetadata(
        personId: Long,
        body: Esse3ForeignTitleAttachmentMetadata
    ) {
        val response = executePost("/allegati/${personId}/allegatiTitStra/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupero metadati allegati matricola
     *
     * @param studentId identificativo della carriera
     * @param attachmentTypology tipologia dell'allegato relativo all'entità p17_tipologia_allegati
     * @param validFlag flag che indica se l'allegato risulta validato o meno
     * @param webVisibleFlag flag che indica se l'allegato risulta visibile da web o meno
     */
    suspend fun getMatricolaAttachmentMetadata(
        studentId: Long,
        attachmentTypology: String? = null,
        validFlag: Int? = null,
        webVisibleFlag: Int? = null
    ): List<Esse3GetEnrollmentNumberAttachmentMetadata> {
        return executeJsonGetList<Esse3GetEnrollmentNumberAttachmentMetadata>("/allegati/${studentId}/allegatiMatricola/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            attachmentTypology?.let { parameter("tipologiaAllegato", it) }
            validFlag?.let { parameter("validoFlg", it) }
            webVisibleFlag?.let { parameter("webVisFlg", it) }
        }
    }

    /**
     * inserimento metadati allegato matricola
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postMatricolaAttachmentMetadata(
        studentId: Long,
        body: Esse3EnrollmentNumberAttachmentMetadata
    ) {
        val response = executePost("/allegati/${studentId}/allegatiMatricola/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero atenei
     *
     * @param istatCode Codice ISTAT dell'ateneo
     * @param universityId id univoco ateneo
     * @param unifiedCode Codice università MIUR
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getUniversities(
        istatCode: String? = null,
        universityId: Long? = null,
        unifiedCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3University> {
        return executeJsonGetList<Esse3University>("/atenei", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            istatCode?.let { parameter("istatCod", it) }
            universityId?.let { parameter("ateneoId", it) }
            unifiedCode?.let { parameter("codeUn", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero corsi di studio di un ateneo
     *
     * @param erasmusCode Codice Erasmus ateneo
     * @param universityId id univoco ateneo
     * @param istatCode Codice ISTAT dell'ateneo
     * @param unifiedCode Codice università MIUR
     * @param courseTypeCode Codice tipo corso di studio.
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getUniversityCourses(
        erasmusCode: String? = null,
        universityId: Long? = null,
        istatCode: String? = null,
        unifiedCode: String? = null,
        courseTypeCode: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3StudyCourse> {
        return executeJsonGetList<Esse3StudyCourse>("/atenei/corsi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            erasmusCode?.let { parameter("erasmusCod", it) }
            universityId?.let { parameter("ateneoId", it) }
            istatCode?.let { parameter("istatCod", it) }
            unifiedCode?.let { parameter("codeUn", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero atenei stranieri
     *
     * @param erasmusCode Codice Erasmus ateneo
     * @param foreignUniversityId id univoco ateneo
     * @param nationFiscalCode Codice fiscale della nazione
     * @param orderNationFiscalCode Codice fiscale della nazione di ordinamento
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getForeignUniversities(
        erasmusCode: String? = null,
        foreignUniversityId: Long? = null,
        nationFiscalCode: String? = null,
        orderNationFiscalCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ForeignUniversity> {
        return executeJsonGetList<Esse3ForeignUniversity>("/ateneiStranieri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            erasmusCode?.let { parameter("erasmusCod", it) }
            foreignUniversityId?.let { parameter("ateneoStranieroId", it) }
            nationFiscalCode?.let { parameter("nazioneCodFisc", it) }
            orderNationFiscalCode?.let { parameter("nazioneOrdCodFisc", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getCareers(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3Career> {
        return executeJsonGetList<Esse3Career>("/carriere", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * Recupero della carriera degli studenti
     *
     * @param studentId identificativo della carriera
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getGdprCareerByStudent(
        studentId: Long,
        optionalFields: String? = null
    ): List<Esse3CareerGDPR> {
        return executeJsonGetList<Esse3CareerGDPR>("/carriere-gdpr/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * permette di aggiornare i dati relativi all'attesa di laurea dello studente
     *
     * @param body Oggetto con i parametri relativi all'attesa di laurea Per identificare lo studente occorre passare come parametro lo stuId o la matricola. Se attlauFlg vale 0, il parametro dataAttlau deve essere null. Se attlauFlg vale 1, il parametro dataAttlau non deve essere null.
     */
    suspend fun putGraduationWaiting(
        body: Esse3GraduationWaitingParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/aggiornaAttesaLaurea", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupero delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentId identificativo dello studente
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getPhdCareersData(
        userId: String? = null,
        govIdentifier: String? = null,
        studentId: Long? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        fromModificationTime: String? = null,
        onlyEnrolled: Int? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearEnrollmentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        onlyActive: Int? = null
    ): List<Esse3PhDProgramCareer> {
        return executeJsonGetList<Esse3PhDProgramCareer>("/carriere/datiDottorato", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentId?.let { parameter("stuId", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * Recupero dei dati minimi delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getMinimalCareersData(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3CareerMinimalData> {
        return executeJsonGetList<Esse3CareerMinimalData>("/carriere/datiMin", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * aggiorna la data di iscrizione e il tipo esonero, effettuando il recupero per matricola
     *
     * @param matricola matricola dello studente
     * @param body Oggetto con i parametri relativi alla data di iscrizione e alla tipologia esonero
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putEnrollmentDateAndExemptionTypeByMatricola(
        matricola: String,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${matricola}/iscrizioni/aggiornaDataIscrAndTipoEsoneroByMat", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * Recupero della carriera degli studenti
     *
     * @param studentId identificativo della carriera
     */
    suspend fun getCareerByStudent(
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'aggiornamento del numero di protocollo dello studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putCareerByStudent(
        studentId: Long,
        body: Esse3CareerParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * effettua la chiusura della carriera per un dato studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri per la chiusura della carriera
     */
    suspend fun careerClosure(
        studentId: Long,
        body: Esse3CareerClosureParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}/chiudiCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupero delle iscrizione di uno studente
     *
     * @param studentId identificativo della carriera
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     * @param lastEnrollmentFlag 1 se si vuole recuperare SOLO l'ultima iscrizione, 0 altrimenti
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getAnnualEnrollment(
        studentId: Long,
        academicYear: Long? = null,
        lastEnrollmentFlag: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYear?.let { parameter("annoAccademico", it) }
            lastEnrollmentFlag?.let { parameter("ultimaIscrizioneFlg", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * aggiorna la data di iscrizione e il tipo esonero
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla data di iscrizione e alla tipologia esonero
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putEnrollmentDateAndExemptionTypeByStudentId(
        studentId: Long,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaDataIscrAndTipoEsonero", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * aggiorna fascia mensa di uno studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla fascia mensa
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putCanteenBand(
        studentId: Long,
        body: Esse3CanteenBandParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaFasciaMensa", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * aggiorna codice tipologia studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla tipologia studente
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putStudentTypeCode(
        studentId: Long,
        body: Esse3StudentTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaTipoStuCod", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * recupero dei dati bancari
     *
     * @param personId identificativo della persona
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getBankDetails(
        personId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3BankDetails> {
        return executeJsonGetList<Esse3BankDetails>("/datiBancari/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * @param lecturerId id del docente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturer(
        lecturerId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonGetList<Esse3NewTeachers>("/docenti/${lecturerId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero delle carriere degli studenti per anno accademico.
     *
     * @param academicYearEnrollmentId id anno accademico delle iscrizioni che si vogliono recuperare
     * @param includeSXH includi iscrizioni sospese per ipotesi.
     * @param includeCondition includi iscrizioni condizionate.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param enrollmentTypeCode Codice tipo iscrizione.
     * @param courseYear Anno di corso.
     * @param enrollmentStatusCode codice stato iscrizione annuale.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEnrollments(
        academicYearEnrollmentId: String,
        includeSXH: Long,
        includeCondition: Long,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        enrollmentTypeCode: String? = null,
        courseYear: Long? = null,
        enrollmentStatusCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/iscrizioni/${academicYearEnrollmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("inclSXH", includeSXH)
            parameter("inclCond", includeCondition)
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            enrollmentTypeCode?.let { parameter("tipoIscrCod", it) }
            courseYear?.let { parameter("annoCorso", it) }
            enrollmentStatusCode?.let { parameter("staIscCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupero istituti
     *
     * @param schoolTypologyCode Codice della tipologia della scuola.
     * @param miurSchoolCode Codice meccanografico della scuola.
     * @param higherSchoolId Identificativo della scuola.
     * @param miurSchoolId Identificativo scuola MIUR.
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getInstitutions(
        schoolTypologyCode: String? = null,
        miurSchoolCode: String? = null,
        higherSchoolId: Long? = null,
        miurSchoolId: Long? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3Institute> {
        return executeJsonGetList<Esse3Institute>("/istituti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            schoolTypologyCode?.let { parameter("tipologiaScuolaCod", it) }
            miurSchoolCode?.let { parameter("scuolaMiurCod", it) }
            higherSchoolId?.let { parameter("scuolaSupId", it) }
            miurSchoolId?.let { parameter("scuolaMiurId", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero misure compensative
     *
     * @param handicapType tipo di handicap
     */
    suspend fun getCompensatoryMeasures(
        handicapType: String? = null
    ): List<Esse3CompensatoryMeasures> {
        return executeJsonGetList<Esse3CompensatoryMeasures>("/misureCompensative", setOf(Esse3PermissionLevel.ANY)) {
            handicapType?.let { parameter("tipoHandicap", it) }
        }
    }

    /**
     * Recupero delle normative legate alle dichiarazioni di handicap
     */
    suspend fun getHandicapRegulations(): List<Esse3HandicapRegulations> {
        return executeJsonGetList<Esse3HandicapRegulations>("/normativeHandicap", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero delle anagrafiche presenti a sistema
     *
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param personId identificativo della persona
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getPersons(
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        fromModificationDate: String? = null,
        fromModificationTime: String? = null,
        personId: Long? = null,
        govIdentifier: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3PersonCommonRegistry> {
        return executeJsonGetList<Esse3PersonCommonRegistry>("/persone", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            personId?.let { parameter("persId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero delle anagrafiche presenti a sistema
     *
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getGdprPersons(
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        fromModificationDate: String? = null,
        fromModificationTime: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3PersonGDPR> {
        return executeJsonGetList<Esse3PersonGDPR>("/persone-gdpr", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupero delle informazioni relative ad una singola persona presente a sistema ed identificata dal persid
     *
     * @param personId identificativo della persona
     */
    suspend fun getGdprPerson(
        personId: Long
    ): Esse3PersonGDPR {
        return executeJsonGet<Esse3PersonGDPR>("/persone-gdpr/${personId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Dismette un indirizzo email istituzionale
     *
     * @param universityEmail indirizzio email istituzionale da dismettere
     */
    suspend fun dismissEmailByAteEmail(
        universityEmail: String
    ): Esse3Person {
        return executeJsonPatch<Esse3Person>("/persone/${universityEmail}/dismetti", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero delle informazioni relative ad una singola persona presente a sistema ed identificata dal persid
     *
     * @param personId identificativo della persona
     */
    suspend fun getPerson(
        personId: Long
    ): Esse3Person {
        return executeJsonGet<Esse3Person>("/persone/${personId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'aggiornamento dei dettagli di una misura compensative associate alla dichiarazione di invalidità di una anagrafica.
     *
     * @param personId identificativo della persona
     * @param body Oggetto con i campi da modificare
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param handicapDeclarationMeasuresId Identificativo associazione dichiarazione e misura compensativa.
     * @param handicapType tipo di handicap
     * @param compensatoryMeasureCode codice misura compensativa
     */
    suspend fun putPersonCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        body: Esse3PutCompensatoryMeasuresHandicapDeclarationParameters,
        handicapDeclarationId: Long,
        handicapDeclarationMeasuresId: Long? = null,
        handicapType: String? = null,
        compensatoryMeasureCode: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonPutList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/agg-dicHandMisComp", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("dicHandId", handicapDeclarationId)
            handicapDeclarationMeasuresId?.let { parameter("dicHandMisureId", it) }
            handicapType?.let { parameter("tipoHandicap", it) }
            compensatoryMeasureCode?.let { parameter("misuraCompensativaCod", it) }
        }
    }

    /**
     * recupero degli autorizzati legati ad una anagrafica
     *
     * @param personId identificativo della persona
     * @param authorizedId identificativo dell'autorizzato
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getAuthorizedForPerson(
        personId: Long,
        authorizedId: Long? = null,
        optionalFields: String? = null
    ): List<Esse3AuthorizedPerson> {
        return executeJsonGetList<Esse3AuthorizedPerson>("/persone/${personId}/autorizzati/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            authorizedId?.let { parameter("autorizzatoId", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero della carriera degli studenti
     *
     * @param personId identificativo della persona
     * @param studentId identificativo della carriera
     */
    suspend fun getCareerByStudentPerson(
        personId: Long,
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/persone/${personId}/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * aggiornamento cellulare
     *
     * @param personId identificativo della persona
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putMobilePhone(
        personId: Long,
        body: Esse3MobileParameter
    ): Esse3MobileParameter {
        return executeJsonPut<Esse3MobileParameter>("/persone/${personId}/cellulare", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera i consensi relativi ad uno studente
     *
     * @param personId identificativo della persona
     * @param webProcedureCode Codice processo WEB
     * @param iso6392Code Codice ISO lingua
     * @param studentId identificativo della carriera
     * @param academicYearId identificativo anno accademico
     */
    suspend fun getStudentConsents(
        personId: Long,
        webProcedureCode: String,
        iso6392Code: String? = null,
        studentId: Long? = null,
        academicYearId: Long? = null
    ): List<Esse3StudentsConsents> {
        return executeJsonGetList<Esse3StudentsConsents>("/persone/${personId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
            studentId?.let { parameter("stuId", it) }
            academicYearId?.let { parameter("aaId", it) }
        }
    }

    /**
     * Effettua l'aggiornamento dei consensi dello studente
     *
     * @param personId identificativo della persona
     * @param body Array contenente i consensi che si vogliono aggiornare
     * @param webProcedureCode Codice processo WEB
     * @param iso6392Code Codice ISO lingua
     * @param studentId identificativo della carriera
     * @param academicYearId identificativo anno accademico
     */
    suspend fun putStudentConsents(
        personId: Long,
        body: List<Esse3ConsentsParameters>,
        webProcedureCode: String,
        iso6392Code: String? = null,
        studentId: Long? = null,
        academicYearId: Long? = null
    ): List<Esse3StudentsConsents> {
        return executeJsonPutList<Esse3StudentsConsents>("/persone/${personId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
            studentId?.let { parameter("stuId", it) }
            academicYearId?.let { parameter("aaId", it) }
        }
    }

    /**
     * Effettua l'inserimento dei titoli di studio relativi ad una persona
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param body Array contenente i dati delle Misure Compensative legate ad una Dichiarazioni di invalidità da inserire
     * @param handicapType tipo di handicap
     */
    suspend fun insertCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long,
        body: Esse3PostCompensatoryMeasuresHandicapDeclarationParameters,
        handicapType: String? = null
    ) {
        val response = executePost("/persone/${personId}/dic-hand/${handicapDeclarationId}/misure-compensative-dicHand") {
            contentType(ContentType.Application.Json)
            setBody(body)
            handicapType?.let { parameter("tipoHandicap", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT))
    }

    /**
     * effettua l'aggiornamento dei dettagli di una misura compensative associate alla dichiarazione di invalidità di una anagrafica.
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param handicapDeclarationMeasuresId Identificativo associazione dichiarazione e misura compensativa.
     * @param body Oggetto con i campi da modificare
     * @param handicapType tipo di handicap
     * @param compensatoryMeasureCode codice misura compensativa
     */
    suspend fun updatePersonCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long,
        handicapDeclarationMeasuresId: Long,
        body: Esse3PutCompensatoryMeasuresHandicapDeclarationParameters,
        handicapType: String? = null,
        compensatoryMeasureCode: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonPutList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/dic-hand/${handicapDeclarationId}/misure-compensative-dicHand/${handicapDeclarationMeasuresId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            handicapType?.let { parameter("tipoHandicap", it) }
            compensatoryMeasureCode?.let { parameter("misuraCompensativaCod", it) }
        }
    }

    /**
     * recupero delle dichiarazioni di handicap legate ad un'anagrafica
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     */
    suspend fun getHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long? = null
    ): List<Esse3HandicapDeclaration> {
        return executeJsonGetList<Esse3HandicapDeclaration>("/persone/${personId}/dicHand", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            handicapDeclarationId?.let { parameter("dicHandId", it) }
        }
    }

    /**
     * Aggiornamento di una dichiarazioni di handicap legata ad un'anagrafica
     *
     * @param personId identificativo della persona
     * @param body Oggetto che contiene i campi della dichiarazione da aggiornare
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param handicapType tipo di handicap
     */
    suspend fun putHandicapDeclaration(
        personId: Long,
        body: Esse3HandicapDeclarationPut,
        handicapDeclarationId: Long? = null,
        handicapType: String? = null
    ): List<Esse3HandicapDeclaration> {
        return executeJsonPutList<Esse3HandicapDeclaration>("/persone/${personId}/dicHand", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            handicapDeclarationId?.let { parameter("dicHandId", it) }
            handicapType?.let { parameter("tipoHandicap", it) }
        }
    }

    /**
     * recupero di una dichiarazioni di handicap legata ad un'anagrafica
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     */
    suspend fun getHandicapDeclarationById(
        personId: Long,
        handicapDeclarationId: Long
    ): Esse3HandicapDeclaration {
        return executeJsonGet<Esse3HandicapDeclaration>("/persone/${personId}/dicHand/${handicapDeclarationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT))
    }

    /**
     * Aggiornamento di una dichiarazioni di handicap legata ad un'anagrafica
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param body Oggetto che contiene i campi della dichiarazione da aggiornare
     */
    suspend fun putHandicapDeclarationById(
        personId: Long,
        handicapDeclarationId: Long,
        body: Esse3HandicapDeclarationPut
    ): Esse3HandicapDeclaration {
        return executeJsonPut<Esse3HandicapDeclaration>("/persone/${personId}/dicHand/${handicapDeclarationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera il blob dell'allegato richiesto
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param attachmentId identificativo allegato
     * @param userId userId
     */
    suspend fun getAttachmentContent(
        personId: Long,
        handicapDeclarationId: Long,
        attachmentId: Long,
        userId: String? = null
    ): ByteReadChannel {
        return executeStreamGet("/persone/${personId}/dicHand/${handicapDeclarationId}/allegatiDicHand/${attachmentId}/blob", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            userId?.let { parameter("userId", it) }
        }
    }

    /**
     * Dismette un indirizzo email istituzionale
     *
     * @param personId identificativo della persona
     * @param universityEmail indirizzio email istituzionale da dismettere
     */
    suspend fun dismissEmail(
        personId: Long,
        universityEmail: String? = null
    ): Esse3Person {
        return executeJsonPatch<Esse3Person>("/persone/${personId}/dismettiEmail", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            universityEmail?.let { parameter("emailAte", it) }
        }
    }

    /**
     * Effettua l'aggiornamento dell'email personale dello studente
     *
     * @param personId identificativo della persona
     * @param email indirizzo email personale
     */
    suspend fun putStudentEmail(
        personId: Long,
        email: String? = null
    ): Esse3Person {
        return executeJsonPut<Esse3Person>("/persone/${personId}/email", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            email?.let { parameter("email", it) }
        }
    }

    /**
     * Effettua l'aggiornamento dell'email istituzionale dello studente
     *
     * @param personId identificativo della persona
     * @param universityEmail indirizzo email istituzionale
     */
    suspend fun putStudentAteEmail(
        personId: Long,
        universityEmail: String
    ): Esse3Person {
        return executeJsonPut<Esse3Person>("/persone/${personId}/emailAte", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("emailAte", universityEmail)
        }
    }

    /**
     * @param personId identificativo della persona
     */
    suspend fun getPersonPhoto(
        personId: Long
    ): ByteReadChannel {
        return executeStreamGet("/persone/${personId}/foto", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Effettua l'inserimento dei titoli di studio relativi ad una persona
     *
     * @param personId identificativo della persona
     * @param body Array contenente i dati delle Misure Compensative legate ad una Dichiarazioni di invalidità da inserire
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param handicapType tipo di handicap
     */
    suspend fun postCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        body: Esse3PostCompensatoryMeasuresHandicapDeclarationParameters,
        handicapDeclarationId: Long,
        handicapType: String? = null
    ) {
        val response = executePost("/persone/${personId}/ins-dicHandMisComp") {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("dicHandId", handicapDeclarationId)
            handicapType?.let { parameter("tipoHandicap", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * misure compensative per i bisogni speciali degli studeneti
     *
     * @param personId identificativo della persona
     * @param callStartDate data inizio appello.
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3CompensatoryMeasureFilter.Prenotazione]: recupera solo le misure compensative valide per un determinato appello
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getPersonCompensatoryMeasures(
        personId: Long,
        callStartDate: String? = null,
        q: Esse3CompensatoryMeasureFilter? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonGetList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/misure-compensative", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            callStartDate?.let { parameter("dataInizioApp", it) }
            q?.let { parameter("q", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * misure compensative per i bisogni speciali degli studeneti
     *
     * @param personId identificativo della persona
     * @param handicapDeclarationId identificativo dichiarazione invalidità
     * @param handicapType tipo di handicap
     */
    suspend fun getPersonCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long? = null,
        handicapType: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonGetList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/misure-compensative-dicHand", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            handicapDeclarationId?.let { parameter("dicHandId", it) }
            handicapType?.let { parameter("tipoHandicap", it) }
        }
    }

    /**
     * aggiornamento telefono di domicilio
     *
     * @param personId identificativo della persona
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putDomicilePhone(
        personId: Long,
        body: Esse3PhoneParameters
    ): Esse3PhoneParameters {
        return executeJsonPut<Esse3PhoneParameters>("/persone/${personId}/telefono-domicilio", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * aggiornamento telefono di residenza
     *
     * @param personId identificativo della persona
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putResidencePhone(
        personId: Long,
        body: Esse3PhoneParameters
    ): Esse3PhoneParameters {
        return executeJsonPut<Esse3PhoneParameters>("/persone/${personId}/telefono-residenza", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * recupero dei tutori legati ad una anagrafica
     *
     * @param personId identificativo della persona
     * @param tutorsFilter 1: recupera tutti i tutori. 0 (o nullo): recupera solo i tutori validi.
     */
    suspend fun getPersonTutors(
        personId: Long,
        tutorsFilter: Int? = null
    ): List<Esse3Tutor> {
        return executeJsonGetList<Esse3Tutor>("/persone/${personId}/tutori/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            tutorsFilter?.let { parameter("filtroTutori", it) }
        }
    }

    /**
     * @param personId identificativo della persona
     */
    suspend fun getPhotoValidationFlag(
        personId: Long
    ): Esse3ValidationFlag {
        return executeJsonGet<Esse3ValidationFlag>("/persone/${personId}/validaFoto", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero range voti maturità
     */
    suspend fun getHighSchoolGradeRange(): List<Esse3HighSchoolGradeRange> {
        return executeJsonGetList<Esse3HighSchoolGradeRange>("/rangeVotiMaturita", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero dei soggetti esterni
     *
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param departmentId id dipartimento
     * @param externalSubjectTypeCode codice tipo relatore
     * @param abbreviatedId identificativo address book della persona in U-GOV
     * @param fiscalCode codice fiscale dell'utente
     * @param externalSubjectId Lista di identificativi di Soggetti Esterni
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getExternalSubject(
        surname: String? = null,
        name: String? = null,
        departmentId: Long? = null,
        externalSubjectTypeCode: String? = null,
        abbreviatedId: Long? = null,
        fiscalCode: String? = null,
        externalSubjectId: List<Long>? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ExternalSubject> {
        return executeJsonGetList<Esse3ExternalSubject>("/soggettiEsterni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            departmentId?.let { parameter("dipId", it) }
            externalSubjectTypeCode?.let { parameter("tipoSoggEstCod", it) }
            abbreviatedId?.let { parameter("idAb", it) }
            fiscalCode?.let { parameter("codFis", it) }
            externalSubjectId?.let { parameter("soggEstId", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Inserisce oppure aggiorna i dati di un soggetto esterno in esse3
     *
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putExternalSubject(
        body: Esse3PutExternalSubject
    ): List<Esse3ExternalSubject> {
        return executeJsonPutList<Esse3ExternalSubject>("/soggettiEsterni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Elimina i dati di un soggetto esterno in esse3
     *
     * @param externalSubjectId Identificativo soggetto esterno
     */
    suspend fun deleteExternalSubject(
        externalSubjectId: Int
    ) {
        val response = executeDelete("/soggettiEsterni/${externalSubjectId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera i consensi relativi ad un soggetto esterno
     *
     * @param externalSubjectId Identificativo soggetto esterno
     * @param webProcedureCode Codice processo WEB
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun getExternalSubjectConsents(
        externalSubjectId: Int,
        webProcedureCode: String,
        iso6392Code: String? = null
    ): List<Esse3ExternalSubjectsConsents> {
        return executeJsonGetList<Esse3ExternalSubjectsConsents>("/soggettiEsterni/${externalSubjectId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * Effettua l'aggiornamento dei consensi del soggetto esterno
     *
     * @param externalSubjectId Identificativo soggetto esterno
     * @param body Array contenente i consensi che si vogliono aggiornare
     * @param webProcedureCode Codice processo WEB
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun putExternalSubjectConsents(
        externalSubjectId: Int,
        body: List<Esse3ConsentsParameters>,
        webProcedureCode: String,
        iso6392Code: String? = null
    ): List<Esse3ExternalSubjectsConsents> {
        return executeJsonPutList<Esse3ExternalSubjectsConsents>("/soggettiEsterni/${externalSubjectId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * Recupero dei soggetti esterni
     *
     * @param externalSubjectId identificativo soggetto esterno
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param departmentId id dipartimento
     * @param externalSubjectTypeCode codice tipo relatore
     * @param abbreviatedId identificativo address book della persona in U-GOV
     * @param fiscalCode codice fiscale dell'utente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getExternalSubjectsReplica(
        externalSubjectId: Long? = null,
        surname: String? = null,
        name: String? = null,
        departmentId: Long? = null,
        externalSubjectTypeCode: String? = null,
        abbreviatedId: Long? = null,
        fiscalCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ExternalSubjectReplica> {
        return executeJsonGetList<Esse3ExternalSubjectReplica>("/soggettiEsterniReplica", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            externalSubjectId?.let { parameter("soggEstId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            departmentId?.let { parameter("dipId", it) }
            externalSubjectTypeCode?.let { parameter("tipoSoggEstCod", it) }
            abbreviatedId?.let { parameter("idAb", it) }
            fiscalCode?.let { parameter("codFis", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero dei soggetti esterni
     *
     * @param externalSubjectId Identificativo soggetto esterno
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExternalSubjectReplica(
        externalSubjectId: Int,
        optionalFields: String? = null
    ): Esse3ExternalSubjectReplica {
        return executeJsonGet<Esse3ExternalSubjectReplica>("/soggettiEsterniReplica/${externalSubjectId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero tipi istituto superiore
     */
    suspend fun getHigherInstitutionTypes(): List<Esse3HigherInstituteTypes> {
        return executeJsonGetList<Esse3HigherInstituteTypes>("/tipiIstituto", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero tipi titoli scuola superiore
     *
     * @param titleTypologyCode Codice della tipologia del titolo superiore.
     * @param titleTypeCode Codice MIUR del tipo di titolo superiore.
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getHigherSchoolTitleTypes(
        titleTypologyCode: String? = null,
        titleTypeCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3HigherSchoolTitleType> {
        return executeJsonGetList<Esse3HigherSchoolTitleType>("/tipiTitoliScuolaSup", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            titleTypologyCode?.let { parameter("tipologiaTitoloCod", it) }
            titleTypeCode?.let { parameter("tipoTitoloCod", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero tipi titoli stranieri
     *
     * @param levelCode Indica se il titolo è di livello universitario (U) o di livello di scuola superiore (S).
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getForeignTitleTypes(
        levelCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ForeignTitleType> {
        return executeJsonGetList<Esse3ForeignTitleType>("/tipiTitoliStranieri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            levelCode?.let { parameter("livelloCod", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupero tipologie di dichiarazione dei titoli stranieri
     *
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getForeignTitleValueDeclarationTypologies(
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ForeignTitleValidationDeclaration> {
        return executeJsonGetList<Esse3ForeignTitleValidationDeclaration>("/tipologieDichiarazioneValoreTitoloStraniero", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupera le tipologie di handicap
     *
     * @param lecturerId id univoco che consente di individuare l'account utente
     */
    suspend fun getHandicapTypologies(
        lecturerId: Long? = null
    ): List<Esse3HandicapTypesLookup> {
        return executeJsonGetList<Esse3HandicapTypesLookup>("/tipologieHandicap", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            lecturerId?.let { parameter("docenteId", it) }
        }
    }

    /**
     * Recupera le tipologie di handicap per cui sono presenti dichiarazioni da valutare.
     *
     * @param lecturerId id univoco che consente di individuare l'account utente
     */
    suspend fun getHandicapTypologiesToEvaluate(
        lecturerId: Long? = null
    ): List<Esse3HandicapTypesLookup> {
        return executeJsonGetList<Esse3HandicapTypesLookup>("/tipologieHandicapDaValutare", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            lecturerId?.let { parameter("docenteId", it) }
        }
    }

    /**
     * Recupera le tipologie di parentele
     */
    suspend fun getRelationshipTypologies(): List<Esse3RelationshipTypes> {
        return executeJsonGetList<Esse3RelationshipTypes>("/tipologieParentele", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Effettua l'aggiornamento o l'inserimento dei titoli di studio relativi ad una persona
     *
     * @param body Array contenente i titoli e i parametri delle persone chew voglio aggiornare
     */
    suspend fun putTitles(
        body: Esse3TitlesInsertion
    ): Esse3EnrollmentReturn {
        return executeJsonPost<Esse3EnrollmentReturn>("/titoli/import", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera i titoli relativi ad una persona
     *
     * @param personId identificativo della persona
     * @param studentId identificativo dello studente
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getTitles(
        personId: Long,
        studentId: Long? = null,
        optionalFields: String? = null
    ): Esse3PersonTitles {
        return executeJsonGet<Esse3PersonTitles>("/titoli/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)) {
            studentId?.let { parameter("stuId", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera i titoli relativi ad una persona
     *
     * @param personId identificativo della persona
     * @param studentId identificativo dello studente
     * @param fiscalCode codice fiscale dell'utente
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getPersonTitles(
        personId: Long? = null,
        studentId: Long? = null,
        fiscalCode: String? = null,
        optionalFields: String? = null
    ): Esse3PersonTitles {
        return executeJsonGet<Esse3PersonTitles>("/titoliPersona", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)) {
            personId?.let { parameter("persId", it) }
            studentId?.let { parameter("stuId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupero delle regole di richiesta tutori
     */
    suspend fun getTutorRules(): List<Esse3TutorsRulesHeader> {
        return executeJsonGetList<Esse3TutorsRulesHeader>("/tutori/regoleRichiesta/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
