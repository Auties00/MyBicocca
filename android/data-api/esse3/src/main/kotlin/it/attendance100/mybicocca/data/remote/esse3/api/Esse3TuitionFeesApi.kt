package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CancelInvoiceResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CollectionData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentForTuition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExemptionData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExemptionResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InvoiceAttachmentMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InvoiceInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InvoicePaymentData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Invoices
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MeritData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MessageOutcomeResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MultibenefitPayment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransaction
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransactionResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Payment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Payments
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonDebit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostCollectionResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Refunds
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RejectExemption
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ScholarshipOutcomeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ScholarshipOutcomeResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SpgTransactionStatusData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentDebit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentExemptions
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TrafficLight
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Transaction
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ValidExemptionsAcademicYear
import kotlinx.serialization.json.Json

class Esse3TuitionFeesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/tasse-service-v1") {

    /**
     * Consente di acquisire gli esiti delle borse di studio regionali.
     *
     * @param body Oggetto che contiene i dati della borsa da estudio da inserire.
     */
    suspend fun postAcquireScholarshipOutcomeApplications(
        body: Esse3ScholarshipOutcomeData
    ): List<Esse3ScholarshipOutcomeResponse> {
        return executeJsonPostList<Esse3ScholarshipOutcomeResponse>("/acqDomandeEsitiBorse", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Inserimento metadati esoneri.
     *
     * @param body Oggetto che contiene i dati dell'esonero da inserire.
     */
    suspend fun postAcquireExemptions(
        body: List<Esse3ExemptionData>
    ): List<Esse3ExemptionResponse> {
        return executeJsonPostList<Esse3ExemptionResponse>("/acqEsoneri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * @param personId Codice univoco che consente di individuare una persona.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param invoiceId Identificativo della fattura.
     * @param paidFlag Flag che indica se l'addebito è pagato o meno. (1 = solo tasse pagate - null = tutte le tasse - 0 = tasse non pagate)
     * @param canceledFlag Flag che indica se l'addebito è annullato o meno. (1 = solo tasse annullate - 0 = tasse non non annullate). Il default è 0.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * @param studentId Codice univoco che consente di individuare uno studente.
     * @param externalStudentCode Codice dello studente dell'archivio esterno.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param invoiceId Identificativo della fattura.
     * @param invoiceExpiration Data di scadenza della fattura.
     * @param installmentId Identificativo rata.
     * @param taxTypeCode Tipologia di tassa.
     * @param expiredFlag Flag che indica se l'addebito è scaduto o meno. (1 = solo tasse scadute - null = tutte le tasse - 0 = tasse non scadute).
     * @param paidFlag Flag che indica se l'addebito è pagato o meno. (1 = solo tasse pagate - null = tutte le tasse - 0 = tasse non pagate)
     * @param canceledFlag Flag che indica se l'addebito è annullato o meno. (1 = solo tasse annullate - 0 = tasse non non annullate). Il default è 0.
     * @param taxCode Codice della tassa.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
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

    /**
     * @param fiscalCode Codice fiscale dello studente.
     * @param academicYearEnrollmentId Anno di iscrizione.
     */
    suspend fun getAdisurcMeritScholarships(
        fiscalCode: String,
        academicYearEnrollmentId: Long
    ): Esse3MeritData {
        return executeJsonGet<Esse3MeritData>("/adisurc/datiMerito/${fiscalCode}/annoIscr/${academicYearEnrollmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * inserimento metadati allegato fattura
     *
     * @param invoiceId Identificativo della fattura.
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
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

    /**
     * @param invoiceId Identificativo della fattura che si vuole annullare.
     * @param cancellationType tipo annullamento, i possibili valori sono 1 e 2.
     */
    suspend fun cancelInvoice(
        invoiceId: Long,
        cancellationType: Int
    ): Esse3CancelInvoiceResponse {
        return executeJsonPut<Esse3CancelInvoiceResponse>("/annullaFattura/${invoiceId}/tipoAnnullamento/${cancellationType}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Annulla un pagamento acquisito manualmente.
     *
     * @param invoiceId Identificativo della fattura di cui si vuole chiedere lo stato.
     * @param cancelCall Flag che indica se annullare anche la convalida del pagamento.
     */
    suspend fun putCancelPayment(
        invoiceId: Long,
        cancelCall: Int
    ) {
        val response = executePut("/annullaPagamento/${invoiceId}/annullaConvalida/${cancelCall}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * @param personId Codice univoco che consente di individuare una persona.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * @param academicYearId Identificativo dell'anno accademico.
     * @param exemptionCode Codice esonero.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
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

    /**
     * Respigi esonero.
     *
     * @param body Oggetto che contiene i dati dell'esonero da respingere.
     */
    suspend fun rejectExemption(
        body: Esse3RejectExemption
    ) {
        val response = executePut("/esoneri/respingi") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Consente di creare una fattura associata a un anno accademico e a un studente.
     *
     * @param body Oggetto che contiene gli addebiti da fatturare.
     */
    suspend fun postCreateStudentInvoice(
        body: Esse3InvoiceInsert
    ) {
        val response = executePost("/fattura") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Consente di recuperare i dati di una fattura.
     *
     * @param invoiceId Identificativo della fattura di cui si vuole chiedere lo stato.
     */
    suspend fun getInvoice(
        invoiceId: Long
    ): Esse3Invoices {
        return executeJsonGet<Esse3Invoices>("/fattura/${invoiceId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Inserimento metadati di un incasso.
     *
     * @param body Oggetto che contiene i dati del pagamento da inserire
     */
    suspend fun postCollection(
        body: Esse3CollectionData
    ): Esse3PostCollectionResponse {
        return executeJsonPost<Esse3PostCollectionResponse>("/incassi/inserisciIncasso", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * @param studentId Codice univoco che consente di individuare uno studente.
     * @param courseOfStudyId Identificativo del corso di studio
     * @param academicYearEnrollmentId Identificativo dell'anno d'iscrizione.
     * @param externalStudentCode Codice dello studente dell'archivio esterno.
     * @param modificationDate Data di modifica (Usare il formato dd/mm/yyyy).
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
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

    /**
     * @param personId Codice univoco che consente di individuare una persona.
     * @param invoiceId Identificativo della fattura.
     * @param iUV Identificativo Univoco Versamento.
     * @param noticeCode Codice avviso PagoPA
     * @param expiredFlag Flag che indica se l'addebito è scaduto o meno. (1 = solo tasse scadute - null = tutte le tasse - 0 = tasse non scadute).
     * @param paidFlag Flag che indica se l'addebito è pagato o meno. (1 = solo tasse pagate - null = tutte le tasse - 0 = tasse non pagate)
     * @param canceledInvoice Flag che indica se la fattura è annullata o meno. (1 = solo fatture annullate - 0 = fatture non annullate). Il default è 0.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param retrieveAdditionalInfo Indica se recuperare o meno le infoAggiuntive della fattura. Se true si deve valorizzare il fattId di una fattura.
     */
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

    /**
     * @param personId Codice univoco che consente di individuare una persona.
     * @param invoiceId Identificativo della fattura.
     */
    suspend fun getPaymentsList(
        personId: Long,
        invoiceId: Long? = null
    ): List<Esse3Payments> {
        return executeJsonGetList<Esse3Payments>("/lista-pagamenti/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            invoiceId?.let { parameter("fattId", it) }
        }
    }

    /**
     * @param personId Codice univoco che consente di individuare una persona.
     * @param refundedFlag Flag che indica se considerare le fatture rimborsate, non rimborsate o tutte.
     */
    suspend fun getRefundsList(
        personId: Long,
        refundedFlag: Int? = null
    ): List<Esse3Refunds> {
        return executeJsonGetList<Esse3Refunds>("/lista-rimborsi/${personId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            refundedFlag?.let { parameter("rimborsatoFlg", it) }
        }
    }

    /**
     * Inserimento metadati del pagamenti di una fattura.
     *
     * @param body Oggetto che contiene i dati del pagamento da inserire
     */
    suspend fun postPayInvoice(
        body: Esse3InvoicePaymentData
    ): Esse3MessageOutcomeResponse {
        return executeJsonPost<Esse3MessageOutcomeResponse>("/pagaFattura", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * .
     *
     * @param invoiceId Identificativo della fattura per cui si vuole stampare l'avviso.
     */
    suspend fun putPrintPagoPANotice(
        invoiceId: Long
    ): ByteReadChannel {
        return executeStreamPut("/pagopa/avviso/${invoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Chiede lo stato del versamento e in caso di pagamento eseguito lo aggiorna.
     *
     * @param invoiceId Identificativo della fattura di cui si vuole chiedere lo stato.
     */
    suspend fun putRequestPaymentStatus(
        invoiceId: Long
    ): Esse3Payment {
        return executeJsonPut<Esse3Payment>("/pagopa/chiediStatoVersamento/${invoiceId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * .
     *
     * @param invoiceId Identificativo della fattura per cui si vuole stampare l'avviso.
     * @param language Lingua in cui si vuole stampare la quietanza, 'it' per italiano, 'en' per inglese.
     */
    suspend fun getPagoPAReceipt(
        invoiceId: Long,
        language: String
    ): ByteReadChannel {
        return executeStreamGet("/pagopa/quietanza/${invoiceId}/lingua/${language}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Consente di iniziare una transazione di pagamento immediato pagoPA in Esse3.
     *
     * @param body Oggetto che contiene il fattId che si intende pagare.
     */
    suspend fun postInitPagoPaTransaction(
        body: Esse3PagoPATransaction
    ): Esse3PagoPATransactionResponse {
        return executeJsonPost<Esse3PagoPATransactionResponse>("/pagopa/transaction", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * @param invoiceId Identificativo della fattura.
     * @param iUV Identificativo Univoco Versamento.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param fiscalCode Codice fiscale dello studente.
     * @param finalState Indica se lo stato della transazione è finale.
     * @param lastTransaction Indica se la transazione è l'ultima efettuata.
     */
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

    /**
     * @param entity Ente che interroga il servizio.
     * @param iUV Identificativo Univoco Versamento.
     * @param iur Identificativo univoco di Riscossione.
     * @param paymentDateFrom Data di pagamento da.
     * @param paymentDateTo Data di pagamento A.
     * @param secondaryEntityDomain Codice dominio dell'ente secondario.
     * @param creditIban Iban di accredito dell'ente secondario.
     * @param reportingFlowCode Codice del flusso di rendicontazione pagoPA.
     * @param academicYearDebt Anno del debito.
     */
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

    /**
     * @param invoiceId Identificativo della fattura.
     * @param iUV Identificativo Univoco Versamento.
     */
    suspend fun getPagoPAYPayment(
        invoiceId: Long? = null,
        iUV: String? = null
    ): Esse3Payment {
        return executeJsonGet<Esse3Payment>("/pagopa/versamento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            invoiceId?.let { parameter("fattId", it) }
            iUV?.let { parameter("IUV", it) }
        }
    }

    /**
     * @param studentId Codice univoco che consente di individuare uno studente.
     * @param academicYearEnrollmentId Identificativo dell'anno d'iscrizione.
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * @param studentId Codice univoco che consente di individuare uno studente.
     * @param academicYearId Identificativo dell'anno accademico.
     * @param referenceDate Data di riferimento.
     */
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

    /**
     * Consente di aggiornare lo status di una transazione in Esse3.
     *
     * @param body Oggetto che contiene i dati del pagamento.
     */
    suspend fun postNotifyStatus(
        body: Esse3SpgTransactionStatusData
    ): Esse3MessageOutcomeResponse {
        return executeJsonPost<Esse3MessageOutcomeResponse>("/spg/notifyStatus", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
