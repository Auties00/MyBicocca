package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivitiesCountPlans
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivitiesCountPlansFilters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivityDeletable
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivityParentGroup
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ContextualizedActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ContextualizedSegment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ContextualizedTeachingUnit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3DeletedOffer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3FullPartitions
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GenericActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Offer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3OfferActivityDeletable
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3OfferTeachingUnitDeletable
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3OfferWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PartitionFactor
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionStateCode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachersPerTeachingUnit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UpdateContextualizedActivity
import kotlinx.serialization.json.Json

class Esse3OfferApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/offerta-service-v1") {

    /**
     * Recupera i raggruppamenti delle AD.
     *
     * @param cohortYear anno coorte
     * @param courseOfStudyCode codice del corso di studio
     * @param activityCode codice dell'attivita didattica
     */
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

    /**
     * Recupera le informazioni delle attivita didattiche generiche. I parametri opzionali filtrano una AD qualsiasi.
     *
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni dei domini di partizione. I parametri opzionali filtrano una AD qualsiasi.
     *
     * @param invoicePartialCode codice del fattore di partizione
     * @param invoicePartialDescription descrizione del fattore di partizione (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni dei fattori di partizione. I parametri opzionali filtrano una AD qualsiasi.
     *
     * @param invoicePartialCode codice del fattore di partizione
     * @param invoicePartialDescription descrizione del fattore di partizione (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni delle testate delle offerte didattiche. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param departmentCode codice del dipartimento di afferenza amministrativa del corso di studio
     * @param departmentDescription descrizione del dipartimento di afferenza amministrativa del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityStateCode stato del'offerta, se non valorizzato vengono recuperate tutte le offerte
     * @param courseTypesCode codice del tipo di corso di studio
     * @param odModificationDate data di ultima modifica dell'offerta didattica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getOffers(
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        departmentCode: String? = null,
        departmentDescription: String? = null,
        activityStateCode: Esse3QuestionStateCode? = null,
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
            activityStateCode?.let { parameter("statoAttCod", it.value) }
            courseTypesCode?.let { parameter("tipiCorsoCod", it) }
            odModificationDate?.let { parameter("dataModOd", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Indica se una AD risulta cancellabile.
     *
     * @param activityFunctionId af_id di U-Gov
     * @param academicYearOfferId identificativo dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanCode codice del percorso di studio
     * @param activityCode codice dell'attivita didattica
     * @param teachingUnitCode codice dell'unità didattica
     */
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

    /**
     * informazioni sui conteggi piani collegati a una AD generica
     *
     * @param body Oggetto con i dati per gestione filtri AD
     */
    suspend fun putActivityPlanCount(
        body: Esse3ActivitiesCountPlansFilters
    ): Esse3ActivitiesCountPlans {
        return executeJsonPut<Esse3ActivitiesCountPlans>("/offerte/attivita/conteggio-piani", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera le informazioni delle attività. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera un intero che indica se la AD è cancellabile o no.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Modifica una attività didattica contestualizzata
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderOfferId id dell'ordinamento del corso di studio
     * @param studyPlanOfferId id del percorso di studio
     * @param activityOfferId id dell'attività didattica
     * @param body Oggetto che contiene i parametri per la modifica dell'attività  didattica contestualizzata
     */
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

    /**
     * Recupera le informazioni dei docenti collegati alla UD. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param teachingUnitCode codice dell'unità didattica
     * @param teachingUnitDescription descrizione dell'unità didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente
     * @param lecturerSurname cognome del docente (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerName nome del docente (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni dei moduli. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera un intero che indica se la AD è cancellabile o no.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param teachingUnitId id dell'unità didattica
     * @param teachingUnitCode codice dell'unità didattica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni dei segmenti. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param teachingUnitId id dell'unità didattica
     * @param teachingUnitCode codice dell'unità didattica
     * @param teachingUnitDescription descrizione dell'unità didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni di una una attività didattica contestualizzata.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderOfferId id dell'ordinamento del corso di studio
     * @param studyPlanOfferId id del percorso di studio
     * @param activityOfferId id dell'attività didattica
     */
    suspend fun getContextualizedTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        academicYearOrderOfferId: Long,
        studyPlanOfferId: Long,
        activityOfferId: Long
    ): Esse3ContextualizedActivity {
        return executeJsonGet<Esse3ContextualizedActivity>("/offerte/${academicYearOfferId}/${courseOfStudyOfferId}/${academicYearOrderOfferId}/${studyPlanOfferId}/${activityOfferId}", setOf(Esse3PermissionLevel.ANY))
    }

    /**
     * Recupera le informazioni relative alle offerte eliminate con la eventuale data di eliminazione. I parametri opzionali filtrano una o più offerta eliminata.
     *
     * @param odModificationDate data di ultima modifica dell'offerta didattica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupera le informazioni delle offerte didattiche complete. I parametri opzionali filtrano una UD qualsiasi dell'offerta.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
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
