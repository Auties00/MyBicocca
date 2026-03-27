package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3NewTeachers
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostTeacherSchedule
import it.attendance100.mybicocca.data.dto.esse3.Esse3PutTeacherNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRole
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachersNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachersTimetable
import kotlinx.serialization.json.Json

class Esse3TeachersApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/docenti-service-v1") {

    suspend fun getLecturers(
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        fiscalCode: String? = null,
        abbreviatedId: Long? = null,
        modificationDate: String? = null,
        insertionDate: String? = null,
        positionId: Long? = null,
        courseOfStudyPositionId: Long? = null,
        facultyPositionId: Long? = null,
        positionValidityDate: String? = null,
        facultyBelongingId: Long? = null,
        lecturerRolesCode: List<String>? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonGetList<Esse3NewTeachers>("/docenti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            abbreviatedId?.let { parameter("idAb", it) }
            modificationDate?.let { parameter("dataMod", it) }
            insertionDate?.let { parameter("dataIns", it) }
            positionId?.let { parameter("caricaId", it) }
            courseOfStudyPositionId?.let { parameter("cdsIdCarica", it) }
            facultyPositionId?.let { parameter("facIdCarica", it) }
            positionValidityDate?.let { parameter("dataValiditaCarica", it) }
            facultyBelongingId?.let { parameter("facIdAppartenenza", it) }
            lecturerRolesCode?.let { parameter("ruoliDocCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getLecturerRoles(
        lecturerRoleCode: String? = null,
        csaCode: String? = null,
        lecturerRoleTypeCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TeacherRole> {
        return executeJsonGetList<Esse3TeacherRole>("/docenti/ruoli", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerRoleCode?.let { parameter("ruoloDocCod", it) }
            csaCode?.let { parameter("csaCod", it) }
            lecturerRoleTypeCode?.let { parameter("tipoRuoloDocCod", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getLecturer(
        lecturerId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonGetList<Esse3NewTeachers>("/docenti/${lecturerId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putLecturer(
        lecturerId: Long,
        body: Esse3TeacherParameters,
        optionalFields: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonPatchList<Esse3NewTeachers>("/docenti/${lecturerId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturerNotes(
        lecturerId: Long
    ): Esse3TeachersNotes {
        return executeJsonGet<Esse3TeachersNotes>("/docenti/${lecturerId}/note", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER))
    }

    suspend fun putLecturerNotes(
        lecturerId: Long,
        body: Esse3PutTeacherNotes
    ): Esse3TeachersNotes {
        return executeJsonPut<Esse3TeachersNotes>("/docenti/${lecturerId}/note", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getLecturerSchedule(
        lecturerId: Long,
        day: Int? = null
    ): List<Esse3TeachersTimetable> {
        return executeJsonGetList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            day?.let { parameter("giorno", it) }
        }
    }

    suspend fun postLecturerSchedule(
        lecturerId: Long,
        body: List<Esse3PostTeacherSchedule>
    ): List<Esse3TeachersTimetable> {
        return executeJsonPostList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putLecturerSchedule(
        lecturerId: Long,
        body: Esse3PostTeacherSchedule,
        day: Int? = null,
        lecturerResearchScheduleId: Long? = null,
        startTime: String? = null,
        endTime: String? = null
    ): List<Esse3TeachersTimetable> {
        return executeJsonPutList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            day?.let { parameter("giorno", it) }
            lecturerResearchScheduleId?.let { parameter("docenteOrarioRicId", it) }
            startTime?.let { parameter("oraInizio", it) }
            endTime?.let { parameter("oraFine", it) }
        }
    }

    suspend fun deleteLecturerSchedule(
        lecturerId: Long,
        day: Int? = null,
        lecturerResearchScheduleId: Long? = null,
        startTime: String? = null,
        endTime: String? = null
    ) {
        val response = executeDelete("/docenti/${lecturerId}/orario") {
            day?.let { parameter("giorno", it) }
            lecturerResearchScheduleId?.let { parameter("docenteOrarioRicId", it) }
            startTime?.let { parameter("oraInizio", it) }
            endTime?.let { parameter("oraFine", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER))
    }
}
