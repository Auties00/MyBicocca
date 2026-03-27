package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeData
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import kotlinx.serialization.json.Json

class Esse3BadgeApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/badge-service-v1") {

    suspend fun getNewBadges(
        rfid: String? = null,
        studentId: Long? = null,
        fiscalCode: String? = null,
        courseOfStudyCode: String? = null,
        academicYearAnnualEnrollment: Int? = null,
        studentStatusCode: String? = null,
        matStatusCode: String? = null,
        order: String? = null,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3BadgeData> {
        return executeJsonGetList<Esse3BadgeData>("/badges", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)) {
            rfid?.let { parameter("rfid", it) }
            studentId?.let { parameter("stuId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearAnnualEnrollment?.let { parameter("aaIscrAnn", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            matStatusCode?.let { parameter("staMatCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getBadgeBlobFrontPage(
        badgeBlobId: Long
    ): ByteReadChannel {
        return executeStreamGet("/badges/blobs/${badgeBlobId}/front", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT))
    }

    suspend fun getBadgeBlobRearPage(
        badgeBlobId: Long
    ): ByteReadChannel {
        return executeStreamGet("/badges/blobs/${badgeBlobId}/rear", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT))
    }
}
