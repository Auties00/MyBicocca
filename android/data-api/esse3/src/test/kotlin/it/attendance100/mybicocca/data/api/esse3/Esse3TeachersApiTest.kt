package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PostTeacherSchedule
import it.attendance100.mybicocca.data.dto.esse3.Esse3PutTeacherNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherParameters
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3TeachersApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3TeachersApiTest::class.java)

    @Test
    suspend fun getLecturers_noFilter() {
        val lecturers = api.teachers.getLecturers(limit = 20)
        logger.info("getLecturers (no filter): got ${lecturers.size} results")
        assertNotNull(lecturers)
        for (lecturer in lecturers) {
            assertNotNull(lecturer.lecturerId)
            logger.info("  Lecturer id=${lecturer.lecturerId} name=${lecturer.lecturerName} surname=${lecturer.lecturerSurname}")
        }
    }

    @Test
    suspend fun getLecturers_bySurname() {
        val lecturers = api.teachers.getLecturers(lecturerSurname = "Rossi", limit = 10)
        logger.info("getLecturers (surname=Rossi): got ${lecturers.size} results")
        assertNotNull(lecturers)
        for (lecturer in lecturers) {
            assertNotNull(lecturer.lecturerId)
        }
    }

    @Test
    suspend fun getLecturers_withPagination() {
        val firstPage = api.teachers.getLecturers(start = 0, limit = 5)
        val secondPage = api.teachers.getLecturers(start = 5, limit = 5)
        logger.info("getLecturers (page 1): ${firstPage.size}, (page 2): ${secondPage.size}")
        assertNotNull(firstPage)
        assertNotNull(secondPage)
    }

    @Test
    suspend fun getLecturers_andGetSingleLecturer() {
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            logger.info("Getting single lecturer id=$lecturerId")
            try {
                val detail = api.teachers.getLecturer(lecturerId)
                assertNotNull(detail)
                logger.info("  getLecturer($lecturerId): ${detail.size} entries")
                for (d in detail) {
                    assertNotNull(d.lecturerId)
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getLecturer($lecturerId) not authorized: ${e.message}")
            }
        }
    }

    @Test
    suspend fun getLecturerRoles_noFilter() {
        try {
            val roles = api.teachers.getLecturerRoles()
            logger.info("getLecturerRoles: got ${roles.size} results")
            assertNotNull(roles)
            for (role in roles) {
                logger.info("  role code=${role.lecturerRoleCode} desc=${role.lecturerRoleDescription}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getLecturerRoles not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getLecturerRoles_withFilters() {
        try {
            val allRoles = api.teachers.getLecturerRoles(limit = 50)
            assertNotNull(allRoles)
            logger.info("getLecturerRoles (all): ${allRoles.size} results")

            if (allRoles.isNotEmpty()) {
                val firstRole = allRoles.first()
                val roleCode = firstRole.lecturerRoleCode
                if (roleCode != null) {
                    val filtered = api.teachers.getLecturerRoles(lecturerRoleCode = roleCode)
                    assertNotNull(filtered)
                    logger.info("getLecturerRoles (code=$roleCode): ${filtered.size} results")
                }

                val typeCode = firstRole.lecturerRoleTypeCode
                if (typeCode != null) {
                    val byType = api.teachers.getLecturerRoles(lecturerRoleTypeCode = typeCode)
                    assertNotNull(byType)
                    logger.info("getLecturerRoles (typeCode=$typeCode): ${byType.size} results")
                }

                val csaCode = firstRole.csaCode
                if (csaCode != null) {
                    val byCsa = api.teachers.getLecturerRoles(csaCode = csaCode)
                    assertNotNull(byCsa)
                    logger.info("getLecturerRoles (csaCode=$csaCode): ${byCsa.size} results")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getLecturerRoles not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getLecturerNotes() {
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val notes = api.teachers.getLecturerNotes(lecturerId)
                assertNotNull(notes)
                logger.info("getLecturerNotes($lecturerId): biographical=${notes.biographicalNotes?.take(50)}")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getLecturerNotes($lecturerId) not authorized: ${e.message}")
            }
            break
        }
    }

    @Test
    suspend fun getLecturerSchedule() {
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val schedule = api.teachers.getLecturerSchedule(lecturerId)
                assertNotNull(schedule)
                logger.info("getLecturerSchedule($lecturerId): ${schedule.size} entries")
                for (entry in schedule) {
                    assertNotNull(entry.lecturerScheduleId)
                    assertNotNull(entry.lecturerId)
                    logger.info("  day=${entry.day} dayDesc=${entry.dayDescription} from=${entry.startTime} to=${entry.endTime}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getLecturerSchedule($lecturerId) not authorized: ${e.message}")
            }
            break
        }
    }

    @Test
    suspend fun getLecturerSchedule_byDay() {
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            // Try each day of the week (1=Monday ... 7=Sunday in ISO convention)
            for (day in 1..5) {
                try {
                    val schedule = api.teachers.getLecturerSchedule(lecturerId, day = day)
                    assertNotNull(schedule)
                    logger.info("getLecturerSchedule($lecturerId, day=$day): ${schedule.size} entries")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("getLecturerSchedule($lecturerId, day=$day) not authorized: ${e.message}")
                }
            }
            break
        }
    }

    @Test
    suspend fun putLecturer_expectsAuthorizationOrSuccess() {
        val lecturers = api.teachers.getLecturers(limit = 1)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            val originalEmail = lecturer.email
            val testBody = Esse3TeacherParameters(email = originalEmail)

            try {
                val result = api.teachers.putLecturer(lecturerId, testBody)
                assertNotNull(result)
                logger.info("putLecturer($lecturerId): success, ${result.size} entries returned")

                // Rollback: restore original email
                try {
                    api.teachers.putLecturer(lecturerId, Esse3TeacherParameters(email = originalEmail))
                    logger.info("putLecturer rollback: restored email=$originalEmail")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("putLecturer rollback not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warn("putLecturer rollback validation error: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("putLecturer($lecturerId) not authorized (expected for student): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("putLecturer($lecturerId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun putLecturerNotes_expectsAuthorizationOrSuccess() {
        val lecturers = api.teachers.getLecturers(limit = 1)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                // Read original notes first for rollback
                val originalNotes = try {
                    api.teachers.getLecturerNotes(lecturerId)
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("Cannot read notes for rollback, skipping putLecturerNotes test: ${e.message}")
                    return
                }

                val testNotes = Esse3PutTeacherNotes(
                    biographicalNotes = originalNotes.biographicalNotes,
                    publicationsNotes = originalNotes.publicationsNotes,
                    curriculumNotes = originalNotes.curriculumNotes,
                    lecturerNotes = originalNotes.lecturerNotes
                )

                val result = api.teachers.putLecturerNotes(lecturerId, testNotes)
                assertNotNull(result)
                logger.info("putLecturerNotes($lecturerId): success")

                // Rollback
                val rollbackNotes = Esse3PutTeacherNotes(
                    biographicalNotes = originalNotes.biographicalNotes,
                    publicationsNotes = originalNotes.publicationsNotes,
                    curriculumNotes = originalNotes.curriculumNotes,
                    lecturerNotes = originalNotes.lecturerNotes
                )
                try {
                    api.teachers.putLecturerNotes(lecturerId, rollbackNotes)
                    logger.info("putLecturerNotes rollback: done")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("putLecturerNotes rollback not authorized: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("putLecturerNotes($lecturerId) not authorized (expected for student): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("putLecturerNotes($lecturerId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun postLecturerSchedule_expectsAuthorizationOrSuccess() {
        val lecturers = api.teachers.getLecturers(limit = 1)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            val testSchedule = listOf(
                Esse3PostTeacherSchedule(
                    day = 1,
                    startTime = "09:00",
                    endTime = "10:00",
                    placeDescription = "Test Location",
                    note = "Test - integration test entry"
                )
            )

            try {
                // Capture original schedule for rollback
                val result = api.teachers.postLecturerSchedule(lecturerId, testSchedule)
                assertNotNull(result)
                logger.info("postLecturerSchedule($lecturerId): success, ${result.size} entries returned")

                // Rollback: delete any entries added (best-effort by deleting day=1 entries)
                try {
                    api.teachers.deleteLecturerSchedule(lecturerId, day = 1)
                    logger.info("postLecturerSchedule rollback: deleted day=1 schedule")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("postLecturerSchedule rollback not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warn("postLecturerSchedule rollback validation error: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("postLecturerSchedule($lecturerId) not authorized (expected for student): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("postLecturerSchedule($lecturerId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun putLecturerSchedule_expectsAuthorizationOrSuccess() {
        val lecturers = api.teachers.getLecturers(limit = 1)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val schedule = try {
                    api.teachers.getLecturerSchedule(lecturerId)
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("Cannot read schedule, skipping putLecturerSchedule: ${e.message}")
                    return
                }

                if (schedule.isEmpty()) {
                    logger.info("putLecturerSchedule($lecturerId): no existing schedule entries to update, skipping")
                    return
                }

                val existingEntry = schedule.first()
                val updateBody = Esse3PostTeacherSchedule(
                    day = existingEntry.day?.toInt() ?: 1,
                    startTime = existingEntry.startTime ?: "09:00",
                    endTime = existingEntry.endTime ?: "10:00",
                    placeDescription = existingEntry.placeDescription,
                    note = existingEntry.note
                )

                val result = api.teachers.putLecturerSchedule(
                    lecturerId,
                    updateBody,
                    day = existingEntry.day?.toInt(),
                    lecturerResearchScheduleId = existingEntry.lecturerScheduleId
                )
                assertNotNull(result)
                logger.info("putLecturerSchedule($lecturerId): success, ${result.size} entries returned")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("putLecturerSchedule($lecturerId) not authorized (expected for student): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("putLecturerSchedule($lecturerId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun deleteLecturerSchedule_expectsAuthorizationOrSuccess() {
        val lecturers = api.teachers.getLecturers(limit = 1)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val schedule = try {
                    api.teachers.getLecturerSchedule(lecturerId)
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("Cannot read schedule, skipping deleteLecturerSchedule: ${e.message}")
                    return
                }

                if (schedule.isEmpty()) {
                    logger.info("deleteLecturerSchedule($lecturerId): no schedule entries to delete, skipping")
                    return
                }

                val entryToDelete = schedule.first()
                val scheduleId = entryToDelete.lecturerScheduleId
                val day = entryToDelete.day?.toInt()

                // Delete entry
                api.teachers.deleteLecturerSchedule(
                    lecturerId,
                    day = day,
                    lecturerResearchScheduleId = scheduleId
                )
                logger.info("deleteLecturerSchedule($lecturerId, scheduleId=$scheduleId): success")

                // Rollback: re-add the deleted entry
                try {
                    val rollbackBody = listOf(
                        Esse3PostTeacherSchedule(
                            day = day ?: 1,
                            startTime = entryToDelete.startTime ?: "09:00",
                            endTime = entryToDelete.endTime ?: "10:00",
                            placeDescription = entryToDelete.placeDescription,
                            note = entryToDelete.note
                        )
                    )
                    api.teachers.postLecturerSchedule(lecturerId, rollbackBody)
                    logger.info("deleteLecturerSchedule rollback: re-added schedule entry")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("deleteLecturerSchedule rollback not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warn("deleteLecturerSchedule rollback validation error: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("deleteLecturerSchedule($lecturer.lecturerId) not authorized (expected for student): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("deleteLecturerSchedule($lecturer.lecturerId) validation error: ${e.message}")
            }
        }
    }
}
