package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AnnualEnrollment
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceDays
import it.attendance100.mybicocca.data.dto.esse3.Esse3CanteenBandParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3Career
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerClosureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerMinimalData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerMinimalDataGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentClasses
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3GraduationWaitingParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PhDProgramCareer
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentTypeParameters
import kotlinx.serialization.json.Json

class Esse3CareersApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/carriere-service-v1") {

    suspend fun getCareers(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3Career> {
        return executeJsonGetList<Esse3Career>("/carriere", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    suspend fun getMinimalGdprCareersData(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3CareerMinimalDataGDPR> {
        return executeJsonGetList<Esse3CareerMinimalDataGDPR>("/carriere-gdpr/datiMin", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    suspend fun getGdprCareerByStudent(
        studentId: Long,
        optionalFields: String? = null
    ): List<Esse3CareerGDPR> {
        return executeJsonGetList<Esse3CareerGDPR>("/carriere-gdpr/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putGraduationWaiting(
        body: Esse3GraduationWaitingParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/aggiornaAttesaLaurea", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getPhdCareersData(
        userId: String? = null,
        govIdentifier: String? = null,
        studentId: Long? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        fromModificationTime: String? = null,
        onlyEnrolled: Int? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearEnrollmentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        onlyActive: Int? = null
    ): List<Esse3PhDProgramCareer> {
        return executeJsonGetList<Esse3PhDProgramCareer>("/carriere/datiDottorato", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentId?.let { parameter("stuId", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    suspend fun getMinimalCareersData(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3CareerMinimalData> {
        return executeJsonGetList<Esse3CareerMinimalData>("/carriere/datiMin", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    suspend fun verifyEnrollmentCancellability(
        studentId: Long? = null,
        academicYearId: Long? = null,
        skipGraduationCheck: Long? = null,
        deleteCharges: Long? = null,
        cancelPlan: Long? = null,
        paymentCheck: Long? = null,
        reverseCharges: Long? = null
    ): String {
        return executeJsonGet<String>("/carriere/verifica-annullabilita-iscrizioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            studentId?.let { parameter("stuId", it) }
            academicYearId?.let { parameter("aaId", it) }
            skipGraduationCheck?.let { parameter("skipCheckEsa", it) }
            deleteCharges?.let { parameter("elimAddebiti", it) }
            cancelPlan?.let { parameter("annullaPiano", it) }
            paymentCheck?.let { parameter("checkPag", it) }
            reverseCharges?.let { parameter("stornaAddebiti", it) }
        }
    }

    suspend fun verifyEnrollment(
        fiscalCode: String? = null,
        studentMatricola: String? = null,
        daysAfterGraduation: Int? = null
    ): Int {
        return executeJsonGet<Int>("/carriere/verifica-iscrizione", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fiscalCode?.let { parameter("codFis", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            daysAfterGraduation?.let { parameter("ggDopoLaurea", it) }
        }
    }

    suspend fun putEnrollmentDateAndExemptionTypeByMatricola(
        matricola: String,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${matricola}/iscrizioni/aggiornaDataIscrAndTipoEsoneroByMat", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    suspend fun getCareerByStudent(
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putCareerByStudent(
        studentId: Long,
        body: Esse3CareerParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun careerClosure(
        studentId: Long,
        body: Esse3CareerClosureParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}/chiudiCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getAttendanceDays(
        studentId: Long
    ): Esse3AttendanceDays {
        return executeJsonGet<Esse3AttendanceDays>("/carriere/${studentId}/giorni-freq", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.UNKNOWN, Esse3PermissionLevel.ADMIN_OFFICE))
    }

    suspend fun getAnnualEnrollment(
        studentId: Long,
        academicYear: Long? = null,
        lastEnrollmentFlag: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYear?.let { parameter("annoAccademico", it) }
            lastEnrollmentFlag?.let { parameter("ultimaIscrizioneFlg", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun putEnrollmentDateAndExemptionTypeByStudentId(
        studentId: Long,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaDataIscrAndTipoEsonero", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    suspend fun putCanteenBand(
        studentId: Long,
        body: Esse3CanteenBandParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaFasciaMensa", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    suspend fun putStudentTypeCode(
        studentId: Long,
        body: Esse3StudentTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaTipoStuCod", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    suspend fun getEnrollmentClasses(
        academicYearEnrollmentId: Long,
        studentId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3EnrollmentClasses> {
        return executeJsonGetList<Esse3EnrollmentClasses>("/carriere/${studentId}/iscrizioni/${academicYearEnrollmentId}/classi", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.STUDENT)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getCareerNotesByStudent(
        studentId: Long,
        verificationDate: String? = null,
        blockingNote: Long? = null,
        academicYearNote: Long? = null,
        csNote: Long? = null,
        certificateNote: Long? = null,
        taxNote: Long? = null,
        webNote: Long? = null,
        contractNoteTypeCode: String? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerNotes> {
        return executeJsonGetList<Esse3CareerNotes>("/carriere/${studentId}/noteCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            verificationDate?.let { parameter("dataVerifica", it) }
            blockingNote?.let { parameter("notaBloccante", it) }
            academicYearNote?.let { parameter("notaAa", it) }
            csNote?.let { parameter("notaCs", it) }
            certificateNote?.let { parameter("notaCert", it) }
            taxNote?.let { parameter("notaTax", it) }
            webNote?.let { parameter("notaWeb", it) }
            contractNoteTypeCode?.let { parameter("tipoContrNotaCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getEnrollmentInfo(
        languageCode: String? = null,
        tcGroupCode: String? = null,
        courseTypeCode: String? = null,
        facultyCode: String? = null,
        facultyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyId: Long? = null
    ): String {
        return executeJsonGet<String>("/immatricolazioni/informazioni", setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)) {
            languageCode?.let { parameter("codLingua", it) }
            tcGroupCode?.let { parameter("gruppoTcCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            facultyCode?.let { parameter("facCod", it) }
            facultyId?.let { parameter("facId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
        }
    }

    suspend fun getAllEnrollmentInfo(
        languageCode: String? = null,
        tcGroupCode: String? = null,
        courseTypeCode: String? = null,
        facultyCode: String? = null,
        facultyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyId: Long? = null
    ): String {
        return executeJsonGet<String>("/immatricolazioni/informazioni/dettaglio", setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)) {
            languageCode?.let { parameter("codLingua", it) }
            tcGroupCode?.let { parameter("gruppoTcCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            facultyCode?.let { parameter("facCod", it) }
            facultyId?.let { parameter("facId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
        }
    }

    suspend fun getEnrollments(
        academicYearEnrollmentId: String,
        includeSXH: Long,
        includeCondition: Long,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        enrollmentTypeCode: String? = null,
        courseYear: Long? = null,
        enrollmentStatusCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/iscrizioni/${academicYearEnrollmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("inclSXH", includeSXH)
            parameter("inclCond", includeCondition)
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            enrollmentTypeCode?.let { parameter("tipoIscrCod", it) }
            courseYear?.let { parameter("annoCorso", it) }
            enrollmentStatusCode?.let { parameter("staIscCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }
}
