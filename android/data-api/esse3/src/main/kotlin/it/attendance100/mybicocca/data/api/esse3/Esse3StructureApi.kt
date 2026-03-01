package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AdmissionTitles
import it.attendance100.mybicocca.data.dto.esse3.Esse3AdmissionTitlesWithCode
import it.attendance100.mybicocca.data.dto.esse3.Esse3CourseCharacteristics
import it.attendance100.mybicocca.data.dto.esse3.Esse3CourseDeadlines
import it.attendance100.mybicocca.data.dto.esse3.Esse3CoursePositions
import it.attendance100.mybicocca.data.dto.esse3.Esse3CourseTuitionFees
import it.attendance100.mybicocca.data.dto.esse3.Esse3CourseTypes
import it.attendance100.mybicocca.data.dto.esse3.Esse3DeletedStudyCourse
import it.attendance100.mybicocca.data.dto.esse3.Esse3DisciplinaryArea
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExternalEntity
import it.attendance100.mybicocca.data.dto.esse3.Esse3Location
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3RegulationWithPhDSectors
import it.attendance100.mybicocca.data.dto.esse3.Esse3StructureWithLocations
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyCourseWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyCourseWithStructure
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPath
import kotlinx.serialization.json.Json

class Esse3StructureApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/struttura-service-v1") {

    suspend fun getDisciplinaryAreas(
        disciplinaryAreaDescription: String? = null,
        order: String? = null
    ): List<Esse3DisciplinaryArea> {
        return executeJsonGetList<Esse3DisciplinaryArea>("/areeDisc", setOf(Esse3PermissionLevel.ANY)) {
            disciplinaryAreaDescription?.let { parameter("areaDiscDes", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getDisciplinaryArea(
        disciplinaryAreaCode: String
    ): Esse3DisciplinaryArea {
        return executeJsonGet<Esse3DisciplinaryArea>("/areeDisc/${disciplinaryAreaCode}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getCoursesOfStudy(
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        courseTypeCode: String? = null,
        courseOfStudyModificationDate: String? = null,
        siteFlag: Int? = null,
        orderActiveFlag: Int? = null,
        orderEnableEnrollmentFlag: Int? = null,
        catalogTypeCode: String? = null,
        trainingCycleTypeCode: String? = null,
        activeInAcademicYear: Int? = null,
        facultyId: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        q: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3StudyCourseWithStructure> {
        return executeJsonGetList<Esse3StudyCourseWithStructure>("/corsi", setOf(Esse3PermissionLevel.ANY)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseOfStudyModificationDate?.let { parameter("dataModCds", it) }
            siteFlag?.let { parameter("sdrFlg", it) }
            orderActiveFlag?.let { parameter("ordAttivoFlg", it) }
            orderEnableEnrollmentFlag?.let { parameter("ordAbilImmaFlg", it) }
            catalogTypeCode?.let { parameter("tipoCatalogoCod", it) }
            trainingCycleTypeCode?.let { parameter("tipoCicloFormCod", it) }
            activeInAcademicYear?.let { parameter("attivoInAa", it) }
            facultyId?.let { parameter("facId", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            q?.let { parameter("q", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCourseCharacteristics(
        courseOfStudyCode: String,
        academicYearStart: Int,
        academicYearEnd: Int
    ): List<Esse3CourseCharacteristics> {
        return executeJsonGetList<Esse3CourseCharacteristics>("/corsi/caratteristiche", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("cdsCod", courseOfStudyCode)
            parameter("aaInizio", academicYearStart)
            parameter("aaFine", academicYearEnd)
        }
    }

    suspend fun getCoursePositions(
        courseOfStudyCode: String,
        academicYearStart: Int,
        academicYearEnd: Int,
        evaluationStartDate: String? = null,
        evaluationEndDate: String? = null
    ): List<Esse3CoursePositions> {
        return executeJsonGetList<Esse3CoursePositions>("/corsi/cariche", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("cdsCod", courseOfStudyCode)
            parameter("aaInizio", academicYearStart)
            parameter("aaFine", academicYearEnd)
            evaluationStartDate?.let { parameter("dataInizioVal", it) }
            evaluationEndDate?.let { parameter("dataFineVal", it) }
        }
    }

    suspend fun getCourseDeadlines(
        courseOfStudyCode: String,
        academicYearId: Int
    ): List<Esse3CourseDeadlines> {
        return executeJsonGetList<Esse3CourseDeadlines>("/corsi/scadenze", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("cdsCod", courseOfStudyCode)
            parameter("aaId", academicYearId)
        }
    }

    suspend fun getCourseTaxes(
        courseOfStudyCode: String,
        academicYearId: Int
    ): List<Esse3CourseTuitionFees> {
        return executeJsonGetList<Esse3CourseTuitionFees>("/corsi/tasse", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("cdsCod", courseOfStudyCode)
            parameter("aaId", academicYearId)
        }
    }

    suspend fun getCourseAccessTitles(
        courseOfStudyCode: String,
        typologyCode: String
    ): List<Esse3AdmissionTitlesWithCode> {
        return executeJsonGetList<Esse3AdmissionTitlesWithCode>("/corsi/titoli-accesso", setOf(Esse3PermissionLevel.ANY)) {
            parameter("cdsCod", courseOfStudyCode)
            parameter("tipologiaCod", typologyCode)
        }
    }

    suspend fun getCourseOfStudy(
        courseOfStudyId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        q: String? = null
    ): Esse3StudyCourseWithStructure {
        return executeJsonGet<Esse3StudyCourseWithStructure>("/corsi/${courseOfStudyId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            q?.let { parameter("q", it) }
        }
    }

    suspend fun getStudyOrders(
        courseOfStudyId: Long,
        optionalFields: String? = null
    ): List<Esse3RegulationWithPhDSectors> {
        return executeJsonGetList<Esse3RegulationWithPhDSectors>("/corsi/${courseOfStudyId}/ordinamenti", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getStudyOrder(
        courseOfStudyId: Long,
        academicYearOrderId: Int,
        optionalFields: String? = null
    ): Esse3RegulationWithPhDSectors {
        return executeJsonGet<Esse3RegulationWithPhDSectors>("/corsi/${courseOfStudyId}/ordinamenti/${academicYearOrderId}", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getPaths(
        courseOfStudyId: Long,
        academicYearOrderId: Int
    ): List<Esse3StudyPath> {
        return executeJsonGetList<Esse3StudyPath>("/corsi/${courseOfStudyId}/ordinamenti/${academicYearOrderId}/percorsi", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getPath(
        courseOfStudyId: Long,
        academicYearOrderId: Int,
        studyPlanId: Long
    ): Esse3StudyPath {
        return executeJsonGet<Esse3StudyPath>("/corsi/${courseOfStudyId}/ordinamenti/${academicYearOrderId}/percorsi/${studyPlanId}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getAccessTitlesStudyPlanOrder(
        courseOfStudyId: Long,
        academicYearOrderId: Int,
        studyPlanId: Long,
        typologyCode: String
    ): List<Esse3AdmissionTitles> {
        return executeJsonGetList<Esse3AdmissionTitles>("/corsi/${courseOfStudyId}/ordinamenti/${academicYearOrderId}/percorsi/${studyPlanId}/titoli-accesso/${typologyCode}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getAccessTitlesOrder(
        courseOfStudyId: Long,
        academicYearOrderId: Int,
        typologyCode: String
    ): List<Esse3AdmissionTitles> {
        return executeJsonGetList<Esse3AdmissionTitles>("/corsi/${courseOfStudyId}/ordinamenti/${academicYearOrderId}/titoli-accesso/${typologyCode}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getAccessTitles(
        courseOfStudyId: Long,
        typologyCode: String
    ): List<Esse3AdmissionTitles> {
        return executeJsonGetList<Esse3AdmissionTitles>("/corsi/${courseOfStudyId}/titoli-accesso/${typologyCode}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getDeletedCoursesOfStudy(
        courseOfStudyModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3DeletedStudyCourse> {
        return executeJsonGetList<Esse3DeletedStudyCourse>("/corsiEliminati", setOf(Esse3PermissionLevel.ANY)) {
            courseOfStudyModificationDate?.let { parameter("dataModCds", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getFullCourseOfStudy(
        courseOfStudyId: Long,
        usageTypeId: Long? = null,
        orderActiveFlag: Int? = null,
        orderEnableEnrollmentFlag: Int? = null,
        optionalFields: String? = null
    ): Esse3StudyCourseWithDetails {
        return executeJsonGet<Esse3StudyCourseWithDetails>("/corsiFull/${courseOfStudyId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            usageTypeId?.let { parameter("tipoUtilizzoId", it) }
            orderActiveFlag?.let { parameter("ordAttivoFlg", it) }
            orderEnableEnrollmentFlag?.let { parameter("ordAbilImmaFlg", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getExternalEntities(
        entityId: Int? = null,
        entityTypeCode: String? = null,
        entityCode: String? = null,
        entityType: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExternalEntity> {
        return executeJsonGetList<Esse3ExternalEntity>("/entiEsterni", setOf(Esse3PermissionLevel.ANY)) {
            entityId?.let { parameter("enteId", it) }
            entityTypeCode?.let { parameter("tipoEnteCod", it) }
            entityCode?.let { parameter("enteCod", it) }
            entityType?.let { parameter("tipoEnte", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getSites(
        siteDescription: String? = null,
        order: String? = null,
        fields: String? = null,
        facultyId: Long? = null,
        courseOfStudyId: Long? = null,
        departmentId: Long? = null
    ): List<Esse3Location> {
        return executeJsonGetList<Esse3Location>("/sedi", setOf(Esse3PermissionLevel.ANY)) {
            siteDescription?.let { parameter("sedeDes", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            facultyId?.let { parameter("idFac", it) }
            courseOfStudyId?.let { parameter("idCds", it) }
            departmentId?.let { parameter("idDip", it) }
        }
    }

    suspend fun getSite(
        siteId: Long
    ): Esse3Location {
        return executeJsonGet<Esse3Location>("/sedi/${siteId}", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getDidacticStructures(
        facultyCode: String? = null,
        facultyDescription: String? = null,
        siteType: String? = null,
        activeInAcademicYear: Int? = null,
        courseOfStudyCode: String? = null,
        order: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3StructureWithLocations> {
        return executeJsonGetList<Esse3StructureWithLocations>("/strutture", setOf(Esse3PermissionLevel.ANY)) {
            facultyCode?.let { parameter("facCod", it) }
            facultyDescription?.let { parameter("facDes", it) }
            siteType?.let { parameter("sdrTip", it) }
            activeInAcademicYear?.let { parameter("attivoInAa", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getDidacticStructures(
        facultyId: Long,
        optionalFields: String? = null
    ): Esse3StructureWithLocations {
        return executeJsonGet<Esse3StructureWithLocations>("/strutture/${facultyId}/", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getCourseTypes(
        courseTypeCode: String? = null,
        courseTypeDescription: String? = null,
        facultyId: Int? = null,
        phdFlag: Int? = null,
        specializationSchoolFlag: Int? = null,
        tcGroupCode: String? = null,
        order: String? = null
    ): List<Esse3CourseTypes> {
        return executeJsonGetList<Esse3CourseTypes>("/tipiCorso", setOf(Esse3PermissionLevel.ANY)) {
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseTypeDescription?.let { parameter("tipoCorsoDes", it) }
            facultyId?.let { parameter("facId", it) }
            phdFlag?.let { parameter("dottoratoFlg", it) }
            specializationSchoolFlag?.let { parameter("scuolaSpecFlg", it) }
            tcGroupCode?.let { parameter("gruppoTcCod", it) }
            order?.let { parameter("order", it) }
        }
    }
}
