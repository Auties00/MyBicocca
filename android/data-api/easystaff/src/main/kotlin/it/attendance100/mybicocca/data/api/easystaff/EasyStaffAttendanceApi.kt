/*
TODO: Authentication seems to be broken on the UniMiB courses application
      Even if I log in, the app still uses for auth Bearer RWFzeUJhZGdlQVBJOkVhc3kyMDE5IQ==
      Not sure why, but the login url shoud be https://gestioneorari.didattica.unimib.it/auth/auth_app.php
 */

package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.*
import java.time.Instant

/**
 * API for attendance operations.
 */
class EasyStaffAttendanceApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    companion object {
        private const val ATTENDANCE_HISTORY_ENDPOINT = "/easybadge/api/corso_iscritti.php"
        private const val CERTIFY_ATTENDANCE_ENDPOINT = "/easybadge/api/TimbratureApi.php"
    }

    /**
     * Queries a student's attendance history.
     *
     * @param studentId The unique registration number (matricola) of the student.
     * @return The response.
     */
    suspend fun getAttendanceHistory(
        studentId: String
    ): List<EasyStaffCourseAttendanceHistory> {
        val requestBody = EasyStaffGetAttendanceHistoryRequest(
            studentId = studentId
        )

        val httpResponse = executePostJson<JsonElement>(
            ATTENDANCE_HISTORY_ENDPOINT,
            requestBody
        )

        if(httpResponse !is JsonObject) {
            throw IllegalArgumentException("Cannot get attendance history: invalid json body")
        }

        return when (val studentResults = httpResponse[studentId]) {
            is JsonArray -> listOf()
            is JsonObject -> {
                studentResults.map { (courseTitle, recordsJson) ->
                    val records = json.decodeFromJsonElement<List<EasyStaffAttendanceRecord>>(recordsJson)
                    EasyStaffCourseAttendanceHistory(
                        title = courseTitle,
                        records = records
                    )
                }
            }

            else -> throw IllegalStateException("Cannot get attendance history: invalid json body")
        }
    }

    /**
     * Certifies attendance for a lesson.
     *
     * @param studentId The unique registration number (matricola) of the student.
     * @param lessonCode The unique identifier for the specific lesson.
     * @param deviceToken The unique identifier for the physical device being used.
     * @param longitude The GPS longitude coordinate.
     * @param latitude The GPS latitude coordinate.
     * @return The response.
     */
    suspend fun certifyAttendance(
        studentId: String,
        lessonCode: String,
        deviceToken: String,
        longitude: Double,
        latitude: Double
    ): EasyStaffCertifyAttendanceResult {
        val additionalData = EasyStaffCertifyAttendanceAdditionalData(
            timestamp = Instant.now().toEpochMilli(),
            longitude = longitude,
            latitude = latitude
        )

        val requestBody = EasyStaffCertifyAttendanceRequest(
            studentId = studentId,
            lessonCode = lessonCode,
            deviceToken = deviceToken,
            seat = "",
            language = "en",
            isAuthenticated = true,
            action = "CreateFromJSON",
            additionalData = additionalData
        )

        return executePostJson<EasyStaffCertifyAttendanceResult>(
            CERTIFY_ATTENDANCE_ENDPOINT,
            requestBody
        )
    }
}
