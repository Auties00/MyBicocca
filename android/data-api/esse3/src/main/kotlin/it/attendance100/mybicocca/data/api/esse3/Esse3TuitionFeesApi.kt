package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.dto.esse3.Esse3CancelInvoiceResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3CollectionData
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentForTuition
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionData
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoiceAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoiceInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoicePaymentData
import it.attendance100.mybicocca.data.dto.esse3.Esse3Invoices
import it.attendance100.mybicocca.data.dto.esse3.Esse3MeritData
import it.attendance100.mybicocca.data.dto.esse3.Esse3MessageOutcomeResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3MultibenefitPayment
import it.attendance100.mybicocca.data.dto.esse3.Esse3PagoPATransaction
import it.attendance100.mybicocca.data.dto.esse3.Esse3PagoPATransactionResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3Payment
import it.attendance100.mybicocca.data.dto.esse3.Esse3Payments
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonDebit
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostCollectionResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3Refunds
import it.attendance100.mybicocca.data.dto.esse3.Esse3RejectExemption
import it.attendance100.mybicocca.data.dto.esse3.Esse3ScholarshipOutcomeData
import it.attendance100.mybicocca.data.dto.esse3.Esse3ScholarshipOutcomeResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3SelfCertification
import it.attendance100.mybicocca.data.dto.esse3.Esse3SpgTransactionStatusData
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentDebit
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentExemptions
import it.attendance100.mybicocca.data.dto.esse3.Esse3TrafficLight
import it.attendance100.mybicocca.data.dto.esse3.Esse3Transaction
import it.attendance100.mybicocca.data.dto.esse3.Esse3ValidExemptionsAcademicYear
import kotlinx.serialization.json.Json

