package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3CacheInfo
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChangePasswordResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChangeUserPasswordParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CheckLoginResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3JWKModel
import it.attendance100.mybicocca.data.dto.esse3.Esse3JWTModel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3SessionLanguage
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserSession
import kotlinx.serialization.json.Json

class Esse3AuthApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/") {

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

    suspend fun refreshJWT(
        jwt: String? = null
    ): Esse3JWTModel {
        return executeJsonGet<Esse3JWTModel>("/jwt/refresh", setOf(Esse3PermissionLevel.ANY)) {
            jwt?.let { parameter("jwt", it) }
        }
    }

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

    suspend fun checkSessionId(
        sessionId: String
    ) {
        val response = executeGet("/sessions/${sessionId}/check")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
