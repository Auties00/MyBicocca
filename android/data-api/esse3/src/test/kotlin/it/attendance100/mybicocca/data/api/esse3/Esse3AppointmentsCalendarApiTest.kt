package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3Appointment
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Esse3AppointmentsCalendarApiTest : Esse3ApiTestBase() {

    private val logger: Logger = LoggerFactory.getLogger(Esse3AppointmentsCalendarApiTest::class.java)

    companion object {
        // Common calendar context codes used by ESSE3 for appointment calendars.
        // "PRENOTAZIONE_APPELLI" is a typical context; actual codes depend on university config.
        private val CALENDAR_CONTEXT_CANDIDATES = listOf(
            "PRENOTAZIONE_APPELLI",
            "ORIENTAMENTO",
            "TUTORATO",
            "COLLOQUIO"
        )
    }

    @Test
    suspend fun `getCalendarList - try known calendar contexts`() {
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendars = api.appointmentsCalendar.getCalendarList(calendarContext = context)
                logger.info("getCalendarList(context={}): {} entries", context, calendars.size)
                for (calendar in calendars) {
                    logger.info("  calendar: typeCode={}, typeDescription={}, cancellationModeId={}",
                        calendar.calendarCallTypeCode,
                        calendar.calendarCallTypeDescription,
                        calendar.enrollmentCancellationCalendarModeId)
                }
                if (calendars.isNotEmpty()) {
                    // If we found calendars, assert minimal integrity
                    val first = calendars.first()
                    assertNotNull(first.calendarCallTypeCode, "calendarCallTypeCode should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getCalendarList(context={}): not authorized - {}", context, e.message)
            } catch (e: Esse3ValidationException) {
                logger.warn("getCalendarList(context={}): validation error - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getCalendarList(context={}): error - {} {}", context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getCalendarList - with language parameter`() {
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendars = api.appointmentsCalendar.getCalendarList(
                    calendarContext = context,
                    language = "it"
                )
                logger.info("getCalendarList(context={}, language=it): {} entries", context, calendars.size)
                for (calendar in calendars) {
                    logger.info("  calendar: typeCode={}, typeDescription={}",
                        calendar.calendarCallTypeCode, calendar.calendarCallTypeDescription)
                }
                if (calendars.isNotEmpty()) break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getCalendarList (language=it, context={}): not authorized - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getCalendarList (language=it, context={}): error - {} {}",
                    context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getCalendarList - with pagination`() {
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendarsPage1 = api.appointmentsCalendar.getCalendarList(
                    calendarContext = context,
                    start = 0,
                    limit = 5
                )
                logger.info("getCalendarList (page1, limit=5, context={}): {} entries", context, calendarsPage1.size)
                assertTrue(calendarsPage1.size <= 5, "Page size should be <= 5")

                if (calendarsPage1.size == 5) {
                    val calendarsPage2 = api.appointmentsCalendar.getCalendarList(
                        calendarContext = context,
                        start = 5,
                        limit = 5
                    )
                    logger.info("getCalendarList (page2, limit=5, context={}): {} entries", context, calendarsPage2.size)
                    assertTrue(calendarsPage2.size <= 5, "Page size should be <= 5")
                }
                if (calendarsPage1.isNotEmpty()) break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getCalendarList (pagination, context={}): not authorized - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getCalendarList (pagination, context={}): error - {} {}",
                    context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getShiftsList - retrieve shifts for available calendar type codes`() {
        // First discover available calendar type codes via getCalendarList
        val discoveredTypeCodes = mutableListOf<String>()
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendars = api.appointmentsCalendar.getCalendarList(calendarContext = context)
                for (calendar in calendars) {
                    val typeCode = calendar.calendarCallTypeCode
                    if (typeCode != null && typeCode !in discoveredTypeCodes) {
                        discoveredTypeCodes.add(typeCode)
                    }
                }
                if (discoveredTypeCodes.isNotEmpty()) break
            } catch (_: Esse3NotAuthorizedException) {
                logger.warn("getCalendarList (for getShiftsList setup, context={}): not authorized", context)
            } catch (e: Exception) {
                logger.warn("getCalendarList (for getShiftsList setup, context={}): error - {}", context, e.message)
            }
        }

        if (discoveredTypeCodes.isEmpty()) {
            logger.warn("getShiftsList: no calendar type codes discovered, skipping shifts test")
            return
        }

        logger.info("getShiftsList: discovered {} calendar type codes: {}", discoveredTypeCodes.size, discoveredTypeCodes)

        for (typeCode in discoveredTypeCodes) {
            try {
                val shifts = api.appointmentsCalendar.getShiftsList(calendarTypeCode = typeCode)
                logger.info("getShiftsList(typeCode={}): {} shifts", typeCode, shifts.size)
                for (shift in shifts) {
                    logger.info("  shift: calendarCallId={}, typeCode={}, code={}, description={}",
                        shift.calendarCallId, shift.calendarCallTypeCode,
                        shift.calendarCallCode, shift.calendarCallDescription)
                    logger.info("    startDate={}, endDate={}, bookingStart={}, bookingEnd={}",
                        shift.shiftStartDate, shift.shiftEndDate,
                        shift.bookingStartDate, shift.bookingEndDate)
                    logger.info("    siteDescription={}, day={}, calendarShiftId={}",
                        shift.siteDescription, shift.day, shift.calendarShiftId)
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getShiftsList(typeCode={}): not authorized - {}", typeCode, e.message)
            } catch (e: Esse3ValidationException) {
                logger.warn("getShiftsList(typeCode={}): validation error - {}", typeCode, e.message)
            } catch (e: Exception) {
                logger.warn("getShiftsList(typeCode={}): error - {} {}", typeCode, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getShiftsList - with language and pagination`() {
        val discoveredTypeCodes = mutableListOf<String>()
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendars = api.appointmentsCalendar.getCalendarList(calendarContext = context)
                for (calendar in calendars) {
                    val typeCode = calendar.calendarCallTypeCode
                    if (typeCode != null && typeCode !in discoveredTypeCodes) {
                        discoveredTypeCodes.add(typeCode)
                    }
                }
                if (discoveredTypeCodes.isNotEmpty()) break
            } catch (_: Exception) {
                // ignore, handled below
            }
        }

        if (discoveredTypeCodes.isEmpty()) {
            logger.warn("getShiftsList (language+pagination): no type codes discovered, skipping")
            return
        }

        val typeCode = discoveredTypeCodes.first()
        try {
            val shifts = api.appointmentsCalendar.getShiftsList(
                calendarTypeCode = typeCode,
                language = "it",
                start = 0,
                limit = 10
            )
            logger.info("getShiftsList (language=it, limit=10, typeCode={}): {} shifts", typeCode, shifts.size)
            assertTrue(shifts.size <= 10, "Page size should be <= 10")
            for (shift in shifts) {
                logger.info("  shift: id={}, description={}, day={}", shift.calendarCallId, shift.calendarCallDescription, shift.day)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getShiftsList (language+pagination): not authorized - {}", e.message)
        } catch (e: Exception) {
            logger.warn("getShiftsList (language+pagination): error - {} {}", e.javaClass.simpleName, e.message)
        }
    }

    @Test
    suspend fun `getBookingsList - retrieve bookings for current student`() {
        val personId = studentProfile.personId
        val studentId = studentProfile.studentId
        val enrollmentId = studentProfile.enrollmentId
        val degreeCourseId = studentProfile.degreeCourseId

        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val bookings = api.appointmentsCalendar.getBookingsList(
                    calendarContext = context,
                    personId = personId,
                    studentId = studentId,
                    enrollmentId = enrollmentId,
                    courseOfStudyId = degreeCourseId
                )
                logger.info("getBookingsList(context={}, personId={}): {} bookings", context, personId, bookings.size)
                for (booking in bookings) {
                    logger.info("  booking: typeCode={}, typeDescription={}, day={}, bookingDate={}",
                        booking.calendarCallTypeCode, booking.calendarCallTypeDescription,
                        booking.day, booking.bookingDate)
                    logger.info("    site={}, administrativeStructure={}, modificationDays={}",
                        booking.site, booking.administrativeStructureDescription,
                        booking.modificationDaysNumber)
                    logger.info("    shiftViewStart={}, shiftViewEnd={}",
                        booking.shiftViewStartDate, booking.shiftViewEndDate)
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getBookingsList(context={}): not authorized - {}", context, e.message)
            } catch (e: Esse3ValidationException) {
                logger.warn("getBookingsList(context={}): validation error - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getBookingsList(context={}): error - {} {}", context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getBookingsList - with minimal params`() {
        val personId = studentProfile.personId
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val bookings = api.appointmentsCalendar.getBookingsList(
                    calendarContext = context,
                    personId = personId
                )
                logger.info("getBookingsList (minimal, context={}, personId={}): {} bookings", context, personId, bookings.size)
                for (booking in bookings) {
                    logger.info("  booking: typeCode={}, day={}", booking.calendarCallTypeCode, booking.day)
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getBookingsList (minimal, context={}): not authorized - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getBookingsList (minimal, context={}): error - {} {}", context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `getBookingsList - with pagination`() {
        val personId = studentProfile.personId
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val bookings = api.appointmentsCalendar.getBookingsList(
                    calendarContext = context,
                    personId = personId,
                    start = 0,
                    limit = 5
                )
                logger.info("getBookingsList (pagination, context={}, limit=5): {} bookings", context, bookings.size)
                assertTrue(bookings.size <= 5, "Page size should be <= 5")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getBookingsList (pagination, context={}): not authorized - {}", context, e.message)
            } catch (e: Exception) {
                logger.warn("getBookingsList (pagination, context={}): error - {} {}", context, e.javaClass.simpleName, e.message)
            }
        }
    }

    @Test
    suspend fun `insertAppointment updateAppointment cancelAppointment - create update cancel triplet with rollback`() {
        // Discover a valid shift ID to use for booking
        val discoveredShiftId = findFirstAvailableShiftId()

        if (discoveredShiftId == null) {
            logger.warn("insertAppointment/updateAppointment/cancelAppointment: no shift found, skipping triplet test")
            return
        }

        val personId = studentProfile.personId
        val studentId = studentProfile.studentId
        logger.info("Attempting appointment triplet: personId={}, shiftId={}", personId, discoveredShiftId)

        var insertedCallId: Long? = null
        try {
            // Insert appointment
            val appointmentToInsert = Esse3Appointment(
                shiftId = discoveredShiftId.toInt(),
                studentId = studentId.toInt(),
                note = "Test appointment - integration test"
            )
            try {
                api.appointmentsCalendar.insertAppointment(personId = personId, body = appointmentToInsert)
                logger.info("insertAppointment: success")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("insertAppointment: not authorized (expected for student) - {}", e.message)
                return
            } catch (e: Esse3ValidationException) {
                logger.warn("insertAppointment: validation error (shift may be unavailable) - {}", e.message)
                return
            }

            // Retrieve existing bookings to find the newly inserted appointment's callId
            // (insertAppointment returns Unit, so we need to find it via bookings)
            var foundCallId: Long? = null
            for (context in CALENDAR_CONTEXT_CANDIDATES) {
                try {
                    val bookings = api.appointmentsCalendar.getBookingsList(
                        calendarContext = context,
                        personId = personId
                    )
                    val booking = bookings.firstOrNull { it.calendarCallEnrolledId != null }
                    if (booking != null) {
                        foundCallId = booking.calendarCallEnrolledId
                        logger.info("insertAppointment: found booking callId={}", foundCallId)
                        break
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }

            insertedCallId = foundCallId

            if (insertedCallId != null) {
                // Update appointment
                val appointmentToUpdate = Esse3Appointment(
                    shiftId = discoveredShiftId.toInt(),
                    studentId = studentId.toInt(),
                    note = "Updated test appointment - integration test"
                )
                try {
                    val updateResult = api.appointmentsCalendar.updateAppointment(
                        callId = insertedCallId,
                        personId = personId,
                        body = appointmentToUpdate
                    )
                    logger.info("updateAppointment: success, returnedShiftId={}", updateResult.shiftId)
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("updateAppointment: not authorized - {}", e.message)
                } catch (e: Esse3ValidationException) {
                    logger.warn("updateAppointment: validation error - {}", e.message)
                }
            } else {
                logger.warn("insertAppointment: could not find callId in bookings list after insertion, skipping update")
            }
        } finally {
            // Rollback: cancel the appointment if we found a callId
            if (insertedCallId != null) {
                try {
                    api.appointmentsCalendar.cancelAppointment(callId = insertedCallId, personId = personId)
                    logger.info("cancelAppointment (rollback): success for callId={}", insertedCallId)
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("cancelAppointment (rollback): not authorized - {}", e.message)
                } catch (e: Esse3ValidationException) {
                    logger.warn("cancelAppointment (rollback): validation error - {}", e.message)
                } catch (e: Exception) {
                    logger.warn("cancelAppointment (rollback): error - {} {}", e.javaClass.simpleName, e.message)
                }
            }
        }
    }

    /**
     * Tries to find the first available shift ID across known calendar contexts.
     * Returns the shift ID (as Long) or null if none found.
     */
    private suspend fun findFirstAvailableShiftId(): Long? {
        for (context in CALENDAR_CONTEXT_CANDIDATES) {
            try {
                val calendars = api.appointmentsCalendar.getCalendarList(calendarContext = context)
                for (calendar in calendars) {
                    val typeCode = calendar.calendarCallTypeCode ?: continue
                    try {
                        val shifts = api.appointmentsCalendar.getShiftsList(calendarTypeCode = typeCode)
                        val shift = shifts.firstOrNull { it.calendarShiftId != null }
                        if (shift?.calendarShiftId != null) {
                            logger.info("findFirstAvailableShiftId: found shiftId={} (context={}, typeCode={})",
                                shift.calendarShiftId, context, typeCode)
                            return shift.calendarShiftId
                        }
                    } catch (_: Exception) {
                        // try next
                    }
                }
            } catch (_: Exception) {
                // try next context
            }
        }
        return null
    }
}
