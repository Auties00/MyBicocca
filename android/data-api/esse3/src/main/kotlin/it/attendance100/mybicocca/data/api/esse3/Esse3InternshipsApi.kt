package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyAgreementsData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyContactData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyLocationsData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPostInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPostOutput
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPutInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3GenericAttachmentInsertMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationAttachments
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationDetail
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationQuestionnaireData
import it.attendance100.mybicocca.data.dto.esse3.Esse3OpportunityData
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentInternshipEligibilityData
import it.attendance100.mybicocca.data.dto.esse3.Esse3TrainingProject
import kotlinx.serialization.json.Json

class Esse3InternshipsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/tirocini-service-v1") {

    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/tirocini", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun saveCompany(
        body: Esse3CompanyPostInput
    ): Esse3CompanyPostOutput {
        return executeJsonPost<Esse3CompanyPostOutput>("/tirocini/azienda", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun updateCompany(
        companyId: Long,
        body: Esse3CompanyPutInput
    ) {
        val response = executePut("/tirocini/azienda/${companyId}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun deleteCompany(
        companyId: Long
    ) {
        val response = executeDelete("/tirocini/azienda/${companyId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getCompanyContacts(
        companyId: Long,
        surname: String? = null,
        name: String? = null,
        matFiscalCode: String? = null,
        activeFlag: Int? = null,
        role: String? = null
    ): List<Esse3CompanyContactData> {
        return executeJsonGetList<Esse3CompanyContactData>("/tirocini/azienda/${companyId}/contatti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            matFiscalCode?.let { parameter("matCodfis", it) }
            activeFlag?.let { parameter("attivoFlg", it) }
            role?.let { parameter("ruolo", it) }
        }
    }

    suspend fun getCompanyConventions(
        companyId: Long,
        conventionSiteDescription: String? = null,
        conventionStateCode: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        durationYears: Int? = null,
        academicYearId: Int? = null,
        defaultFlag: Int? = null
    ): List<Esse3CompanyAgreementsData> {
        return executeJsonGetList<Esse3CompanyAgreementsData>("/tirocini/azienda/${companyId}/convenzioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            conventionSiteDescription?.let { parameter("sdrCnvzDes", it) }
            conventionStateCode?.let { parameter("statoCnvzCod", it) }
            startDate?.let { parameter("dataInizio", it) }
            endDate?.let { parameter("dataFine", it) }
            durationYears?.let { parameter("durataAnni", it) }
            academicYearId?.let { parameter("aaId", it) }
            defaultFlag?.let { parameter("defaultFlg", it) }
        }
    }

    suspend fun getCompanySites(
        companyId: Long,
        companySiteDescription: String? = null,
        siteTypeCode: String? = null,
        city: String? = null,
        nationId: Long? = null,
        deactivate: Int? = null
    ): List<Esse3CompanyLocationsData> {
        return executeJsonGetList<Esse3CompanyLocationsData>("/tirocini/azienda/${companyId}/sedi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            companySiteDescription?.let { parameter("sedeAziendaDes", it) }
            siteTypeCode?.let { parameter("tipoSedeCod", it) }
            city?.let { parameter("citta", it) }
            nationId?.let { parameter("nazioneId", it) }
            deactivate?.let { parameter("disattiva", it) }
        }
    }

    suspend fun getInternshipCompanies(
        companyCode: String? = null,
        company: String? = null,
        fiscalCode: String? = null,
        vatNumber: String? = null,
        groupVatNumber: String? = null,
        companyTypeCode: String? = null,
        companyStateCode: String? = null,
        duns: String? = null,
        hasValidConvention: Int? = null,
        hasValidOpportunity: Int? = null
    ): List<Esse3CompanyData> {
        return executeJsonGetList<Esse3CompanyData>("/tirocini/aziende", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            companyCode?.let { parameter("aziendaCod", it) }
            company?.let { parameter("azienda", it) }
            fiscalCode?.let { parameter("cf", it) }
            vatNumber?.let { parameter("piva", it) }
            groupVatNumber?.let { parameter("pivaGruppo", it) }
            companyTypeCode?.let { parameter("tipoAziendaCod", it) }
            companyStateCode?.let { parameter("statoAziendaCod", it) }
            duns?.let { parameter("duns", it) }
            hasValidConvention?.let { parameter("hasValidCnvz", it) }
            hasValidOpportunity?.let { parameter("hasValidOpportunita", it) }
        }
    }

    suspend fun checkStudentInternshipEligibility(
        fiscalCode: String,
        languageCode: List<String>,
        matricola: String? = null,
        serviceType: List<String>? = null,
        internshipStartDate: String? = null
    ): List<Esse3StudentInternshipEligibilityData> {
        return executeJsonGetList<Esse3StudentInternshipEligibilityData>("/tirocini/checkEligibilitaStageStu", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            parameter("codFisc", fiscalCode)
            serviceType?.let { parameter("tipoServizio", it) }
            internshipStartDate?.let { parameter("dataIniTiro", it) }
            parameter("codLingua", languageCode)
        }
    }

    suspend fun importInternship(
        body: Esse3TrainingProject
    ) {
        val response = executePut("/tirocini/import") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getInternshipOpportunities(
        area: String? = null,
        disciplinaryAreaId: Long? = null,
        geographicAreaCode: String? = null,
        company: String? = null,
        campaignId: Long? = null,
        atecoCategoryId: Long? = null,
        protectedCategoryFlag: Int? = null,
        enrollmentEndDateTo: String? = null,
        enrollmentEndDateFrom: String? = null,
        enrollmentStartDateTo: String? = null,
        enrollmentStartDateFrom: String? = null,
        description: String? = null,
        entityId: Long? = null,
        excludeOpportunityCampaign: Int? = null,
        nation: Int? = null,
        nationId: Long? = null,
        provinceCode: String? = null,
        requiredCodeFlag: Int? = null,
        requiredObjective: String? = null,
        sectorDisciplinaryAreaId: Long? = null,
        atecoSectorId: Long? = null,
        sector: Long? = null,
        text: String? = null,
        internshipTypeCode: String? = null,
        title: String? = null,
        expiredOpportunitiesVisible: Int? = null,
        durationFrom: Int? = null,
        durationTo: Int? = null,
        internshipStartDateFrom: String? = null,
        internshipStartDateTo: String? = null
    ): List<Esse3OpportunityData> {
        return executeJsonGetList<Esse3OpportunityData>("/tirocini/opportunita", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            area?.let { parameter("area", it) }
            disciplinaryAreaId?.let { parameter("areaDiscId", it) }
            geographicAreaCode?.let { parameter("areaGeograficaCod", it) }
            company?.let { parameter("azienda", it) }
            campaignId?.let { parameter("campagnaId", it) }
            atecoCategoryId?.let { parameter("catAtecoId", it) }
            protectedCategoryFlag?.let { parameter("catProtettaFlg", it) }
            enrollmentEndDateTo?.let { parameter("dataFinIscrA", it) }
            enrollmentEndDateFrom?.let { parameter("dataFinIscrDa", it) }
            enrollmentStartDateTo?.let { parameter("dataIniIscrA", it) }
            enrollmentStartDateFrom?.let { parameter("dataIniIscrDa", it) }
            description?.let { parameter("descr", it) }
            entityId?.let { parameter("enteId", it) }
            excludeOpportunityCampaign?.let { parameter("esclOppCamp", it) }
            nation?.let { parameter("nazione", it) }
            nationId?.let { parameter("nazioneId", it) }
            provinceCode?.let { parameter("provCod", it) }
            requiredCodeFlag?.let { parameter("reqCodFlg", it) }
            requiredObjective?.let { parameter("reqObiett", it) }
            sectorDisciplinaryAreaId?.let { parameter("settAreaDiscId", it) }
            atecoSectorId?.let { parameter("settAtecoId", it) }
            sector?.let { parameter("settore", it) }
            text?.let { parameter("testo", it) }
            internshipTypeCode?.let { parameter("tipoTirocCod", it) }
            title?.let { parameter("title", it) }
            expiredOpportunitiesVisible?.let { parameter("visOppScadute", it) }
            durationFrom?.let { parameter("durataDa", it) }
            durationTo?.let { parameter("durataA", it) }
            internshipStartDateFrom?.let { parameter("dataIniTiroDa", it) }
            internshipStartDateTo?.let { parameter("dataIniTiroA", it) }
        }
    }

    suspend fun postInternshipApplicationAttachmentMetadata(
        studentId: Long,
        body: Esse3GenericAttachmentInsertMetadata
    ) {
        val response = executePost("/tirocini/${studentId}/allegati/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getAttachmentContent(
        studentId: Long,
        attachmentId: Long
    ): String {
        return executeJsonGet<String>("/tirocini/${studentId}/allegati/${attachmentId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getStudentInternshipApplicationHeaders(
        studentId: Long,
        internshipApplicationStateCode: List<String>? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3InternshipApplicationHeader> {
        return executeJsonGetList<Esse3InternshipApplicationHeader>("/tirocini/${studentId}/domande", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            internshipApplicationStateCode?.let { parameter("statoDomTiroCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getStudentInternshipApplication(
        studentId: Long,
        domicileInternshipId: Long,
        fields: String? = null
    ): Esse3InternshipApplicationDetail {
        return executeJsonGet<Esse3InternshipApplicationDetail>("/tirocini/${studentId}/domande/${domicileInternshipId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getStudentInternshipApplicationAttachments(
        studentId: Long,
        domicileInternshipId: Long,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3InternshipApplicationAttachments> {
        return executeJsonGetList<Esse3InternshipApplicationAttachments>("/tirocini/${studentId}/domande/${domicileInternshipId}/allegati", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getStudentInternshipEvaluation(
        studentId: Long,
        domicileInternshipId: Long,
        questionTypeCode: List<String>? = null,
        order: String? = null
    ): List<Esse3InternshipApplicationQuestionnaireData> {
        return executeJsonGetList<Esse3InternshipApplicationQuestionnaireData>("/tirocini/${studentId}/valutazioni/${domicileInternshipId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            questionTypeCode?.let { parameter("tipoQuestCod", it) }
            order?.let { parameter("order", it) }
        }
    }
}
