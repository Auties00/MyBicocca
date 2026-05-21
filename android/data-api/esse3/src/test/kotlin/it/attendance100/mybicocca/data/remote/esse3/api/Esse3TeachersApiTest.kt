package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostTeacherSchedule
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PutTeacherNotes
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeacherParameters
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3TeachersApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3TeachersApiTest::class.java.name)
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
    fun testGetLecturers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturers: calling getLecturers() with defaults")
            val defaultResult = api.teachers.getLecturers()
            logger.info("testGetLecturers: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLecturers() default should return non-empty list")
            for (teacher in defaultResult.take(3)) {
                logger.info("testGetLecturers: lecturerId=${teacher.lecturerId}, surname=${teacher.lecturerSurname}, name=${teacher.lecturerName}, email=${teacher.email}, roleCode=${teacher.lecturerRoleCode}")
            }

            logger.info("testGetLecturers: calling getLecturers() with pagination start=0, limit=5")
            val paginatedResult = api.teachers.getLecturers(start = 0, limit = 5)
            logger.info("testGetLecturers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            val firstTeacher = defaultResult.first()

            if (firstTeacher.lecturerSurname != null) {
                logger.info("testGetLecturers: calling getLecturers() with lecturerSurname=${firstTeacher.lecturerSurname}")
                val filteredBySurname = api.teachers.getLecturers(lecturerSurname = firstTeacher.lecturerSurname)
                logger.info("testGetLecturers: filtered by surname result size=${filteredBySurname.size}")
                assertTrue(filteredBySurname.isNotEmpty(), "Filtered by surname should return non-empty list")
            }

            if (firstTeacher.lecturerMatricola != null) {
                logger.info("testGetLecturers: calling getLecturers() with lecturerMatricola=${firstTeacher.lecturerMatricola}")
                val filteredByMatricola = api.teachers.getLecturers(lecturerMatricola = firstTeacher.lecturerMatricola)
                logger.info("testGetLecturers: filtered by matricola result size=${filteredByMatricola.size}")
                assertTrue(filteredByMatricola.isNotEmpty(), "Filtered by matricola should return non-empty list")
            }

            if (firstTeacher.lecturerRoleCode != null) {
                logger.info("testGetLecturers: calling getLecturers() with lecturerRolesCode=[${firstTeacher.lecturerRoleCode}]")
                val filteredByRole = api.teachers.getLecturers(lecturerRolesCode = listOf(firstTeacher.lecturerRoleCode!!))
                logger.info("testGetLecturers: filtered by role result size=${filteredByRole.size}")
                assertTrue(filteredByRole.isNotEmpty(), "Filtered by lecturerRolesCode should return non-empty list")
            }

            logger.info("testGetLecturers: calling getLecturers() with order=docenteCognome")
            val orderedResult = api.teachers.getLecturers(order = "docenteCognome", start = 0, limit = 5)
            logger.info("testGetLecturers: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should not be empty")
        } else {
            logger.info("testGetLecturers: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.getLecturers()
            }
            logger.info("testGetLecturers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerRoles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerRoles: calling getLecturerRoles() with defaults")
            val defaultResult = api.teachers.getLecturerRoles()
            logger.info("testGetLecturerRoles: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLecturerRoles() default should return non-empty list")
            for (role in defaultResult.take(5)) {
                logger.info("testGetLecturerRoles: roleCode=${role.lecturerRoleCode}, description=${role.lecturerRoleDescription}, typeCode=${role.lecturerRoleTypeCode}, csaCode=${role.csaCode}")
            }

            logger.info("testGetLecturerRoles: calling getLecturerRoles() with pagination start=0, limit=5")
            val paginatedResult = api.teachers.getLecturerRoles(start = 0, limit = 5)
            logger.info("testGetLecturerRoles: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            val firstRole = defaultResult.first()

            if (firstRole.lecturerRoleCode != null) {
                logger.info("testGetLecturerRoles: calling getLecturerRoles() with lecturerRoleCode=${firstRole.lecturerRoleCode}")
                val filteredByCode = api.teachers.getLecturerRoles(lecturerRoleCode = firstRole.lecturerRoleCode)
                logger.info("testGetLecturerRoles: filtered by code result size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by lecturerRoleCode should return non-empty list")
            }

            if (firstRole.lecturerRoleTypeCode != null) {
                logger.info("testGetLecturerRoles: calling getLecturerRoles() with lecturerRoleTypeCode=${firstRole.lecturerRoleTypeCode}")
                val filteredByType = api.teachers.getLecturerRoles(lecturerRoleTypeCode = firstRole.lecturerRoleTypeCode)
                logger.info("testGetLecturerRoles: filtered by type result size=${filteredByType.size}")
                assertTrue(filteredByType.isNotEmpty(), "Filtered by lecturerRoleTypeCode should return non-empty list")
            }

            if (firstRole.csaCode != null) {
                logger.info("testGetLecturerRoles: calling getLecturerRoles() with csaCode=${firstRole.csaCode}")
                val filteredByCsa = api.teachers.getLecturerRoles(csaCode = firstRole.csaCode)
                logger.info("testGetLecturerRoles: filtered by csaCode result size=${filteredByCsa.size}")
                assertTrue(filteredByCsa.isNotEmpty(), "Filtered by csaCode should return non-empty list")
            }

            logger.info("testGetLecturerRoles: calling getLecturerRoles() with order=ruoloDocCod")
            val orderedResult = api.teachers.getLecturerRoles(order = "ruoloDocCod", start = 0, limit = 5)
            logger.info("testGetLecturerRoles: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should not be empty")
        } else {
            logger.info("testGetLecturerRoles: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.getLecturerRoles()
            }
            logger.info("testGetLecturerRoles: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturer: first fetching a lecturerId from getLecturers()")
            val lecturers = api.teachers.getLecturers(start = 0, limit = 1)
            assertTrue(lecturers.isNotEmpty(), "Need at least one lecturer to test getLecturer()")
            val lecturerId = lecturers.first().lecturerId
            assertNotNull(lecturerId, "lecturerId should not be null")
            logger.info("testGetLecturer: using lecturerId=$lecturerId")

            logger.info("testGetLecturer: calling getLecturer() with defaults")
            val defaultResult = api.teachers.getLecturer(lecturerId = lecturerId)
            logger.info("testGetLecturer: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLecturer() should return non-empty list")
            val lecturer = defaultResult.first()
            logger.info("testGetLecturer: lecturerId=${lecturer.lecturerId}, surname=${lecturer.lecturerSurname}, name=${lecturer.lecturerName}, email=${lecturer.email}, roleCode=${lecturer.lecturerRoleCode}, facultyDescription=${lecturer.facultyDescription}")
            assertNotNull(lecturer.lecturerId, "lecturerId should not be null in result")

            logger.info("testGetLecturer: calling getLecturer() with optionalFields=docentiRecapiti,docentiNote,orario")
            val resultWithOptFields = api.teachers.getLecturer(
                lecturerId = lecturerId,
                optionalFields = "docentiRecapiti,docentiNote,orario"
            )
            logger.info("testGetLecturer: result with optionalFields size=${resultWithOptFields.size}")
            if (resultWithOptFields.isNotEmpty()) {
                val detailed = resultWithOptFields.first()
                logger.info("testGetLecturer: contacts size=${detailed.lecturersContacts.size}, notes size=${detailed.lecturersNotes.size}, schedule size=${detailed.schedule.size}")
            }
        } else {
            logger.info("testGetLecturer: user lacks required permissions (TECHNICAL_USER or TEACHER), expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.getLecturer(lecturerId = 1)
            }
            logger.info("testGetLecturer: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutLecturer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutLecturer: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutLecturer: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.putLecturer(
                    lecturerId = 1,
                    body = Esse3TeacherParameters(email = "test@example.com")
                )
            }
            logger.info("testPutLecturer: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerNotes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerNotes: first fetching a lecturerId from getLecturers()")
            val lecturers = api.teachers.getLecturers(start = 0, limit = 1)
            assertTrue(lecturers.isNotEmpty(), "Need at least one lecturer to test getLecturerNotes()")
            val lecturerId = lecturers.first().lecturerId
            assertNotNull(lecturerId, "lecturerId should not be null")
            logger.info("testGetLecturerNotes: using lecturerId=$lecturerId")

            logger.info("testGetLecturerNotes: calling getLecturerNotes()")
            val result = api.teachers.getLecturerNotes(lecturerId = lecturerId)
            logger.info("testGetLecturerNotes: lecturerId=${result.lecturerId}, biographicalNotes=${result.biographicalNotes?.take(50)}, publicationsNotes=${result.publicationsNotes?.take(50)}, curriculumNotes=${result.curriculumNotes?.take(50)}, lecturerNotes=${result.lecturerNotes?.take(50)}")
            assertNotNull(result.lecturerId, "lecturerId in notes should not be null")
        } else {
            logger.info("testGetLecturerNotes: user lacks required permissions (TECHNICAL_USER or TEACHER), expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.getLecturerNotes(lecturerId = 1)
            }
            logger.info("testGetLecturerNotes: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutLecturerNotes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutLecturerNotes: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutLecturerNotes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.putLecturerNotes(
                    lecturerId = 1,
                    body = Esse3PutTeacherNotes(
                        biographicalNotes = "Test",
                        publicationsNotes = "Test",
                        curriculumNotes = "Test",
                        lecturerNotes = "Test"
                    )
                )
            }
            logger.info("testPutLecturerNotes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturerSchedule() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerSchedule: first fetching a lecturerId from getLecturers()")
            val lecturers = api.teachers.getLecturers(start = 0, limit = 1)
            assertTrue(lecturers.isNotEmpty(), "Need at least one lecturer to test getLecturerSchedule()")
            val lecturerId = lecturers.first().lecturerId
            assertNotNull(lecturerId, "lecturerId should not be null")
            logger.info("testGetLecturerSchedule: using lecturerId=$lecturerId")

            logger.info("testGetLecturerSchedule: calling getLecturerSchedule() with defaults")
            val defaultResult = api.teachers.getLecturerSchedule(lecturerId = lecturerId)
            logger.info("testGetLecturerSchedule: default result size=${defaultResult.size}")
            for (entry in defaultResult.take(5)) {
                logger.info("testGetLecturerSchedule: scheduleId=${entry.lecturerScheduleId}, lecturerId=${entry.lecturerId}, day=${entry.day}, dayDescription=${entry.dayDescription}, startTime=${entry.startTime}, endTime=${entry.endTime}, place=${entry.placeDescription}, note=${entry.note}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstEntry = defaultResult.first()
                if (firstEntry.day != null) {
                    logger.info("testGetLecturerSchedule: calling getLecturerSchedule() with day=${firstEntry.day}")
                    val filteredByDay = api.teachers.getLecturerSchedule(
                        lecturerId = lecturerId,
                        day = firstEntry.day.toInt()
                    )
                    logger.info("testGetLecturerSchedule: filtered by day result size=${filteredByDay.size}")
                    assertTrue(filteredByDay.isNotEmpty(), "Filtered by day should return non-empty list")
                }
            }
        } else {
            logger.info("testGetLecturerSchedule: user lacks required permissions (TECHNICAL_USER or TEACHER), expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.getLecturerSchedule(lecturerId = 1)
            }
            logger.info("testGetLecturerSchedule: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostLecturerSchedule() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostLecturerSchedule: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostLecturerSchedule: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.postLecturerSchedule(
                    lecturerId = 1,
                    body = listOf(
                        Esse3PostTeacherSchedule(
                            day = 1,
                            startTime = "09:00",
                            endTime = "10:00",
                            placeDescription = "Test Room",
                            note = "Test note"
                        )
                    )
                )
            }
            logger.info("testPostLecturerSchedule: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutLecturerSchedule() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutLecturerSchedule: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutLecturerSchedule: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.putLecturerSchedule(
                    lecturerId = 1,
                    body = Esse3PostTeacherSchedule(
                        day = 1,
                        startTime = "09:00",
                        endTime = "10:00",
                        placeDescription = "Test Room",
                        note = "Test note"
                    ),
                    day = 1,
                    lecturerResearchScheduleId = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            }
            logger.info("testPutLecturerSchedule: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteLecturerSchedule() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteLecturerSchedule: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteLecturerSchedule: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.teachers.deleteLecturerSchedule(
                    lecturerId = 1,
                    day = 1,
                    lecturerResearchScheduleId = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            }
            logger.info("testDeleteLecturerSchedule: Esse3Exception thrown as expected")
        }
    }
}
