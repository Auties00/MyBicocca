package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Award
import it.attendance100.mybicocca.data.dto.esse3.Esse3AwardReturn
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeClass
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeClassReturn
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeIssuanceNotification
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import kotlinx.serialization.json.Json

class Esse3BadgeImportApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/importbadge-service-v1") {

    /**
     * consente di importare uno o più badge (Award)
     *
     * @param body Oggetto che contiene il json con i dati da inserire
     */
    suspend fun postImportAward(
        body: List<Esse3Award>
    ): Esse3AwardReturn {
        return executeJsonPost<Esse3AwardReturn>("/importAward", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * consente di importare una o più tipologie di badge (Badge Class)
     *
     * @param body Oggetto che contiene il json con i dati da inserire
     */
    suspend fun postImportBadgeClass(
        body: List<Esse3BadgeClass>
    ): Esse3BadgeClassReturn {
        return executeJsonPost<Esse3BadgeClassReturn>("/importBadgeClass", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * consente di importare i dati di emissione di un badge
     *
     * @param body Oggetto che contiene i dati di emissione/annullamento di un badge
     */
    suspend fun putBadgeIssuing(
        body: Esse3BadgeIssuanceNotification
    ): String {
        return executeJsonPut<String>("/importBadgeIssuing", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
