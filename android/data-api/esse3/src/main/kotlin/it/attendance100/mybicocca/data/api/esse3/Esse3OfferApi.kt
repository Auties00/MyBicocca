package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivitiesCountPlans
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivitiesCountPlansFilters
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityDeletable
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityParentGroup
import it.attendance100.mybicocca.data.dto.esse3.Esse3ContextualizedActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3ContextualizedSegment
import it.attendance100.mybicocca.data.dto.esse3.Esse3ContextualizedTeachingUnit
import it.attendance100.mybicocca.data.dto.esse3.Esse3DeletedOffer
import it.attendance100.mybicocca.data.dto.esse3.Esse3FullPartitions
import it.attendance100.mybicocca.data.dto.esse3.Esse3GenericActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3Offer
import it.attendance100.mybicocca.data.dto.esse3.Esse3OfferActivityDeletable
import it.attendance100.mybicocca.data.dto.esse3.Esse3OfferTeachingUnitDeletable
import it.attendance100.mybicocca.data.dto.esse3.Esse3OfferWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3PartitionFactor
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachersPerTeachingUnit
import it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateContextualizedActivity
import kotlinx.serialization.json.Json

class Esse3OfferApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/offerta-service-v1") {

    suspend fun getGroupedTeachingActivities(
        cohortYear: Int,
        courseOfStudyCode: String? = null,
        activityCode: String? = null
    ): List<Esse3ActivityParentGroup> {
        return executeJsonGetList<Esse3ActivityParentGroup>("/ad-raggruppate", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("annoCoorte", cohortYear)
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            activityCode?.let { parameter("adCod", it) }
        }
    }

    suspend fun getGenericTeachingActivities(
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3GenericActivity> {
        return executeJsonGetList<Esse3GenericActivity>("/attivitaGeneriche", setOf(Esse3PermissionLevel.ANY)) {
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getPartialDomicile(
        invoicePartialCode: String? = null,
        invoicePartialDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3FullPartitions> {
        return executeJsonGetList<Esse3FullPartitions>("/dominiPartizione", setOf(Esse3PermissionLevel.ANY)) {
            invoicePartialCode?.let { parameter("fatPartCod", it) }
            invoicePartialDescription?.let { parameter("fatPartDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getInvoicePartial(
        invoicePartialCode: String? = null,
        invoicePartialDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3PartitionFactor> {
        return executeJsonGetList<Esse3PartitionFactor>("/fattoriPartizione", setOf(Esse3PermissionLevel.ANY)) {
            invoicePartialCode?.let { parameter("fatPartCod", it) }
            invoicePartialDescription?.let { parameter("fatPartDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getOffers(
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        departmentCode: String? = null,
        departmentDescription: String? = null,
        activityStateCode: String? = null,
        courseTypesCode: String? = null,
        odModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3Offer> {
        return executeJsonGetList<Esse3Offer>("/offerte", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            departmentCode?.let { parameter("dipCod", it) }
            departmentDescription?.let { parameter("dipDes", it) }
            activityStateCode?.let { parameter("statoAttCod", it) }
            courseTypesCode?.let { parameter("tipiCorsoCod", it) }
            odModificationDate?.let { parameter("dataModOd", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCancellableOfferTeachingActivity(
        activityFunctionId: Long? = null,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanCode: String? = null,
        activityCode: String? = null,
        teachingUnitCode: String? = null
    ): Esse3ActivityDeletable {
        return executeJsonGet<Esse3ActivityDeletable>("/offerte/attivita/cancellabile", setOf(Esse3PermissionLevel.ANY)) {
            activityFunctionId?.let { parameter("afId", it) }
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            activityCode?.let { parameter("adCod", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
        }
    }

    suspend fun putActivityPlanCount(
        body: Esse3ActivitiesCountPlansFilters
    ): Esse3ActivitiesCountPlans {
        return executeJsonPut<Esse3ActivitiesCountPlans>("/offerte/attivita/conteggio-piani", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getTeachingActivityOffers(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ContextualizedActivity> {
        return executeJsonGetList<Esse3ContextualizedActivity>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/attivita", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCancellableTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3OfferActivityDeletable> {
        return executeJsonGetList<Esse3OfferActivityDeletable>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/attivita/cancellabile", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun patchContextualizedTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderOfferId: Long,
        studyPlanOfferId: Long,
        activityOfferId: Long,
        body: Esse3UpdateContextualizedActivity
    ): Esse3ContextualizedActivity {
        return executeJsonPatch<Esse3ContextualizedActivity>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/attivita/${academicYearOrderOfferId}/${studyPlanOfferId}/${activityOfferId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getLecturersByTeachingUnit(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingUnitCode: String? = null,
        teachingUnitDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        order: String? = null
    ): List<Esse3TeachersPerTeachingUnit> {
        return executeJsonGetList<Esse3TeachersPerTeachingUnit>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/docentiPerUD", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            teachingUnitDescription?.let { parameter("udDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getTeachingUnitOffers(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ContextualizedTeachingUnit> {
        return executeJsonGetList<Esse3ContextualizedTeachingUnit>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/moduli", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCancellableTeachingUnit(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        teachingUnitId: Long? = null,
        teachingUnitCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3OfferTeachingUnitDeletable> {
        return executeJsonGetList<Esse3OfferTeachingUnitDeletable>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/moduli/cancellabili", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            teachingUnitId?.let { parameter("udId", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getOfferedSEG(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingUnitId: Long? = null,
        teachingUnitCode: String? = null,
        teachingUnitDescription: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ContextualizedSegment> {
        return executeJsonGetList<Esse3ContextualizedSegment>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/segmenti", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingUnitId?.let { parameter("udId", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            teachingUnitDescription?.let { parameter("udDes", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getContextualizedTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderOfferId: Long,
        studyPlanOfferId: Long,
        activityOfferId: Long
    ): Esse3ContextualizedActivity {
        return executeJsonGet<Esse3ContextualizedActivity>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/${academicYearOrderOfferId}/${studyPlanOfferId}/${activityOfferId}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getDeletedOffers(
        odModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3DeletedOffer> {
        return executeJsonGetList<Esse3DeletedOffer>("/offerteEliminate", setOf(Esse3PermissionLevel.ANY)) {
            odModificationDate?.let { parameter("dataModOd", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getFullOffers(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        fields: String? = null
    ): Esse3OfferWithDetails {
        return executeJsonGet<Esse3OfferWithDetails>("/offerteFull/${academicYearOfferId}/${courseOfStudyOfferId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }
}
