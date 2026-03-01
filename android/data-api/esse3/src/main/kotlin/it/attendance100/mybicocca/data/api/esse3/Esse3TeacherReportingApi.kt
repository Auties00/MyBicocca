package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherDiary
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherDiaryWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRegister
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRegisterWithDetails
import kotlinx.serialization.json.Json

class Esse3TeacherReportingApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/rendicontazione-doc-service-v1") {

    suspend fun filterLecturerDiary(
        academicYearId: Int? = null,
        lecturerId: Long? = null,
        fiscalCode: String? = null,
        matricola: String? = null,
        surname: String? = null,
        diaryStatusCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3TeacherDiary> {
        return executeJsonGetList<Esse3TeacherDiary>("/diari", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            lecturerId?.let { parameter("docenteId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            matricola?.let { parameter("matricola", it) }
            surname?.let { parameter("cognome", it) }
            diaryStatusCode?.let { parameter("staDiarioCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getLecturerDiary(
        diaryId: Long
    ): List<Esse3TeacherDiaryWithDetails> {
        return executeJsonGetList<Esse3TeacherDiaryWithDetails>("/diari/${diaryId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun filterLecturerRegisters(
        academicYearOfferId: Int? = null,
        facultyCode: String? = null,
        activityCode: String? = null,
        lecturerId: Long? = null,
        fiscalCode: String? = null,
        matricola: String? = null,
        surname: String? = null,
        regulationStatusCode: String? = null,
        fromLastStateTransitionDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherRegister> {
        return executeJsonGetList<Esse3TeacherRegister>("/registri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            facultyCode?.let { parameter("facCod", it) }
            activityCode?.let { parameter("adCod", it) }
            lecturerId?.let { parameter("docenteId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            matricola?.let { parameter("matricola", it) }
            surname?.let { parameter("cognome", it) }
            regulationStatusCode?.let { parameter("staRegCod", it) }
            fromLastStateTransitionDate?.let { parameter("daUltimaDataTransStato", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturerRegister(
        registrationId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherRegisterWithDetails> {
        return executeJsonGetList<Esse3TeacherRegisterWithDetails>("/registri/${registrationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }
}
