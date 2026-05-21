package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CacheInfo
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ChangePasswordResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ChangeUserPasswordParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CheckLoginResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3JWKModel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3JWTModel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SessionLanguage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserSession
import kotlinx.serialization.json.Json

class Esse3AuthApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/") {

    /**
     * @param body Oggetto con i campi da modificare
     */
    suspend fun changeUserPassword(
        body: Esse3ChangeUserPasswordParameters
    ): Esse3ChangePasswordResult {
        return executeJsonPut<Esse3ChangePasswordResult>("/changeUserPassword", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun checkLogon(): Esse3CheckLoginResult {
        return executeJsonGet<Esse3CheckLoginResult>("/checkLogon", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getJWK(): Esse3JWKModel {
        return executeJsonGet<Esse3JWKModel>("/jwt/jwk", setOf(Esse3PermissionLevel.ANY))
    }

    /**
     * @param jwt token jwt in ingresso per il quale effettuare il refresh
     */
    suspend fun refreshJWT(
        jwt: String? = null
    ): Esse3JWTModel {
        return executeJsonGet<Esse3JWTModel>("/jwt/refresh", setOf(Esse3PermissionLevel.ANY)) {
            jwt?.let { parameter("jwt", it) }
        }
    }

    /**
     * @param sessionLanguageCode codice ISO_6392 della lingua con la quale recuperare le descrizioni, nel caso non sia passato in ingresso viene utilizzata la lingua di default del sistema, e nel caso la lingua richiesta non sia disponibile, viene restituita la descrizione nella lingua di default
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun login(
        sessionLanguageCode: String? = null,
        optionalFields: String? = null
    ): Esse3UserSession {
        return executeJsonGet<Esse3UserSession>("/login", setOf(Esse3PermissionLevel.ANY)) {
            sessionLanguageCode?.let { parameter("sessionLinguaCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getCacheParameters(): Esse3CacheInfo {
        return executeJsonGet<Esse3CacheInfo>("/login/cache/", setOf(Esse3PermissionLevel.AUTHENTICATED_USER))
    }

    /**
     * @param body Oggetto che contiene le informazioni da impostare sulla cache
     */
    suspend fun setCacheParameters(
        body: Esse3CacheInfo
    ): Esse3CacheInfo {
        return executeJsonPut<Esse3CacheInfo>("/login/cache/", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getJWT(): Esse3JWTModel {
        return executeJsonGet<Esse3JWTModel>("/login/jwt/new", setOf(Esse3PermissionLevel.AUTHENTICATED_USER))
    }

    suspend fun getLanguageCode(): Esse3SessionLanguage {
        return executeJsonGet<Esse3SessionLanguage>("/login/lingua", setOf(Esse3PermissionLevel.AUTHENTICATED_USER))
    }

    /**
     * @param sessionLanguageCode codice ISO_6392 della lingua con la quale recuperare le descrizioni, nel caso non sia passato in ingresso viene utilizzata la lingua di default del sistema, e nel caso la lingua richiesta non sia disponibile, viene restituita la descrizione nella lingua di default
     */
    suspend fun setLanguageCode(
        sessionLanguageCode: String? = null
    ): Esse3SessionLanguage {
        return executeJsonPut<Esse3SessionLanguage>("/login/lingua", setOf(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            sessionLanguageCode?.let { parameter("sessionLinguaCod", it) }
        }
    }

    suspend fun logout() {
        val response = executeGet("/logout")
        ensureSuccess(response, setOf(Esse3PermissionLevel.AUTHENTICATED_USER))
    }

    /**
     * @param sessionId sessionId di backend
     */
    suspend fun checkSessionId(
        sessionId: String
    ) {
        val response = executeGet("/sessions/${sessionId}/check")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
