package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3PersonalDataApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3PersonalDataApiTest::class.java)

    // ─── Reference data (no params required) ────────────────────────────────

    @Test
    suspend fun getHighSchoolGradeRange() {
        try {
            val result = api.personalData.getHighSchoolGradeRange()
            logger.info("getHighSchoolGradeRange: ${result.size} items")
            for (range in result) {
                logger.info("  yearFrom=${range.yearFrom}, yearTo=${range.yearTo}, minGrade=${range.minGrade}, maxGrade=${range.maxGrade}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHighSchoolGradeRange not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHigherInstitutionTypes() {
        try {
            val result = api.personalData.getHigherInstitutionTypes()
            logger.info("getHigherInstitutionTypes: ${result.size} items")
            for (item in result) {
                logger.info("  institutionType: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHigherInstitutionTypes not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHigherSchoolTitleTypes() {
        try {
            val result = api.personalData.getHigherSchoolTitleTypes()
            logger.info("getHigherSchoolTitleTypes: ${result.size} items")
            for (item in result) {
                logger.info("  titleType: code=${item.titleTypeCode}, desc=${item.description}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHigherSchoolTitleTypes not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getForeignTitleTypes() {
        try {
            val result = api.personalData.getForeignTitleTypes()
            logger.info("getForeignTitleTypes: ${result.size} items")
            for (item in result) {
                logger.info("  foreignTitleType: code=${item.titleStatusTypeCode}, desc=${item.description}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getForeignTitleTypes not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getForeignTitleValueDeclarationTypologies() {
        try {
            val result = api.personalData.getForeignTitleValueDeclarationTypologies()
            logger.info("getForeignTitleValueDeclarationTypologies: ${result.size} items")
            for (item in result) {
                logger.info("  typology: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getForeignTitleValueDeclarationTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHandicapTypologies() {
        try {
            val result = api.personalData.getHandicapTypologies()
            logger.info("getHandicapTypologies: ${result.size} items")
            for (item in result) {
                logger.info("  handicapTypology: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHandicapTypologiesToEvaluate() {
        try {
            val result = api.personalData.getHandicapTypologiesToEvaluate()
            logger.info("getHandicapTypologiesToEvaluate: ${result.size} items")
            for (item in result) {
                logger.info("  handicapTypologyToEvaluate: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapTypologiesToEvaluate not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getRelationshipTypologies() {
        try {
            val result = api.personalData.getRelationshipTypologies()
            logger.info("getRelationshipTypologies: ${result.size} items")
            for (item in result) {
                logger.info("  relationshipType: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getRelationshipTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHandicapRegulations() {
        try {
            val result = api.personalData.getHandicapRegulations()
            logger.info("getHandicapRegulations: ${result.size} items")
            for (item in result) {
                logger.info("  handicapRegulation: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapRegulations not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getTutorRules() {
        try {
            val result = api.personalData.getTutorRules()
            logger.info("getTutorRules: ${result.size} items")
            for (item in result) {
                logger.info("  tutorRule: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getTutorRules not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCompensatoryMeasures() {
        try {
            val result = api.personalData.getCompensatoryMeasures()
            logger.info("getCompensatoryMeasures: ${result.size} items")
            for (item in result) {
                logger.info("  compensatoryMeasure: $item")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCompensatoryMeasures not authorized: ${e.message}")
        }
    }

    // ─── Universities & Institutions ────────────────────────────────────────

    @Test
    suspend fun getUniversities() {
        try {
            val result = api.personalData.getUniversities(limit = 20)
            logger.info("getUniversities: ${result.size} items")
            assert(result.isNotEmpty()) { "Expected at least one university" }
            for (uni in result) {
                logger.info("  university: istatCode=${uni.istatCode}, name=${uni.description}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUniversities not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getUniversityCourses() {
        try {
            val result = api.personalData.getUniversityCourses(limit = 20)
            logger.info("getUniversityCourses: ${result.size} items")
            for (course in result) {
                logger.info("  course: $course")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUniversityCourses not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getForeignUniversities() {
        try {
            val result = api.personalData.getForeignUniversities(limit = 20)
            logger.info("getForeignUniversities: ${result.size} items")
            for (uni in result) {
                logger.info("  foreignUniversity: $uni")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getForeignUniversities not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInstitutions() {
        try {
            val result = api.personalData.getInstitutions(limit = 20)
            logger.info("getInstitutions: ${result.size} items")
            for (inst in result) {
                logger.info("  institution: $inst")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getInstitutions not authorized: ${e.message}")
        }
    }

    // ─── Person endpoints ────────────────────────────────────────────────────

    @Test
    suspend fun getPersons_byPersonId() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersons(personId = personId)
            logger.info("getPersons(personId=$personId): ${result.size} items")
            assert(result.isNotEmpty()) { "Expected at least one person for personId=$personId" }
            val person = result.first()
            assert(person.personId == personId) { "personId mismatch: expected $personId, got ${person.personId}" }
            logger.info("  person: personId=${person.personId}, name=${person.name}, surname=${person.surname}, fiscalCode=${person.fiscalCode}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersons not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getPersons_byFiscalCode() {
        try {
            val fiscalCode = session.fiscalCode ?: run {
                logger.warn("No fiscal code in session, skipping getPersons_byFiscalCode")
                return
            }
            val result = api.personalData.getPersons(fiscalCode = fiscalCode)
            logger.info("getPersons(fiscalCode=$fiscalCode): ${result.size} items")
            for (person in result) {
                logger.info("  person: personId=${person.personId}, name=${person.name}, surname=${person.surname}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersons not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getPerson() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPerson(personId)
            logger.info("getPerson($personId): personId=${result.personId}, name=${result.name}, surname=${result.surname}")
            assert(result.personId == personId) { "personId mismatch: expected $personId, got ${result.personId}" }
            assert(!result.surname.isNullOrBlank()) { "Expected non-null surname" }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPerson not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getGdprPersons_byFiscalCode() {
        try {
            val fiscalCode = session.fiscalCode ?: run {
                logger.warn("No fiscal code in session, skipping getGdprPersons_byFiscalCode")
                return
            }
            val result = api.personalData.getGdprPersons(fiscalCode = fiscalCode)
            logger.info("getGdprPersons(fiscalCode=$fiscalCode): ${result.size} items")
            for (person in result) {
                logger.info("  gdprPerson: personId=${person.personId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getGdprPersons not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getGdprPerson() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getGdprPerson(personId)
            logger.info("getGdprPerson($personId): personId=${result.personId}")
            assert(result.personId == personId) { "personId mismatch: expected $personId, got ${result.personId}" }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getGdprPerson not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getPersonPhoto() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersonPhoto(personId)
            logger.info("getPersonPhoto($personId): length=${result.length}")
            assert(result.isNotBlank()) { "Expected non-empty photo data" }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonPhoto not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getPersonPhoto validation error (no photo?): ${e.message}")
        }
    }

    @Test
    suspend fun getPhotoValidationFlag() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPhotoValidationFlag(personId)
            logger.info("getPhotoValidationFlag($personId): $result")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPhotoValidationFlag not authorized: ${e.message}")
        }
    }

    // ─── Career endpoints ────────────────────────────────────────────────────

    @Test
    suspend fun getCareers_byUserId() {
        try {
            val userId = session.userId
            val result = api.personalData.getCareers(userId = userId)
            logger.info("getCareers(userId=$userId): ${result.size} items")
            assert(result.isNotEmpty()) { "Expected at least one career for userId=$userId" }
            for (career in result) {
                logger.info("  career: studentId=${career.studentId}, courseOfStudyId=${career.courseOfStudyId}, status=${career.studentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCareers_byFiscalCode() {
        try {
            val fiscalCode = session.fiscalCode ?: run {
                logger.warn("No fiscal code in session, skipping getCareers_byFiscalCode")
                return
            }
            val result = api.personalData.getCareers(fiscalCode = fiscalCode)
            logger.info("getCareers(fiscalCode=$fiscalCode): ${result.size} items")
            for (career in result) {
                logger.info("  career: studentId=${career.studentId}, status=${career.studentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCareerByStudent() {
        try {
            val studentId = studentProfile.studentId
            val result = api.personalData.getCareerByStudent(studentId)
            logger.info("getCareerByStudent($studentId): studentId=${result.studentId}, personId=${result.personId}, courseId=${result.courseOfStudyId}")
            assert(result.studentId == studentId) { "studentId mismatch: expected $studentId, got ${result.studentId}" }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerByStudent not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getGdprCareerByStudent() {
        try {
            val studentId = studentProfile.studentId
            val result = api.personalData.getGdprCareerByStudent(studentId)
            logger.info("getGdprCareerByStudent($studentId): ${result.size} items")
            for (career in result) {
                logger.info("  gdprCareer: ${career.studentId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getGdprCareerByStudent not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getMinimalCareersData_byUserId() {
        try {
            val userId = session.userId
            val result = api.personalData.getMinimalCareersData(userId = userId)
            logger.info("getMinimalCareersData(userId=$userId): ${result.size} items")
            for (career in result) {
                logger.info("  minimalCareer: studentId=${career.studentId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalCareersData not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getPhdCareersData_byUserId() {
        try {
            val userId = session.userId
            val result = api.personalData.getPhdCareersData(userId = userId)
            logger.info("getPhdCareersData(userId=$userId): ${result.size} items")
            for (career in result) {
                logger.info("  phdCareer: studentId=${career.studentId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPhdCareersData not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCareerByStudentPerson() {
        try {
            val personId = studentProfile.personId
            val studentId = studentProfile.studentId
            val result = api.personalData.getCareerByStudentPerson(personId, studentId)
            logger.info("getCareerByStudentPerson(personId=$personId, studentId=$studentId): studentId=${result.studentId}")
            assert(result.studentId == studentId) { "studentId mismatch: expected $studentId, got ${result.studentId}" }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerByStudentPerson not authorized: ${e.message}")
        }
    }

    // ─── Annual enrollment ───────────────────────────────────────────────────

    @Test
    suspend fun getAnnualEnrollment() {
        try {
            val studentId = studentProfile.studentId
            val result = api.personalData.getAnnualEnrollment(studentId)
            logger.info("getAnnualEnrollment($studentId): ${result.size} items")
            assert(result.isNotEmpty()) { "Expected at least one enrollment for studentId=$studentId" }
            for (enrollment in result) {
                logger.info("  enrollment: academicYear=${enrollment.academicYearEnrollmentId}, status=${enrollment.enrollmentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAnnualEnrollment not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getEnrollments() {
        try {
            val enrollment = api.personalData.getAnnualEnrollment(studentProfile.studentId).firstOrNull()
            if (enrollment == null) {
                logger.warn("No annual enrollments found, skipping getEnrollments test")
                return
            }
            val academicYearEnrollmentId = enrollment.academicYearEnrollmentId?.toString() ?: run {
                logger.warn("No academicYearEnrollmentId in enrollment, skipping getEnrollments test")
                return
            }
            val result = api.personalData.getEnrollments(
                academicYearEnrollmentId = academicYearEnrollmentId,
                includeSXH = 0,
                includeCondition = 0,
                limit = 10
            )
            logger.info("getEnrollments(academicYearEnrollmentId=$academicYearEnrollmentId): ${result.size} items")
            for (enr in result) {
                logger.info("  enrollment: studentId=${enr.studentId}, status=${enr.enrollmentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollments not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getEnrollments validation error: ${e.message}")
        }
    }

    // ─── Bank details ────────────────────────────────────────────────────────

    @Test
    suspend fun getBankDetails() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getBankDetails(personId)
            logger.info("getBankDetails($personId): ${result.size} items")
            for (bank in result) {
                logger.info("  bankDetails: $bank")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getBankDetails not authorized: ${e.message}")
        }
    }

    // ─── Lecturer ───────────────────────────────────────────────────────────

    @Test
    suspend fun getLecturer_firstFromExternalSubjects() {
        // getLecturer requires a lecturer's abbreviated idAb; try using the first external subject's abbreviatedId
        try {
            val subjects = api.personalData.getExternalSubject(limit = 5)
            val subject = subjects.firstOrNull()
            if (subject == null) {
                logger.info("No external subjects found, skipping getLecturer test")
                return
            }
            val abbreviatedId = subject.abbreviatedId ?: run {
                logger.warn("No abbreviatedId in external subject, skipping getLecturer test")
                return
            }
            val result = api.personalData.getLecturer(abbreviatedId)
            logger.info("getLecturer(abbreviatedId=$abbreviatedId): ${result.size} items")
            for (teacher in result) {
                logger.info("  lecturer: $teacher")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getLecturer not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getLecturer validation error: ${e.message}")
        }
    }

    // ─── Handicap declaration ────────────────────────────────────────────────

    @Test
    suspend fun getHandicapDeclaration() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getHandicapDeclaration(personId)
            logger.info("getHandicapDeclaration($personId): ${result.size} items")
            for (decl in result) {
                logger.info("  handicapDeclaration: id=${decl.handicapDeclarationId}, type=${decl.handicapType}, state=${decl.handicapDeclarationState}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapDeclaration not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getHandicapDeclarationById() {
        try {
            val personId = studentProfile.personId
            val declarations = api.personalData.getHandicapDeclaration(personId)
            val declaration = declarations.firstOrNull() ?: run {
                logger.info("No handicap declarations found for personId=$personId, skipping getHandicapDeclarationById")
                return
            }
            val declarationId = declaration.handicapDeclarationId?.toLong() ?: run {
                logger.warn("No handicapDeclarationId in declaration, skipping")
                return
            }
            val result = api.personalData.getHandicapDeclarationById(personId, declarationId)
            logger.info("getHandicapDeclarationById($personId, $declarationId): id=${result.handicapDeclarationId}, type=${result.handicapType}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapDeclarationById not authorized: ${e.message}")
        }
    }

    // ─── Compensatory measures ───────────────────────────────────────────────

    @Test
    suspend fun getPersonCompensatoryMeasures() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersonCompensatoryMeasures(personId)
            logger.info("getPersonCompensatoryMeasures($personId): ${result.size} items")
            for (measure in result) {
                logger.info("  compensatoryMeasure: $measure")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonCompensatoryMeasures not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getPersonCompensatoryMeasuresHandicapDeclaration() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersonCompensatoryMeasuresHandicapDeclaration(personId)
            logger.info("getPersonCompensatoryMeasuresHandicapDeclaration($personId): ${result.size} items")
            for (measure in result) {
                logger.info("  measure: $measure")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonCompensatoryMeasuresHandicapDeclaration not authorized: ${e.message}")
        }
    }

    // ─── Person tutors ───────────────────────────────────────────────────────

    @Test
    suspend fun getPersonTutors() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersonTutors(personId)
            logger.info("getPersonTutors($personId): ${result.size} items")
            for (tutor in result) {
                logger.info("  tutor: $tutor")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonTutors not authorized: ${e.message}")
        }
    }

    // ─── Titles ──────────────────────────────────────────────────────────────

    @Test
    suspend fun getTitles() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getTitles(personId, studentId = studentProfile.studentId)
            logger.info("getTitles($personId): ${result.personId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getTitles not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getTitles validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getPersonTitles_byPersonId() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getPersonTitles(personId = personId)
            logger.info("getPersonTitles(personId=$personId): personId=${result.personId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonTitles not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getPersonTitles validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getPersonTitles_byStudentId() {
        try {
            val studentId = studentProfile.studentId
            val result = api.personalData.getPersonTitles(studentId = studentId)
            logger.info("getPersonTitles(studentId=$studentId): personId=${result.personId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPersonTitles not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getPersonTitles validation error: ${e.message}")
        }
    }

    // ─── Student consents ────────────────────────────────────────────────────

    @Test
    suspend fun getStudentConsents() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getStudentConsents(
                personId = personId,
                webProcedureCode = "IMMATRICOLA"
            )
            logger.info("getStudentConsents($personId, IMMATRICOLA): ${result.size} items")
            for (consent in result) {
                logger.info("  consent: typeCode=${consent.consentTypesConsentTypeCode}, flag=${consent.consentFlag}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getStudentConsents not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getStudentConsents validation error: ${e.message}")
        }
    }

    // ─── Authorized for person ───────────────────────────────────────────────

    @Test
    suspend fun getAuthorizedForPerson() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getAuthorizedForPerson(personId)
            logger.info("getAuthorizedForPerson($personId): ${result.size} items")
            for (authorized in result) {
                logger.info("  authorized: authorizedId=${authorized.authorizedId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAuthorizedForPerson not authorized: ${e.message}")
        }
    }

    // ─── External subjects ───────────────────────────────────────────────────

    @Test
    suspend fun getExternalSubject() {
        try {
            val result = api.personalData.getExternalSubject(limit = 20)
            logger.info("getExternalSubject: ${result.size} items")
            for (subject in result) {
                logger.info("  externalSubject: id=${subject.externalSubjectId}, surname=${subject.surname}, name=${subject.name}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExternalSubject not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getExternalSubjectConsents() {
        try {
            val subjects = api.personalData.getExternalSubject(limit = 5)
            val subject = subjects.firstOrNull() ?: run {
                logger.info("No external subjects found, skipping getExternalSubjectConsents")
                return
            }
            val subjectId = subject.externalSubjectId?.toInt() ?: run {
                logger.warn("No externalSubjectId in subject, skipping")
                return
            }
            val result = api.personalData.getExternalSubjectConsents(
                externalSubjectId = subjectId,
                webProcedureCode = "IMMATRICOLA"
            )
            logger.info("getExternalSubjectConsents($subjectId, IMMATRICOLA): ${result.size} items")
            for (consent in result) {
                logger.info("  consent: typeCode=${consent.consentTypesConsentTypeCode}, flag=${consent.consentFlag}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExternalSubjectConsents not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getExternalSubjectConsents validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getExternalSubjectsReplica() {
        try {
            val result = api.personalData.getExternalSubjectsReplica(limit = 20)
            logger.info("getExternalSubjectsReplica: ${result.size} items")
            for (replica in result) {
                logger.info("  externalSubjectReplica: id=${replica.externalSubjectId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExternalSubjectsReplica not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getExternalSubjectReplica() {
        try {
            val subjects = api.personalData.getExternalSubject(limit = 5)
            val subject = subjects.firstOrNull() ?: run {
                logger.info("No external subjects found, skipping getExternalSubjectReplica")
                return
            }
            val subjectId = subject.externalSubjectId?.toInt() ?: run {
                logger.warn("No externalSubjectId in subject, skipping")
                return
            }
            val result = api.personalData.getExternalSubjectReplica(subjectId)
            logger.info("getExternalSubjectReplica($subjectId): externalSubjectId=${result.externalSubjectId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExternalSubjectReplica not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getExternalSubjectReplica validation error: ${e.message}")
        }
    }

    // ─── Attachment metadata ─────────────────────────────────────────────────

    @Test
    suspend fun getAuthorizedAttachmentMetadata() {
        try {
            val authorized = api.personalData.getAuthorizedForPerson(studentProfile.personId)
            val auth = authorized.firstOrNull() ?: run {
                logger.info("No authorized persons found, skipping getAuthorizedAttachmentMetadata")
                return
            }
            val authorizedId = auth.authorizedId ?: run {
                logger.warn("No authorizedId, skipping")
                return
            }
            val result = api.personalData.getAuthorizedAttachmentMetadata(authorizedId)
            logger.info("getAuthorizedAttachmentMetadata($authorizedId): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAuthorizedAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getAuthorizedAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getAuthorizedPersonalDocumentAttachmentMetadata() {
        try {
            val authorized = api.personalData.getAuthorizedForPerson(studentProfile.personId)
            val auth = authorized.firstOrNull() ?: run {
                logger.info("No authorized persons found, skipping getAuthorizedPersonalDocumentAttachmentMetadata")
                return
            }
            val authorizedId = auth.authorizedId ?: run {
                logger.warn("No authorizedId, skipping")
                return
            }
            val result = api.personalData.getAuthorizedPersonalDocumentAttachmentMetadata(
                authorizedId = authorizedId,
                identityDocumentTypeCode = "CI"
            )
            logger.info("getAuthorizedPersonalDocumentAttachmentMetadata($authorizedId): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: ${meta.attachmentId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAuthorizedPersonalDocumentAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getAuthorizedPersonalDocumentAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getHandicapDeclarationAttachmentMetadata() {
        try {
            val personId = studentProfile.personId
            val declarations = api.personalData.getHandicapDeclaration(personId)
            val decl = declarations.firstOrNull() ?: run {
                logger.info("No handicap declarations for personId=$personId, skipping getHandicapDeclarationAttachmentMetadata")
                return
            }
            val handicapType = decl.handicapType ?: run {
                logger.warn("No handicapType, skipping")
                return
            }
            val result = api.personalData.getHandicapDeclarationAttachmentMetadata(
                personId = personId,
                handicapType = handicapType
            )
            logger.info("getHandicapDeclarationAttachmentMetadata($personId, $handicapType): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHandicapDeclarationAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getHandicapDeclarationAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getIdentityDocumentAttachmentMetadata() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getIdentityDocumentAttachmentMetadata(
                personId = personId,
                identityDocumentTypeCode = "CI"
            )
            logger.info("getIdentityDocumentAttachmentMetadata($personId, CI): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getIdentityDocumentAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getIdentityDocumentAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getHighSchoolGraduationAttachmentMetadata() {
        try {
            val personId = studentProfile.personId
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val result = api.personalData.getHighSchoolGraduationAttachmentMetadata(
                personId = personId,
                highSchoolGraduationYear = currentYear - 5
            )
            logger.info("getHighSchoolGraduationAttachmentMetadata($personId, ${currentYear - 5}): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getHighSchoolGraduationAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getHighSchoolGraduationAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getItalianTitleAttachmentMetadata() {
        try {
            val personId = studentProfile.personId
            val result = api.personalData.getItalianTitleAttachmentMetadata(
                personId = personId,
                titleCategoryCode = "L"
            )
            logger.info("getItalianTitleAttachmentMetadata($personId, L): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getItalianTitleAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getItalianTitleAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getForeignTitleAttachmentMetadata() {
        try {
            val personId = studentProfile.personId
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val result = api.personalData.getForeignTitleAttachmentMetadata(
                personId = personId,
                academicYearAwardedTitle = currentYear - 5
            )
            logger.info("getForeignTitleAttachmentMetadata($personId, ${currentYear - 5}): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getForeignTitleAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getForeignTitleAttachmentMetadata validation error: ${e.message}")
        }
    }

    @Test
    suspend fun getMatricolaAttachmentMetadata() {
        try {
            val studentId = studentProfile.studentId
            val result = api.personalData.getMatricolaAttachmentMetadata(studentId)
            logger.info("getMatricolaAttachmentMetadata($studentId): ${result.size} items")
            for (meta in result) {
                logger.info("  metadata: attachmentId=${meta.attachmentId}, fileName=${meta.fileName}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMatricolaAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("getMatricolaAttachmentMetadata validation error: ${e.message}")
        }
    }

    // ─── PUT methods with rollback ───────────────────────────────────────────

    @Test
    suspend fun putMobilePhone_rollback() {
        try {
            val personId = studentProfile.personId
            val person = api.personalData.getPerson(personId)
            val originalMobile = person.mobilePhone ?: ""

            logger.info("putMobilePhone rollback test: personId=$personId, original=$originalMobile")
            try {
                val updated = api.personalData.putMobilePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3MobileParameter(mobilePhone = originalMobile)
                )
                logger.info("putMobilePhone result: mobilePhone=${updated.mobilePhone}")
                assert(updated.mobilePhone == originalMobile) { "Mobile phone should be unchanged after rollback PUT" }
            } finally {
                // Restore original value
                api.personalData.putMobilePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3MobileParameter(mobilePhone = originalMobile)
                )
                logger.info("putMobilePhone rollback completed, restored to: $originalMobile")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("putMobilePhone not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("putMobilePhone validation error: ${e.message}")
        }
    }

    @Test
    suspend fun putDomicilePhone_rollback() {
        try {
            val personId = studentProfile.personId
            val person = api.personalData.getPerson(personId)
            val originalPhone = person.domicilePhone ?: ""

            logger.info("putDomicilePhone rollback test: personId=$personId, original=$originalPhone")
            try {
                val updated = api.personalData.putDomicilePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PhoneParameters(phoneNumber = originalPhone)
                )
                logger.info("putDomicilePhone result: phone=${updated.phoneNumber}")
            } finally {
                api.personalData.putDomicilePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PhoneParameters(phoneNumber = originalPhone)
                )
                logger.info("putDomicilePhone rollback completed, restored to: $originalPhone")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("putDomicilePhone not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("putDomicilePhone validation error: ${e.message}")
        }
    }

    @Test
    suspend fun putResidencePhone_rollback() {
        try {
            val personId = studentProfile.personId
            val person = api.personalData.getPerson(personId)
            val originalPhone = person.residencePhone ?: ""

            logger.info("putResidencePhone rollback test: personId=$personId, original=$originalPhone")
            try {
                val updated = api.personalData.putResidencePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PhoneParameters(phoneNumber = originalPhone)
                )
                logger.info("putResidencePhone result: phone=${updated.phoneNumber}")
            } finally {
                api.personalData.putResidencePhone(
                    personId = personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PhoneParameters(phoneNumber = originalPhone)
                )
                logger.info("putResidencePhone rollback completed, restored to: $originalPhone")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("putResidencePhone not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("putResidencePhone validation error: ${e.message}")
        }
    }

    @Test
    suspend fun putStudentConsents_rollback() {
        try {
            val personId = studentProfile.personId
            val webProcedureCode = "IMMATRICOLA"
            val originalConsents = api.personalData.getStudentConsents(personId, webProcedureCode)
            logger.info("putStudentConsents rollback test: ${originalConsents.size} original consents")

            if (originalConsents.isEmpty()) {
                logger.info("No consents to test with, skipping")
                return
            }

            try {
                val params = originalConsents.map { consent ->
                    it.attendance100.mybicocca.data.dto.esse3.Esse3ConsentsParameters(
                        consentTypeCode = consent.consentTypesConsentTypeCode ?: "",
                        consentFlag = consent.consentFlag ?: 0
                    )
                }
                val updated = api.personalData.putStudentConsents(personId, params, webProcedureCode)
                logger.info("putStudentConsents result: ${updated.size} consents updated")
            } finally {
                val restoreParams = originalConsents.map { consent ->
                    it.attendance100.mybicocca.data.dto.esse3.Esse3ConsentsParameters(
                        consentTypeCode = consent.consentTypesConsentTypeCode ?: "",
                        consentFlag = consent.consentFlag ?: 0
                    )
                }
                api.personalData.putStudentConsents(personId, restoreParams, webProcedureCode)
                logger.info("putStudentConsents rollback completed")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("putStudentConsents not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("putStudentConsents validation error: ${e.message}")
        }
    }

    // ─── Destructive / irreversible operations (disabled) ───────────────────

    @Test
    @Disabled("Destructive: dismissEmail would invalidate the institutional email address")
    suspend fun dismissEmail() {
        api.personalData.dismissEmail(studentProfile.personId)
    }

    @Test
    @Disabled("Destructive: dismissEmailByAteEmail would invalidate the institutional email address")
    suspend fun dismissEmailByAteEmail() {
        val person = api.personalData.getPerson(studentProfile.personId)
        val ateEmail = person.ateEmail ?: return
        api.personalData.dismissEmailByAteEmail(ateEmail)
    }

    @Test
    @Disabled("Destructive: careerClosure would permanently close the student career")
    suspend fun careerClosure() {
        api.personalData.careerClosure(
            studentId = studentProfile.studentId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3CareerClosureParameters(
                statusReasonCode = "RINUNCIA",
                closingDate = "2026-01-01"
            )
        )
    }

    @Test
    @Disabled("Destructive: putGraduationWaiting would modify graduation status")
    suspend fun putGraduationWaiting() {
        api.personalData.putGraduationWaiting(
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3GraduationWaitingParameters(
                studentId = studentProfile.studentId,
                degreeAwardFlag = 0
            )
        )
    }

    @Test
    @Disabled("Destructive: putEnrollmentDateAndExemptionTypeByMatricola would modify enrollment dates")
    suspend fun putEnrollmentDateAndExemptionTypeByMatricola() {
        api.personalData.putEnrollmentDateAndExemptionTypeByMatricola(
            matricola = studentProfile.enrollmentNumber,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters(
                exemptionTypeCode = null,
                enrollmentDate = null
            ),
            academicYear = 2025
        )
    }

    @Test
    @Disabled("Destructive: putEnrollmentDateAndExemptionTypeByStudentId would modify enrollment dates")
    suspend fun putEnrollmentDateAndExemptionTypeByStudentId() {
        api.personalData.putEnrollmentDateAndExemptionTypeByStudentId(
            studentId = studentProfile.studentId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters(
                exemptionTypeCode = null,
                enrollmentDate = null
            ),
            academicYear = 2025
        )
    }

    @Test
    @Disabled("Destructive: putCanteenBand would modify student canteen band")
    suspend fun putCanteenBand() {
        api.personalData.putCanteenBand(
            studentId = studentProfile.studentId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3CanteenBandParameters(
                canteenBandId = null
            ),
            academicYear = 2025
        )
    }

    @Test
    @Disabled("Destructive: putStudentTypeCode would modify student type")
    suspend fun putStudentTypeCode() {
        api.personalData.putStudentTypeCode(
            studentId = studentProfile.studentId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3StudentTypeParameters(
                studentTypeCode = null
            ),
            academicYear = 2025
        )
    }

    @Test
    @Disabled("Destructive: putStudentEmail would change the student personal email")
    suspend fun putStudentEmail() {
        val person = api.personalData.getPerson(studentProfile.personId)
        api.personalData.putStudentEmail(studentProfile.personId, person.email)
    }

    @Test
    @Disabled("Destructive: putStudentAteEmail would change the institutional email")
    suspend fun putStudentAteEmail() {
        val person = api.personalData.getPerson(studentProfile.personId)
        val ateEmail = person.ateEmail ?: return
        api.personalData.putStudentAteEmail(studentProfile.personId, ateEmail)
    }

    @Test
    @Disabled("Potentially modifies career protocol number; only safe to run with known value")
    suspend fun putCareerByStudent() {
        val career = api.personalData.getCareerByStudent(studentProfile.studentId)
        val protocolNumber = career.protocolNumber ?: return
        api.personalData.putCareerByStudent(
            studentId = studentProfile.studentId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3CareerParameters(
                protocolNumber = protocolNumber
            )
        )
    }

    @Test
    @Disabled("Destructive: putHandicapDeclaration would insert/update handicap declaration")
    suspend fun putHandicapDeclaration() {
        val personId = studentProfile.personId
        api.personalData.putHandicapDeclaration(
            personId = personId,
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3HandicapDeclarationPut(
                handicapDeclarationState = "RICHIESTA"
            )
        )
    }

    @Test
    @Disabled("Destructive: putTitles would import titles for the student")
    suspend fun putTitles() {
        api.personalData.putTitles(
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3TitlesInsertion(
                personId = studentProfile.personId
            )
        )
    }

    @Test
    @Disabled("Destructive: putExternalSubject would modify external subject data")
    suspend fun putExternalSubject() {
        val subjects = api.personalData.getExternalSubject(limit = 1)
        val subject = subjects.firstOrNull() ?: return
        api.personalData.putExternalSubject(
            body = it.attendance100.mybicocca.data.dto.esse3.Esse3PutExternalSubject(
                externalSubjectId = subject.externalSubjectId,
                surname = subject.surname ?: "Unknown",
                name = subject.name ?: "Unknown",
                consentSmsFlag = 0L
            )
        )
    }

    @Test
    @Disabled("Destructive: putExternalSubjectConsents would modify external subject consents")
    suspend fun putExternalSubjectConsents() {
        val subjects = api.personalData.getExternalSubject(limit = 1)
        val subject = subjects.firstOrNull() ?: return
        val subjectId = subject.externalSubjectId ?: return
        api.personalData.putExternalSubjectConsents(
            externalSubjectId = subjectId.toInt(),
            body = emptyList(),
            webProcedureCode = "IMMATRICOLA"
        )
    }

    // ─── Refresh token ───────────────────────────────────────────────────────

    @Test
    suspend fun refreshToken() {
        try {
            val applicationId = "/myapp"
            val result = api.personalData.refreshToken(applicationId)
            logger.info("refreshToken($applicationId): activationUrl=${result.activationUrl}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("refreshToken not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("refreshToken validation error: ${e.message}")
        }
    }
}
