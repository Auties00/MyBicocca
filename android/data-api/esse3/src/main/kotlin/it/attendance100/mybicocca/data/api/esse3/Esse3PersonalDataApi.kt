package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AnnualEnrollment
import it.attendance100.mybicocca.data.dto.esse3.Esse3AuthorizationAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3AuthorizedPerson
import it.attendance100.mybicocca.data.dto.esse3.Esse3BankDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3CanteenBandParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3Career
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerClosureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerMinimalData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompensatoryMeasures
import it.attendance100.mybicocca.data.dto.esse3.Esse3ConsentsParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentNumberAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentReturn
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExternalSubject
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExternalSubjectReplica
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExternalSubjectsConsents
import it.attendance100.mybicocca.data.dto.esse3.Esse3ForeignTitleAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3ForeignTitleType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ForeignTitleValidationDeclaration
import it.attendance100.mybicocca.data.dto.esse3.Esse3ForeignUniversity
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetAuthorizationAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetEnrollmentNumberAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetForeignTitleAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetHandicapDeclarationAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetHighSchoolDiplomaAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetIdentityDocumentAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetItalianTitleAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GetPersonalDocumentAuthorizationMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3GraduationWaitingParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapDeclaration
import it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapDeclarationAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapDeclarationPut
import it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapRegulations
import it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapTypesLookup
import it.attendance100.mybicocca.data.dto.esse3.Esse3HighSchoolDiplomaAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3HighSchoolGradeRange
import it.attendance100.mybicocca.data.dto.esse3.Esse3HigherInstituteTypes
import it.attendance100.mybicocca.data.dto.esse3.Esse3HigherSchoolTitleType
import it.attendance100.mybicocca.data.dto.esse3.Esse3IdentityDocumentAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3Institute
import it.attendance100.mybicocca.data.dto.esse3.Esse3ItalianTitleAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3MobileParameter
import it.attendance100.mybicocca.data.dto.esse3.Esse3NewTeachers
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3Person
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonCommonRegistry
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonCompensatoryMeasures
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonPhotoAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonTitles
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonalDocumentAuthorizationMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3PhDProgramCareer
import it.attendance100.mybicocca.data.dto.esse3.Esse3PhoneParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostCompensatoryMeasuresHandicapDeclarationParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PutCompensatoryMeasuresHandicapDeclarationParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PutExternalSubject
import it.attendance100.mybicocca.data.dto.esse3.Esse3RefreshedToken
import it.attendance100.mybicocca.data.dto.esse3.Esse3RelationshipTypes
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentTypeParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentsConsents
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyCourse
import it.attendance100.mybicocca.data.dto.esse3.Esse3TitlesInsertion
import it.attendance100.mybicocca.data.dto.esse3.Esse3Tutor
import it.attendance100.mybicocca.data.dto.esse3.Esse3TutorsRulesHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3University
import it.attendance100.mybicocca.data.dto.esse3.Esse3ValidationFlag
import kotlinx.serialization.json.Json

