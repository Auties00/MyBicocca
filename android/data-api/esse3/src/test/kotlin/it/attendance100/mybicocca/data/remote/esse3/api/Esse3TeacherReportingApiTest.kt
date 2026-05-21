package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3TeacherReportingApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3TeacherReportingApiTest::class.java.name)
        private lateinit var userPermissions: Set<Esse3PermissionLevel>
    }

    @BeforeAll
    fun setUp() = runTest {
        logger.info("Logging in to derive user permissions")
        val loginResult = api.auth.login()
        logger.info("Login successful: userId=${loginResult.user.userId}, groupId=${loginResult.user.groupId}")

        val permissions = mutableSetOf(
            Esse3PermissionLevel.ANY,
            Esse3PermissionLevel.AUTHENTICATED_USER,
            Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        )
        for (profile in loginResult.profiles) {
            val description = profile.description
            if (description != null) {
                permissions.add(Esse3PermissionLevel.fromProfileName(description))
            }
        }
        userPermissions = permissions
        logger.info("Derived user permissions: $userPermissions")
    }

    @Test
    fun testFilterLecturerDiaryDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryDefaults: calling filterLecturerDiary() with no params")
            val result = api.teacherReporting.filterLecturerDiary()
            logger.info("testFilterLecturerDiaryDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "filterLecturerDiary() default should return non-empty list")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryDefaults: diaryId=${diary.diaryId}, academicYearId=${diary.academicYearId}, lecturerId=${diary.lecturerId}, surname=${diary.surname}, name=${diary.name}, diaryStateCode=${diary.diaryStateCode}, detailedHours=${diary.detailedHours}")
                assertNotNull(diary.diaryId, "diaryId should not be null")
            }
        } else {
            logger.info("testFilterLecturerDiaryDefaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary()
            }
            logger.info("testFilterLecturerDiaryDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryPagination: calling filterLecturerDiary() with start=0, limit=5")
            val result = api.teacherReporting.filterLecturerDiary(start = 0, limit = 5)
            logger.info("testFilterLecturerDiaryPagination: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryPagination: diaryId=${diary.diaryId}, academicYearId=${diary.academicYearId}, surname=${diary.surname}, name=${diary.name}")
            }
        } else {
            logger.info("testFilterLecturerDiaryPagination: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(start = 0, limit = 5)
            }
            logger.info("testFilterLecturerDiaryPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryWithAcademicYearId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryWithAcademicYearId: calling filterLecturerDiary() with academicYearId=2025")
            val result = api.teacherReporting.filterLecturerDiary(academicYearId = 2025)
            logger.info("testFilterLecturerDiaryWithAcademicYearId: result size=${result.size}")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryWithAcademicYearId: diaryId=${diary.diaryId}, academicYearId=${diary.academicYearId}, surname=${diary.surname}, diaryStateCode=${diary.diaryStateCode}")
            }
        } else {
            logger.info("testFilterLecturerDiaryWithAcademicYearId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(academicYearId = 2025)
            }
            logger.info("testFilterLecturerDiaryWithAcademicYearId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryWithSurname() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryWithSurname: calling filterLecturerDiary() with surname=Rossi")
            val result = api.teacherReporting.filterLecturerDiary(surname = "Rossi")
            logger.info("testFilterLecturerDiaryWithSurname: result size=${result.size}")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryWithSurname: diaryId=${diary.diaryId}, surname=${diary.surname}, name=${diary.name}, fiscalCode=${diary.fiscalCode}")
            }
        } else {
            logger.info("testFilterLecturerDiaryWithSurname: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(surname = "Rossi")
            }
            logger.info("testFilterLecturerDiaryWithSurname: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryWithDiaryStatusCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryWithDiaryStatusCode: calling filterLecturerDiary() with diaryStatusCode=A")
            val result = api.teacherReporting.filterLecturerDiary(diaryStatusCode = "A")
            logger.info("testFilterLecturerDiaryWithDiaryStatusCode: result size=${result.size}")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryWithDiaryStatusCode: diaryId=${diary.diaryId}, diaryStateCode=${diary.diaryStateCode}, diaryStateDescription=${diary.diaryStateDescription}")
            }
        } else {
            logger.info("testFilterLecturerDiaryWithDiaryStatusCode: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(diaryStatusCode = "A")
            }
            logger.info("testFilterLecturerDiaryWithDiaryStatusCode: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryWithOrder() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryWithOrder: calling filterLecturerDiary() with order=+cognome, start=0, limit=5")
            val result = api.teacherReporting.filterLecturerDiary(order = "+cognome", start = 0, limit = 5)
            logger.info("testFilterLecturerDiaryWithOrder: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryWithOrder: diaryId=${diary.diaryId}, surname=${diary.surname}, name=${diary.name}")
            }
        } else {
            logger.info("testFilterLecturerDiaryWithOrder: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(order = "+cognome", start = 0, limit = 5)
            }
            logger.info("testFilterLecturerDiaryWithOrder: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerDiaryWithFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerDiaryWithFields: calling filterLecturerDiary() with fields param")
            val result = api.teacherReporting.filterLecturerDiary(fields = "diarioId,aaId,cognome,nome")
            logger.info("testFilterLecturerDiaryWithFields: result size=${result.size}")
            for (diary in result) {
                logger.info("testFilterLecturerDiaryWithFields: diaryId=${diary.diaryId}, academicYearId=${diary.academicYearId}, surname=${diary.surname}, name=${diary.name}")
            }
        } else {
            logger.info("testFilterLecturerDiaryWithFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerDiary(fields = "diarioId,aaId,cognome,nome")
            }
            logger.info("testFilterLecturerDiaryWithFields: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerDiary() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerDiary: fetching diary list first to obtain a valid diaryId")
            val diaries = api.teacherReporting.filterLecturerDiary(start = 0, limit = 1)
            assertTrue(diaries.isNotEmpty(), "Need at least one diary to test getLecturerDiary()")
            val diaryId = diaries.first().diaryId
            logger.info("testGetLecturerDiary: calling getLecturerDiary() with diaryId=$diaryId")

            val result = api.teacherReporting.getLecturerDiary(diaryId = diaryId)
            logger.info("testGetLecturerDiary: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getLecturerDiary() should return non-empty list for a valid diaryId")
            for (diary in result) {
                logger.info("testGetLecturerDiary: diaryId=${diary.diaryId}, academicYearId=${diary.academicYearId}, surname=${diary.surname}, name=${diary.name}, diaryStateCode=${diary.diaryStateCode}, detailedHours=${diary.detailedHours}, annualDetailedHours=${diary.annualDetailedHours}")
                logger.info("testGetLecturerDiary: activity count=${diary.activity.size}, annualActivities count=${diary.annualActivities.size}, notes count=${diary.notes.size}")
                for (activity in diary.activity) {
                    logger.info("testGetLecturerDiary: activity diaryDetailId=${activity.diaryDetailId}, activityTypeCode=${activity.activityTypeCode}, date=${activity.date}, hours=${activity.hours}, minutes=${activity.minutes}")
                }
                for (annualActivity in diary.annualActivities) {
                    logger.info("testGetLecturerDiary: annualActivity academicYearDiaryDetailId=${annualActivity.academicYearDiaryDetailId}, activityTypeCode=${annualActivity.activityTypeCode}, hours=${annualActivity.hours}, predictedHours=${annualActivity.predictedHours}")
                }
                for (note in diary.notes) {
                    logger.info("testGetLecturerDiary: note diaryNoteId=${note.diaryNoteId}, note=${note.note}")
                }
            }
        } else {
            logger.info("testGetLecturerDiary: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.getLecturerDiary(diaryId = 1L)
            }
            logger.info("testGetLecturerDiary: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersDefaults: calling filterLecturerRegisters() with no params")
            val result = api.teacherReporting.filterLecturerRegisters()
            logger.info("testFilterLecturerRegistersDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "filterLecturerRegisters() default should return non-empty list")
            for (register in result) {
                logger.info("testFilterLecturerRegistersDefaults: registrationId=${register.registrationId}, academicYearOfferId=${register.academicYearOfferId}, lecturerId=${register.lecturerId}, surname=${register.surname}, name=${register.name}, regulationStateCode=${register.regulationStateCode}")
                assertNotNull(register.registrationId, "registrationId should not be null")
            }
        } else {
            logger.info("testFilterLecturerRegistersDefaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters()
            }
            logger.info("testFilterLecturerRegistersDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersPagination: calling filterLecturerRegisters() with start=0, limit=5")
            val result = api.teacherReporting.filterLecturerRegisters(start = 0, limit = 5)
            logger.info("testFilterLecturerRegistersPagination: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (register in result) {
                logger.info("testFilterLecturerRegistersPagination: registrationId=${register.registrationId}, surname=${register.surname}, name=${register.name}, regulationStateCode=${register.regulationStateCode}")
            }
        } else {
            logger.info("testFilterLecturerRegistersPagination: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(start = 0, limit = 5)
            }
            logger.info("testFilterLecturerRegistersPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithAcademicYearOfferId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithAcademicYearOfferId: calling filterLecturerRegisters() with academicYearOfferId=2025")
            val result = api.teacherReporting.filterLecturerRegisters(academicYearOfferId = 2025)
            logger.info("testFilterLecturerRegistersWithAcademicYearOfferId: result size=${result.size}")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithAcademicYearOfferId: registrationId=${register.registrationId}, academicYearOfferId=${register.academicYearOfferId}, surname=${register.surname}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithAcademicYearOfferId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(academicYearOfferId = 2025)
            }
            logger.info("testFilterLecturerRegistersWithAcademicYearOfferId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithSurname() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithSurname: calling filterLecturerRegisters() with surname=Rossi")
            val result = api.teacherReporting.filterLecturerRegisters(surname = "Rossi")
            logger.info("testFilterLecturerRegistersWithSurname: result size=${result.size}")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithSurname: registrationId=${register.registrationId}, surname=${register.surname}, name=${register.name}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithSurname: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(surname = "Rossi")
            }
            logger.info("testFilterLecturerRegistersWithSurname: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithRegulationStatusCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithRegulationStatusCode: calling filterLecturerRegisters() with regulationStatusCode=A")
            val result = api.teacherReporting.filterLecturerRegisters(regulationStatusCode = "A")
            logger.info("testFilterLecturerRegistersWithRegulationStatusCode: result size=${result.size}")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithRegulationStatusCode: registrationId=${register.registrationId}, regulationStateCode=${register.regulationStateCode}, regulationStateDescription=${register.regulationStateDescription}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithRegulationStatusCode: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(regulationStatusCode = "A")
            }
            logger.info("testFilterLecturerRegistersWithRegulationStatusCode: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithOrder() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithOrder: calling filterLecturerRegisters() with order=+cognome, start=0, limit=5")
            val result = api.teacherReporting.filterLecturerRegisters(order = "+cognome", start = 0, limit = 5)
            logger.info("testFilterLecturerRegistersWithOrder: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithOrder: registrationId=${register.registrationId}, surname=${register.surname}, name=${register.name}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithOrder: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(order = "+cognome", start = 0, limit = 5)
            }
            logger.info("testFilterLecturerRegistersWithOrder: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithFields: calling filterLecturerRegisters() with fields param")
            val result = api.teacherReporting.filterLecturerRegisters(fields = "regId,aaOffId,cognome,nome")
            logger.info("testFilterLecturerRegistersWithFields: result size=${result.size}")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithFields: registrationId=${register.registrationId}, academicYearOfferId=${register.academicYearOfferId}, surname=${register.surname}, name=${register.name}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(fields = "regId,aaOffId,cognome,nome")
            }
            logger.info("testFilterLecturerRegistersWithFields: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFilterLecturerRegistersWithOptionalFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFilterLecturerRegistersWithOptionalFields: calling filterLecturerRegisters() with optionalFields param")
            val result = api.teacherReporting.filterLecturerRegisters(optionalFields = "identificativiCoperture")
            logger.info("testFilterLecturerRegistersWithOptionalFields: result size=${result.size}")
            for (register in result) {
                logger.info("testFilterLecturerRegistersWithOptionalFields: registrationId=${register.registrationId}, coverageIdentifiers size=${register.coverageIdentifiers.size}, totalDidacticHours=${register.totalDidacticHours}, totalOtherHours=${register.totalOtherHours}")
            }
        } else {
            logger.info("testFilterLecturerRegistersWithOptionalFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.filterLecturerRegisters(optionalFields = "identificativiCoperture")
            }
            logger.info("testFilterLecturerRegistersWithOptionalFields: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerRegister() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerRegister: fetching register list first to obtain a valid registrationId")
            val registers = api.teacherReporting.filterLecturerRegisters(start = 0, limit = 1)
            assertTrue(registers.isNotEmpty(), "Need at least one register to test getLecturerRegister()")
            val registrationId = registers.first().registrationId
            logger.info("testGetLecturerRegister: calling getLecturerRegister() with registrationId=$registrationId")

            val result = api.teacherReporting.getLecturerRegister(registrationId = registrationId)
            logger.info("testGetLecturerRegister: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getLecturerRegister() should return non-empty list for a valid registrationId")
            for (register in result) {
                logger.info("testGetLecturerRegister: registrationId=${register.registrationId}, academicYearOfferId=${register.academicYearOfferId}, lecturerId=${register.lecturerId}, surname=${register.surname}, name=${register.name}")
                logger.info("testGetLecturerRegister: regulationStateCode=${register.regulationStateCode}, regulationStateDescription=${register.regulationStateDescription}, totalDidacticHours=${register.totalDidacticHours}, totalOtherHours=${register.totalOtherHours}")
                logger.info("testGetLecturerRegister: logistics count=${register.logistics.size}, activity count=${register.activity.size}")
                for (log in register.logistics) {
                    logger.info("testGetLecturerRegister: logistic courseOfStudyId=${log.courseOfStudyId}, activityId=${log.activityId}, activityCode=${log.activityCode}, activityDescription=${log.activityDescription}")
                }
                for (activity in register.activity) {
                    logger.info("testGetLecturerRegister: activity ruleDetailId=${activity.ruleDetailId}, activityTypeCode=${activity.activityTypeCode}, date=${activity.date}, startTime=${activity.startTime}, endTime=${activity.endTime}, academicHours=${activity.academicHours}")
                }
            }
        } else {
            logger.info("testGetLecturerRegister: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.getLecturerRegister(registrationId = 1L)
            }
            logger.info("testGetLecturerRegister: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerRegisterWithFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerRegisterWithFields: fetching register list first to obtain a valid registrationId")
            val registers = api.teacherReporting.filterLecturerRegisters(start = 0, limit = 1)
            assertTrue(registers.isNotEmpty(), "Need at least one register to test getLecturerRegister() with fields")
            val registrationId = registers.first().registrationId
            logger.info("testGetLecturerRegisterWithFields: calling getLecturerRegister() with registrationId=$registrationId and fields param")

            val result = api.teacherReporting.getLecturerRegister(
                registrationId = registrationId,
                fields = "regId,aaOffId,cognome,nome,statoRegCod"
            )
            logger.info("testGetLecturerRegisterWithFields: result size=${result.size}")
            for (register in result) {
                logger.info("testGetLecturerRegisterWithFields: registrationId=${register.registrationId}, academicYearOfferId=${register.academicYearOfferId}, surname=${register.surname}, name=${register.name}, regulationStateCode=${register.regulationStateCode}")
            }
        } else {
            logger.info("testGetLecturerRegisterWithFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.getLecturerRegister(
                    registrationId = 1L,
                    fields = "regId,aaOffId,cognome,nome,statoRegCod"
                )
            }
            logger.info("testGetLecturerRegisterWithFields: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerRegisterWithOptionalFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerRegisterWithOptionalFields: fetching register list first to obtain a valid registrationId")
            val registers = api.teacherReporting.filterLecturerRegisters(start = 0, limit = 1)
            assertTrue(registers.isNotEmpty(), "Need at least one register to test getLecturerRegister() with optionalFields")
            val registrationId = registers.first().registrationId
            logger.info("testGetLecturerRegisterWithOptionalFields: calling getLecturerRegister() with registrationId=$registrationId and optionalFields param")

            val result = api.teacherReporting.getLecturerRegister(
                registrationId = registrationId,
                optionalFields = "logistica,attivita"
            )
            logger.info("testGetLecturerRegisterWithOptionalFields: result size=${result.size}")
            for (register in result) {
                logger.info("testGetLecturerRegisterWithOptionalFields: registrationId=${register.registrationId}, logistics count=${register.logistics.size}, activity count=${register.activity.size}")
                for (log in register.logistics) {
                    logger.info("testGetLecturerRegisterWithOptionalFields: logistic courseOfStudyCode=${log.courseOfStudyCode}, courseOfStudyDescription=${log.courseOfStudyDescription}, teachingUnitCode=${log.teachingUnitCode}")
                }
                for (activity in register.activity) {
                    logger.info("testGetLecturerRegisterWithOptionalFields: activity ruleDetailId=${activity.ruleDetailId}, title=${activity.title}, description=${activity.description}, groups count=${activity.groups.size}")
                }
            }
        } else {
            logger.info("testGetLecturerRegisterWithOptionalFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teacherReporting.getLecturerRegister(
                    registrationId = 1L,
                    optionalFields = "logistica,attivita"
                )
            }
            logger.info("testGetLecturerRegisterWithOptionalFields: Esse3Exception thrown as expected")
        }
    }
}
