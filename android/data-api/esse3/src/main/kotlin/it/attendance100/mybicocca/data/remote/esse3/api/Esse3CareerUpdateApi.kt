package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AttendanceProcedureParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerUpdateHeader
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerUpdateLog
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerUpdateRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ProcedureUpdateActivityOfferParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RemoveAttendanceProcedureParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SegmentProcedureParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SubstitutionProcedureParameters
import kotlinx.serialization.json.Json

class Esse3CareerUpdateApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/aggcarr-service-v1") {

    /**
     * lista preview aggiorna carriera
     */
    suspend fun getCareerUpdateHeaders(): List<Esse3CareerUpdateHeader> {
        return executeJsonGetList<Esse3CareerUpdateHeader>("/aggcarr", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione massiva AGG_AD_OFF
     *
     * @param body Parametri per la procedura AGG_AD_OFF
     */
    suspend fun postAddTeachingActivityOffer(
        body: Esse3ProcedureUpdateActivityOfferParameters
    ) {
        val response = executePost("/aggcarr/AGG_AD_OFF/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione puntuale AGG_AD_OFF
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Parametri per la procedura AGG_AD_OFF
     */
    suspend fun putAddTeachingActivityOfferByStudent(
        matId: Long,
        body: Esse3ProcedureUpdateActivityOfferParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/AGG_AD_OFF/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * invocazione massiva FREQ
     *
     * @param body Parametri per la procedura FREQ
     */
    suspend fun postAttendance(
        body: Esse3AttendanceProcedureParameters
    ) {
        val response = executePost("/aggcarr/FREQ/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione puntuale FREQ
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Parametri per la procedura FREQ
     */
    suspend fun putAttendanceByStudent(
        matId: Long,
        body: Esse3AttendanceProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/FREQ/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * invocazione massiva RIMUOVI_FREQ
     *
     * @param body Parametri per la procedura RIMUOVI_FREQ
     */
    suspend fun postRemoveAttendance(
        body: Esse3RemoveAttendanceProcedureParameters
    ) {
        val response = executePost("/aggcarr/RIMUOVI_FREQ/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione puntuale RIMUOVI_FREQ
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Parametri per la procedura RIMUOVI_FREQ
     */
    suspend fun putRemoveAttendanceByStudent(
        matId: Long,
        body: Esse3RemoveAttendanceProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/RIMUOVI_FREQ/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * invocazione massiva SEG
     *
     * @param body Parametri per la procedura SEG
     */
    suspend fun postSEG(
        body: Esse3SegmentProcedureParameters
    ) {
        val response = executePost("/aggcarr/SEG/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione puntuale SEG
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Parametri per la procedura SEG
     */
    suspend fun putSEGByStudent(
        matId: Long,
        body: Esse3SegmentProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/SEG/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * invocazione massiva SOST
     *
     * @param body Parametri per la procedura SOST
     */
    suspend fun postSubstitution(
        body: Esse3SubstitutionProcedureParameters
    ) {
        val response = executePost("/aggcarr/SOST/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * invocazione puntuale SOST
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Parametri per la procedura FREQ
     */
    suspend fun putSubstitutionByStudent(
        matId: Long,
        body: Esse3SubstitutionProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/SOST/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * informazioni di testata preview
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     */
    suspend fun getCareerUpdateHeader(
        careerUpdateId: Long
    ): Esse3CareerUpdateHeader {
        return executeJsonGet<Esse3CareerUpdateHeader>("/aggcarr/${careerUpdateId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * cancella la preview
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     */
    suspend fun deletePreview(
        careerUpdateId: Long
    ) {
        val response = executeDelete("/aggcarr/${careerUpdateId}/")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * lista righe della preview
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getPreviewDetails(
        careerUpdateId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CareerUpdateRow> {
        return executeJsonGetList<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * singola riga della preview
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     * @param careerUpdateDetailId id della singola riga della preview
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getPreviewDetail(
        careerUpdateId: Long,
        careerUpdateDetailId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3CareerUpdateRow {
        return executeJsonGet<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli/${careerUpdateDetailId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * imposta il flag elabora su una riga
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     * @param careerUpdateDetailId id della singola riga della preview
     * @param processNow elaborazione della riga da parte del motore
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun setProcessingDetailFlag(
        careerUpdateId: Long,
        careerUpdateDetailId: Long,
        processNow: Boolean,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3CareerUpdateRow {
        return executeJsonPut<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli/${careerUpdateDetailId}/elabora", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            setBody(FormDataContent(parameters {
                append("elabora", processNow.toString())
            }))
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * esecuzione aggiorna carriere sulla preview
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     */
    suspend fun executeUpdateCareers(
        careerUpdateId: Long
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/${careerUpdateId}/esegui", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * log esecuzione aggiorna carriere
     *
     * @param careerUpdateId id generato da una delle procedure di setup che defisce la lista di entità da  processare
     */
    suspend fun getUpdateCareersLog(
        careerUpdateId: Long
    ): List<Esse3CareerUpdateLog> {
        return executeJsonGetList<Esse3CareerUpdateLog>("/aggcarr/${careerUpdateId}/log", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