class Esse3TuitionFeesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/tasse-service-v1") {

    suspend fun postAcquireScholarshipOutcomeApplications(
        body: Esse3ScholarshipOutcomeData
    ): List<Esse3ScholarshipOutcomeResponse> {
        return executeJsonPostList<Esse3ScholarshipOutcomeResponse>("/acqDomandeEsitiBorse", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postAcquireExemptions(
        body: List<Esse3ExemptionData>
    ): List<Esse3ExemptionResponse> {
        return executeJsonPostList<Esse3ExemptionResponse>("/acqEsoneri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getPersonChargesList(
        personId: Long,
        academicYearId: Long? = null,
        invoiceId: Long? = null,
        paidFlag: Int? = null,
        canceledFlag: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3PersonDebit> {
        return executeJsonGetList<Esse3PersonDebit>("/addebiti-persona/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)) {
            academicYearId?.let { parameter("aaId", it) }
            invoiceId?.let { parameter("fattId", it) }
            paidFlag?.let { parameter("pagatoFlg", it) }
            canceledFlag?.let { parameter("annullataFlg", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getStudentChargesList(
        studentId: Long? = null,
        externalStudentCode: String? = null,
        academicYearId: Long? = null,
        invoiceId: Long? = null,
        invoiceExpiration: String? = null,
        installmentId: Long? = null,
        taxTypeCode: String? = null,
        expiredFlag: Int? = null,
        paidFlag: Int? = null,
        canceledFlag: Int? = null,
        taxCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3StudentDebit> {
        return executeJsonGetList<Esse3StudentDebit>("/addebiti-studente", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT)) {
            studentId?.let { parameter("stuId", it) }
            externalStudentCode?.let { parameter("extStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            invoiceId?.let { parameter("fattId", it) }
            invoiceExpiration?.let { parameter("scadFattura", it) }
            installmentId?.let { parameter("rataId", it) }
            taxTypeCode?.let { parameter("tipoTaxCod", it) }
            expiredFlag?.let { parameter("scadutoFlg", it) }
            paidFlag?.let { parameter("pagatoFlg", it) }
            canceledFlag?.let { parameter("annullataFlg", it) }
            taxCode?.let { parameter("tassaCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getAdisurcMeritScholarships(
        fiscalCode: String,
        academicYearEnrollmentId: Long
    ): Esse3MeritData {
        return executeJsonGet<Esse3MeritData>("/adisurc/datiMerito/${fiscalCode}/annoIscr/${academicYearEnrollmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postInvoiceAttachmentMetadata(
        invoiceId: Long,
        body: Esse3InvoiceAttachmentMetadata
    ) {
        val response = executePost("/allegati/${invoiceId}/allegatiFattura/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun cancelInvoice(
        invoiceId: Long,
        cancellationType: Int
    ): Esse3CancelInvoiceResponse {
        return executeJsonPut<Esse3CancelInvoiceResponse>("/annullaFattura/${invoiceId}/tipoAnnullamento/${cancellationType}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putCancelPayment(
        invoiceId: Long,
        cancelCall: Int
    ) {
        val response = executePut("/annullaPagamento/${invoiceId}/annullaConvalida/${cancelCall}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getSelfCertification(
        personId: Long,
        academicYearId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3SelfCertification> {
        return executeJsonGetList<Esse3SelfCertification>("/autocert/${personId}/annoAcc/${academicYearId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getValidExemptionsAcademicYear(
        academicYearId: Long,
        exemptionCode: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ValidExemptionsAcademicYear> {
        return executeJsonGetList<Esse3ValidExemptionsAcademicYear>("/esoneri-anno-accademico/${academicYearId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            exemptionCode?.let { parameter("esoneroCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun rejectExemption(
        body: Esse3RejectExemption
    ) {
        val response = executePut("/esoneri/respingi") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postCreateStudentInvoice(
        body: Esse3InvoiceInsert
    ) {
        val response = executePost("/fattura") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getInvoice(
        invoiceId: Long
    ): Esse3Invoices {
        return executeJsonGet<Esse3Invoices>("/fattura/${invoiceId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postCollection(
        body: Esse3CollectionData
    ): Esse3PostCollectionResponse {
        return executeJsonPost<Esse3PostCollectionResponse>("/incassi/inserisciIncasso", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getStudentExemptionsList(
        studentId: Long? = null,
        courseOfStudyId: Long? = null,
        academicYearEnrollmentId: Long? = null,
        externalStudentCode: String? = null,
        modificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3StudentExemptions> {
        return executeJsonGetList<Esse3StudentExemptions>("/lista-esoneri-studente", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            studentId?.let { parameter("stuId", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            externalStudentCode?.let { parameter("extStuCod", it) }
            modificationDate?.let { parameter("dataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getInvoicesList(
        personId: Long? = null,
        invoiceId: Long? = null,
        iUV: String? = null,
        noticeCode: String? = null,
        expiredFlag: Int? = null,
        paidFlag: Int? = null,
        canceledInvoice: Int? = null,
        academicYearId: Long? = null,
        retrieveAdditionalInfo: Boolean? = null
    ): List<Esse3Invoices> {
        return executeJsonGetList<Esse3Invoices>("/lista-fatture", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.ANY)) {
            personId?.let { parameter("persId", it) }
            invoiceId?.let { parameter("fattId", it) }
            iUV?.let { parameter("IUV", it) }
            noticeCode?.let { parameter("codiceAvviso", it) }
            expiredFlag?.let { parameter("scadutoFlg", it) }
            paidFlag?.let { parameter("pagatoFlg", it) }
            canceledInvoice?.let { parameter("fattAnnullata", it) }
            academicYearId?.let { parameter("aaId", it) }
            retrieveAdditionalInfo?.let { parameter("retrieveInfoAggiuntive", it) }
        }
    }

    suspend fun getPaymentsList(
        personId: Long,
        invoiceId: Long? = null
    ): List<Esse3Payments> {
        return executeJsonGetList<Esse3Payments>("/lista-pagamenti/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            invoiceId?.let { parameter("fattId", it) }
        }
    }

    suspend fun getRefundsList(
        personId: Long,
        refundedFlag: Int? = null
    ): List<Esse3Refunds> {
        return executeJsonGetList<Esse3Refunds>("/lista-rimborsi/${personId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            refundedFlag?.let { parameter("rimborsatoFlg", it) }
        }
    }

    suspend fun postPayInvoice(
        body: Esse3InvoicePaymentData
    ): Esse3MessageOutcomeResponse {
        return executeJsonPost<Esse3MessageOutcomeResponse>("/pagaFattura", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putPrintPagoPANotice(
        invoiceId: Long
    ): ByteReadChannel {
        return executeStreamPut("/pagopa/avviso/${invoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putRequestPaymentStatus(
        invoiceId: Long
    ): Esse3Payment {
        return executeJsonPut<Esse3Payment>("/pagopa/chiediStatoVersamento/${invoiceId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getPagoPAReceipt(
        invoiceId: Long,
        language: String
    ): ByteReadChannel {
        return executeStreamGet("/pagopa/quietanza/${invoiceId}/lingua/${language}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postInitPagoPaTransaction(
        body: Esse3PagoPATransaction
    ): Esse3PagoPATransactionResponse {
        return executeJsonPost<Esse3PagoPATransactionResponse>("/pagopa/transaction", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getPagoPATransactions(
        invoiceId: Long? = null,
        iUV: String? = null,
        academicYearId: Long? = null,
        fiscalCode: String? = null,
        finalState: Int? = null,
        lastTransaction: Int? = null
    ): List<Esse3Transaction> {
        return executeJsonGetList<Esse3Transaction>("/pagopa/transazioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)) {
            invoiceId?.let { parameter("fattId", it) }
            iUV?.let { parameter("IUV", it) }
            academicYearId?.let { parameter("aaId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            finalState?.let { parameter("statoFinale", it) }
            lastTransaction?.let { parameter("lastTrans", it) }
        }
    }

    suspend fun getMultibenPayments(
        entity: String,
        iUV: String? = null,
        iur: String? = null,
        paymentDateFrom: String? = null,
        paymentDateTo: String? = null,
        secondaryEntityDomain: String? = null,
        creditIban: String? = null,
        reportingFlowCode: String? = null,
        academicYearDebt: Long? = null
    ): List<Esse3MultibenefitPayment> {
        return executeJsonGetList<Esse3MultibenefitPayment>("/pagopa/versamenti/multiben/ente/${entity}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            iUV?.let { parameter("IUV", it) }
            iur?.let { parameter("iur", it) }
            paymentDateFrom?.let { parameter("dataPagamentoDa", it) }
            paymentDateTo?.let { parameter("dataPagamentoA", it) }
            secondaryEntityDomain?.let { parameter("dominioEnteSecondario", it) }
            creditIban?.let { parameter("ibanAccredito", it) }
            reportingFlowCode?.let { parameter("codFlussoRendicontazione", it) }
            academicYearDebt?.let { parameter("aaDebito", it) }
        }
    }

    suspend fun getPagoPAYPayment(
        invoiceId: Long? = null,
        iUV: String? = null
    ): Esse3Payment {
        return executeJsonGet<Esse3Payment>("/pagopa/versamento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            invoiceId?.let { parameter("fattId", it) }
            iUV?.let { parameter("IUV", it) }
        }
    }

    suspend fun getEnrollmentsForTaxes(
        studentId: Long,
        academicYearEnrollmentId: Long? = null,
        order: String? = null
    ): List<Esse3EnrollmentForTuition> {
        return executeJsonGetList<Esse3EnrollmentForTuition>("/parametri-iscrizioni-per-tasse/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT)) {
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getTrafficLightParameters(
        studentId: Long,
        academicYearId: Long? = null,
        referenceDate: String? = null
    ): Esse3TrafficLight {
        return executeJsonGet<Esse3TrafficLight>("/semaforo/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    suspend fun postNotifyStatus(
        body: Esse3SpgTransactionStatusData
    ): Esse3MessageOutcomeResponse {
        return executeJsonPost<Esse3MessageOutcomeResponse>("/spg/notifyStatus", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
