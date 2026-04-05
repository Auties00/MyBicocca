package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3AppointmentsCalendarApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3AppointmentsCalendarApiTest::class.java.name)
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
    fun testGetCalendarList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.AUTHENTICATED_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val calendarContext = "ESAMI"

            logger.info("testGetCalendarList: calling getCalendarList(calendarContext=$calendarContext) with defaults")
            val defaultResult = api.appointmentsCalendar.getCalendarList(calendarContext = calendarContext)
            logger.info("testGetCalendarList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getCalendarList() default result should not be null")
            for (item in defaultResult) {
                logger.info("testGetCalendarList: calendarCallTypeCode=${item.calendarCallTypeCode}, calendarCallTypeDescription=${item.calendarCallTypeDescription}, enrollmentCancellationCalendarModeId=${item.enrollmentCancellationCalendarModeId}")
            }

            logger.info("testGetCalendarList: calling getCalendarList() with language=it")
            val resultWithLanguage = api.appointmentsCalendar.getCalendarList(
                calendarContext = calendarContext,
                language = "it"
            )
            logger.info("testGetCalendarList: result with language size=${resultWithLanguage.size}")
            assertNotNull(resultWithLanguage, "getCalendarList() with language should not be null")

            logger.info("testGetCalendarList: calling getCalendarList() with language=en")
            val resultWithEnglish = api.appointmentsCalendar.getCalendarList(
                calendarContext = calendarContext,
                language = "en"
            )
            logger.info("testGetCalendarList: result with English language size=${resultWithEnglish.size}")
            assertNotNull(resultWithEnglish, "getCalendarList() with English language should not be null")

            if (defaultResult.isNotEmpty()) {
                logger.info("testGetCalendarList: calling getCalendarList() with siteId=1")
                val resultWithSiteId = api.appointmentsCalendar.getCalendarList(
                    calendarContext = calendarContext,
                    siteId = 1
                )
                logger.info("testGetCalendarList: result with siteId size=${resultWithSiteId.size}")
                assertNotNull(resultWithSiteId, "getCalendarList() with siteId should not be null")
            }

            logger.info("testGetCalendarList: calling getCalendarList() with pagination start=0, limit=5")
            val paginatedResult = api.appointmentsCalendar.getCalendarList(
                calendarContext = calendarContext,
                start = 0,
                limit = 5
            )
            logger.info("testGetCalendarList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetCalendarList: calling getCalendarList() with order=tipoCalAppCod")
            val orderedResult = api.appointmentsCalendar.getCalendarList(
                calendarContext = calendarContext,
                order = "tipoCalAppCod"
            )
            logger.info("testGetCalendarList: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getCalendarList() with order should not be null")
        } else {
            logger.info("testGetCalendarList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.getCalendarList(calendarContext = "ESAMI")
            }
            logger.info("testGetCalendarList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBookingsList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val calendarContext = "ESAMI"
            val personId = studentProfile.personId

            logger.info("testGetBookingsList: calling getBookingsList(calendarContext=$calendarContext, personId=$personId) with defaults")
            val defaultResult = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId
            )
            logger.info("testGetBookingsList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getBookingsList() default result should not be null")
            for (item in defaultResult) {
                logger.info("testGetBookingsList: calendarCallTypeCode=${item.calendarCallTypeCode}, calendarCallTypeDescription=${item.calendarCallTypeDescription}, day=${item.day}, site=${item.site}, bookingDate=${item.bookingDate}")
            }

            logger.info("testGetBookingsList: calling getBookingsList() with language=it")
            val resultWithLanguage = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                language = "it"
            )
            logger.info("testGetBookingsList: result with language size=${resultWithLanguage.size}")
            assertNotNull(resultWithLanguage, "getBookingsList() with language should not be null")

            logger.info("testGetBookingsList: calling getBookingsList() with studentId=${studentProfile.studentId}")
            val resultWithStudentId = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                studentId = studentProfile.studentId
            )
            logger.info("testGetBookingsList: result with studentId size=${resultWithStudentId.size}")
            assertNotNull(resultWithStudentId, "getBookingsList() with studentId should not be null")

            logger.info("testGetBookingsList: calling getBookingsList() with courseOfStudyId=${studentProfile.degreeCourseId}")
            val resultWithCourseId = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                courseOfStudyId = studentProfile.degreeCourseId
            )
            logger.info("testGetBookingsList: result with courseOfStudyId size=${resultWithCourseId.size}")
            assertNotNull(resultWithCourseId, "getBookingsList() with courseOfStudyId should not be null")

            logger.info("testGetBookingsList: calling getBookingsList() with enrollmentId=${studentProfile.enrollmentId}")
            val resultWithEnrollmentId = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                enrollmentId = studentProfile.enrollmentId
            )
            logger.info("testGetBookingsList: result with enrollmentId size=${resultWithEnrollmentId.size}")
            assertNotNull(resultWithEnrollmentId, "getBookingsList() with enrollmentId should not be null")

            logger.info("testGetBookingsList: calling getBookingsList() with siteId=1")
            val resultWithSiteId = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                siteId = 1
            )
            logger.info("testGetBookingsList: result with siteId size=${resultWithSiteId.size}")
            assertNotNull(resultWithSiteId, "getBookingsList() with siteId should not be null")

            logger.info("testGetBookingsList: calling getBookingsList() with pagination start=0, limit=5")
            val paginatedResult = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                start = 0,
                limit = 5
            )
            logger.info("testGetBookingsList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetBookingsList: calling getBookingsList() with order=giorno")
            val orderedResult = api.appointmentsCalendar.getBookingsList(
                calendarContext = calendarContext,
                personId = personId,
                order = "giorno"
            )
            logger.info("testGetBookingsList: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getBookingsList() with order should not be null")
        } else {
            logger.info("testGetBookingsList: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.getBookingsList(
                    calendarContext = "ESAMI",
                    personId = studentProfile.personId
                )
            }
            logger.info("testGetBookingsList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetShiftsList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.AUTHENTICATED_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val calendarListResult = api.appointmentsCalendar.getCalendarList(calendarContext = "ESAMI")
            logger.info("testGetShiftsList: fetched calendar list to derive calendarTypeCode, size=${calendarListResult.size}")

            val calendarTypeCode = calendarListResult.firstOrNull()?.calendarCallTypeCode ?: "ESAMI"
            logger.info("testGetShiftsList: using calendarTypeCode=$calendarTypeCode")

            logger.info("testGetShiftsList: calling getShiftsList(calendarTypeCode=$calendarTypeCode) with defaults")
            val defaultResult = api.appointmentsCalendar.getShiftsList(calendarTypeCode = calendarTypeCode)
            logger.info("testGetShiftsList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getShiftsList() default result should not be null")
            for (item in defaultResult) {
                logger.info("testGetShiftsList: calendarCallId=${item.calendarCallId}, calendarCallTypeCode=${item.calendarCallTypeCode}, calendarCallCode=${item.calendarCallCode}, calendarCallDescription=${item.calendarCallDescription}, shiftStartDate=${item.shiftStartDate}, shiftEndDate=${item.shiftEndDate}, siteDescription=${item.siteDescription}")
            }

            for (calendarEntry in calendarListResult) {
                val typeCode = calendarEntry.calendarCallTypeCode ?: continue
                if (typeCode == calendarTypeCode) continue
                logger.info("testGetShiftsList: calling getShiftsList(calendarTypeCode=$typeCode) for additional context")
                val additionalResult = api.appointmentsCalendar.getShiftsList(calendarTypeCode = typeCode)
                logger.info("testGetShiftsList: result for calendarTypeCode=$typeCode size=${additionalResult.size}")
                assertNotNull(additionalResult, "getShiftsList() for calendarTypeCode=$typeCode should not be null")
            }

            logger.info("testGetShiftsList: calling getShiftsList() with language=it")
            val resultWithLanguage = api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = calendarTypeCode,
                language = "it"
            )
            logger.info("testGetShiftsList: result with language size=${resultWithLanguage.size}")
            assertNotNull(resultWithLanguage, "getShiftsList() with language should not be null")

            logger.info("testGetShiftsList: calling getShiftsList() with language=en")
            val resultWithEnglish = api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = calendarTypeCode,
                language = "en"
            )
            logger.info("testGetShiftsList: result with English language size=${resultWithEnglish.size}")
            assertNotNull(resultWithEnglish, "getShiftsList() with English language should not be null")

            if (defaultResult.isNotEmpty()) {
                logger.info("testGetShiftsList: calling getShiftsList() with siteId=1")
                val resultWithSiteId = api.appointmentsCalendar.getShiftsList(
                    calendarTypeCode = calendarTypeCode,
                    siteId = 1
                )
                logger.info("testGetShiftsList: result with siteId size=${resultWithSiteId.size}")
                assertNotNull(resultWithSiteId, "getShiftsList() with siteId should not be null")

                logger.info("testGetShiftsList: calling getShiftsList() with administrativeStructureId=1")
                val resultWithStructure = api.appointmentsCalendar.getShiftsList(
                    calendarTypeCode = calendarTypeCode,
                    administrativeStructureId = 1
                )
                logger.info("testGetShiftsList: result with administrativeStructureId size=${resultWithStructure.size}")
                assertNotNull(resultWithStructure, "getShiftsList() with administrativeStructureId should not be null")
            }

            logger.info("testGetShiftsList: calling getShiftsList() with pagination start=0, limit=5")
            val paginatedResult = api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = calendarTypeCode,
                start = 0,
                limit = 5
            )
            logger.info("testGetShiftsList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetShiftsList: calling getShiftsList() with order=dataIniTurno")
            val orderedResult = api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = calendarTypeCode,
                order = "dataIniTurno"
            )
            logger.info("testGetShiftsList: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getShiftsList() with order should not be null")
        } else {
            logger.info("testGetShiftsList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.getShiftsList(calendarTypeCode = "ESAMI")
            }
            logger.info("testGetShiftsList: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testInsertAppointment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testInsertAppointment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testInsertAppointment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.insertAppointment(
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3Appointment(
                        shiftId = 1
                    )
                )
            }
            logger.info("testInsertAppointment: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testUpdateAppointment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testUpdateAppointment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testUpdateAppointment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.updateAppointment(
                    callId = 1,
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3Appointment(
                        shiftId = 1
                    )
                )
            }
            logger.info("testUpdateAppointment: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testCancelAppointment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testCancelAppointment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testCancelAppointment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.appointmentsCalendar.cancelAppointment(
                    callId = 1,
                    personId = studentProfile.personId
                )
            }
            logger.info("testCancelAppointment: Esse3Exception thrown as expected")
        }
    }
}