class Esse3PersonalDataApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/anagrafica-service-v2") {

    suspend fun refreshToken(
        applicationId: String
    ): Esse3RefreshedToken {
        return executeJsonPut<Esse3RefreshedToken>("/activation-url${applicationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun getGdprCareerByStudent(
        studentId: Long,
        optionalFields: String? = null
    ): List<Esse3CareerGDPR> {
        return executeJsonGetList<Esse3CareerGDPR>("/carriere-gdpr/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putGraduationWaiting(
        body: Esse3GraduationWaitingParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/aggiornaAttesaLaurea", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

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

    suspend fun getCareerByStudent(
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putCareerByStudent(
        studentId: Long,
        body: Esse3CareerParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun careerClosure(
        studentId: Long,
        body: Esse3CareerClosureParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}/chiudiCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

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

    suspend fun getCompensatoryMeasures(
        handicapType: String? = null
    ): List<Esse3CompensatoryMeasures> {
        return executeJsonGetList<Esse3CompensatoryMeasures>("/misureCompensative", setOf(Esse3PermissionLevel.ANY)) {
            handicapType?.let { parameter("tipoHandicap", it) }
        }
    }

    suspend fun getHandicapRegulations(): List<Esse3HandicapRegulations> {
        return executeJsonGetList<Esse3HandicapRegulations>("/normativeHandicap", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun getGdprPerson(
        personId: Long
    ): Esse3PersonGDPR {
        return executeJsonGet<Esse3PersonGDPR>("/persone-gdpr/${personId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun dismissEmailByAteEmail(
        ateEmail: String
    ): Esse3Person {
        return executeJsonPatch<Esse3Person>("/persone/${ateEmail}/dismetti", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getPerson(
        personId: Long
    ): Esse3Person {
        return executeJsonGet<Esse3Person>("/persone/${personId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putPersonCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        body: Esse3PutCompensatoryMeasuresHandicapDeclarationParameters,
        handicapDeclarationId: Long,
        handicapDeclarationMeasuresId: Long? = null,
        handicapType: String? = null,
        compensatoryMeasureCode: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonGetList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/agg-dicHandMisComp", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("dicHandId", handicapDeclarationId)
            handicapDeclarationMeasuresId?.let { parameter("dicHandMisureId", it) }
            handicapType?.let { parameter("tipoHandicap", it) }
            compensatoryMeasureCode?.let { parameter("misuraCompensativaCod", it) }
        }
    }

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

    suspend fun getCareerByStudentPerson(
        personId: Long,
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/persone/${personId}/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putMobilePhone(
        personId: Long,
        body: Esse3MobileParameter
    ): Esse3MobileParameter {
        return executeJsonPut<Esse3MobileParameter>("/persone/${personId}/cellulare", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

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

    suspend fun putStudentConsents(
        personId: Long,
        body: List<Esse3ConsentsParameters>,
        webProcedureCode: String,
        iso6392Code: String? = null,
        studentId: Long? = null,
        academicYearId: Long? = null
    ): List<Esse3StudentsConsents> {
        return executeJsonGetList<Esse3StudentsConsents>("/persone/${personId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
            studentId?.let { parameter("stuId", it) }
            academicYearId?.let { parameter("aaId", it) }
        }
    }

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
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.UNKNOWN))
    }

    suspend fun updatePersonCompensatoryMeasuresHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long,
        handicapDeclarationMeasuresId: Long,
        body: Esse3PutCompensatoryMeasuresHandicapDeclarationParameters,
        handicapType: String? = null,
        compensatoryMeasureCode: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonGetList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/dic-hand/${handicapDeclarationId}/misure-compensative-dicHand/${handicapDeclarationMeasuresId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.UNKNOWN)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            handicapType?.let { parameter("tipoHandicap", it) }
            compensatoryMeasureCode?.let { parameter("misuraCompensativaCod", it) }
        }
    }

    suspend fun getHandicapDeclaration(
        personId: Long,
        handicapDeclarationId: Long? = null
    ): List<Esse3HandicapDeclaration> {
        return executeJsonGetList<Esse3HandicapDeclaration>("/persone/${personId}/dicHand", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            handicapDeclarationId?.let { parameter("dicHandId", it) }
        }
    }

    suspend fun putHandicapDeclaration(
        personId: Long,
        body: Esse3HandicapDeclarationPut,
        handicapDeclarationId: Long? = null,
        handicapType: String? = null
    ): List<Esse3HandicapDeclaration> {
        return executeJsonGetList<Esse3HandicapDeclaration>("/persone/${personId}/dicHand", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            handicapDeclarationId?.let { parameter("dicHandId", it) }
            handicapType?.let { parameter("tipoHandicap", it) }
        }
    }

    suspend fun getHandicapDeclarationById(
        personId: Long,
        handicapDeclarationId: Long
    ): Esse3HandicapDeclaration {
        return executeJsonGet<Esse3HandicapDeclaration>("/persone/${personId}/dicHand/${handicapDeclarationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.UNKNOWN))
    }

    suspend fun putHandicapDeclarationById(
        personId: Long,
        handicapDeclarationId: Long,
        body: Esse3HandicapDeclarationPut
    ): Esse3HandicapDeclaration {
        return executeJsonPut<Esse3HandicapDeclaration>("/persone/${personId}/dicHand/${handicapDeclarationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.UNKNOWN)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getAttachmentContent(
        personId: Long,
        handicapDeclarationId: Long,
        attachmentId: Long,
        userId: String? = null
    ): String {
        return executeJsonGet<String>("/persone/${personId}/dicHand/${handicapDeclarationId}/allegatiDicHand/${attachmentId}/blob", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.UNKNOWN)) {
            userId?.let { parameter("userId", it) }
        }
    }

    suspend fun dismissEmail(
        personId: Long,
        ateEmail: String? = null
    ): Esse3Person {
        return executeJsonPatch<Esse3Person>("/persone/${personId}/dismettiEmail", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            ateEmail?.let { parameter("emailAte", it) }
        }
    }

    suspend fun putStudentEmail(
        personId: Long,
        email: String? = null
    ): Esse3Person {
        return executeJsonPut<Esse3Person>("/persone/${personId}/email", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            email?.let { parameter("email", it) }
        }
    }

    suspend fun putStudentAteEmail(
        personId: Long,
        ateEmail: String
    ): Esse3Person {
        return executeJsonPut<Esse3Person>("/persone/${personId}/emailAte", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("emailAte", ateEmail)
        }
    }

    suspend fun getPersonPhoto(
        personId: Long
    ): String {
        return executeJsonGet<String>("/persone/${personId}/foto", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun getPersonCompensatoryMeasures(
        personId: Long,
        callStartDate: String? = null,
        q: String? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3PersonCompensatoryMeasures> {
        return executeJsonGetList<Esse3PersonCompensatoryMeasures>("/persone/${personId}/misure-compensative", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            callStartDate?.let { parameter("dataInizioApp", it) }
            q?.let { parameter("q", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

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

    suspend fun putDomicilePhone(
        personId: Long,
        body: Esse3PhoneParameters
    ): Esse3PhoneParameters {
        return executeJsonPut<Esse3PhoneParameters>("/persone/${personId}/telefono-domicilio", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putResidencePhone(
        personId: Long,
        body: Esse3PhoneParameters
    ): Esse3PhoneParameters {
        return executeJsonPut<Esse3PhoneParameters>("/persone/${personId}/telefono-residenza", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getPersonTutors(
        personId: Long,
        tutorsFilter: Int? = null
    ): List<Esse3Tutor> {
        return executeJsonGetList<Esse3Tutor>("/persone/${personId}/tutori/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            tutorsFilter?.let { parameter("filtroTutori", it) }
        }
    }

    suspend fun getPhotoValidationFlag(
        personId: Long
    ): Esse3ValidationFlag {
        return executeJsonGet<Esse3ValidationFlag>("/persone/${personId}/validaFoto", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getHighSchoolGradeRange(): List<Esse3HighSchoolGradeRange> {
        return executeJsonGetList<Esse3HighSchoolGradeRange>("/rangeVotiMaturita", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun putExternalSubject(
        body: Esse3PutExternalSubject
    ): List<Esse3ExternalSubject> {
        return executeJsonGetList<Esse3ExternalSubject>("/soggettiEsterni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun deleteExternalSubject(
        externalSubjectId: Int
    ) {
        val response = executeDelete("/soggettiEsterni/${externalSubjectId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun putExternalSubjectConsents(
        externalSubjectId: Int,
        body: List<Esse3ConsentsParameters>,
        webProcedureCode: String,
        iso6392Code: String? = null
    ): List<Esse3ExternalSubjectsConsents> {
        return executeJsonGetList<Esse3ExternalSubjectsConsents>("/soggettiEsterni/${externalSubjectId}/consensi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("procWebCod", webProcedureCode)
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

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

    suspend fun getExternalSubjectReplica(
        externalSubjectId: Int,
        optionalFields: String? = null
    ): Esse3ExternalSubjectReplica {
        return executeJsonGet<Esse3ExternalSubjectReplica>("/soggettiEsterniReplica/${externalSubjectId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getHigherInstitutionTypes(): List<Esse3HigherInstituteTypes> {
        return executeJsonGetList<Esse3HigherInstituteTypes>("/tipiIstituto", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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

    suspend fun getHandicapTypologies(
        lecturerId: Long? = null
    ): List<Esse3HandicapTypesLookup> {
        return executeJsonGetList<Esse3HandicapTypesLookup>("/tipologieHandicap", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            lecturerId?.let { parameter("docenteId", it) }
        }
    }

    suspend fun getHandicapTypologiesToEvaluate(
        lecturerId: Long? = null
    ): List<Esse3HandicapTypesLookup> {
        return executeJsonGetList<Esse3HandicapTypesLookup>("/tipologieHandicapDaValutare", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            lecturerId?.let { parameter("docenteId", it) }
        }
    }

    suspend fun getRelationshipTypologies(): List<Esse3RelationshipTypes> {
        return executeJsonGetList<Esse3RelationshipTypes>("/tipologieParentele", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putTitles(
        body: Esse3TitlesInsertion
    ): Esse3EnrollmentReturn {
        return executeJsonPost<Esse3EnrollmentReturn>("/titoli/import", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

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

    suspend fun getTutorRules(): List<Esse3TutorsRulesHeader> {
        return executeJsonGetList<Esse3TutorsRulesHeader>("/tutori/regoleRichiesta/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
