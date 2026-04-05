package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3TransactionLogSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3TransactionLogSessionBody
import it.attendance100.mybicocca.data.dto.esse3.Esse3TransactionLogViewParameters
import kotlinx.serialization.json.Json

class Esse3LoggingApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/logging-service-v1") {

    /**
     * Recupera sessioni in override
     */
    suspend fun getTlogSessions(): List<Esse3TransactionLogSession> {
        return executeJsonGetList<Esse3TransactionLogSession>("/tlog/sessions", setOf(Esse3PermissionLevel.AUTHENTICATED_USER))
    }

    /**
     * Aggiunge una nuova sessione in override oppure la modifica, nel caso esista già
     *
     * @param sessionId Id della sessione
     * @param body Oggetto che contiene i dati da inserire nella tlog_session
     */
    suspend fun putTlogSessions(
        sessionId: String,
        body: Esse3TransactionLogSessionBody
    ): Esse3TransactionLogSession {
        return executeJsonPut<Esse3TransactionLogSession>("/tlog/sessions/${sessionId}", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera i log delle procedure
     *
     * @param sessionId Id della sessione
     * @param contextParamsId Id del ctx
     * @param transactionId Id della transazion
     * @param startDate data inizio log
     * @param endDate data fine log
     */
    suspend fun getTlogText(
        sessionId: String? = null,
        contextParamsId: Long? = null,
        transactionId: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): String {
        return executeJsonGet<String>("/tlog/text", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            sessionId?.let { parameter("sessionId", it) }
            contextParamsId?.let { parameter("ctxParamsId", it) }
            transactionId?.let { parameter("transactionId", it) }
            startDate?.let { parameter("dataInizio", it) }
            endDate?.let { parameter("dataFine", it) }
        }
    }

    /**
     * Recupera transazioni Tlog
     *
     * @param sessionId Id della sessione
     * @param contextParamsId Id del ctx
     * @param transactionId Id della transazion
     */
    suspend fun getTlogTransactions(
        sessionId: String? = null,
        contextParamsId: Long? = null,
        transactionId: String? = null
    ): List<Esse3TransactionLogViewParameters> {
        return executeJsonGetList<Esse3TransactionLogViewParameters>("/tlog/transactions", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            sessionId?.let { parameter("sessionId", it) }
            contextParamsId?.let { parameter("ctxParamsId", it) }
            transactionId?.let { parameter("transactionId", it) }
        }
    }
}
