package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

class Esse3CalesaApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetSessions() {
        try {
            val sessions = api.calesa.getSessions()
            assertNotNull(sessions)
            logger.info("getSessions: found ${sessions.size} sessions")
            for (session in sessions) {
                logger.info(
                    "  session: aaSesId=${session.academicYearSessionId}, " +
                        "sesId=${session.sessionId}, des=${session.description}, " +
                        "cdsId=${session.courseOfStudyId}, cdsCod=${session.courseOfStudyCode}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getSessions not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetSessionsFilteredByCourseOfStudy() {
        try {
            val sessions = api.calesa.getSessions(courseOfStudyId = studentProfile.degreeCourseId)
            assertNotNull(sessions)
            logger.info("getSessions(cdsId=${studentProfile.degreeCourseId}): found ${sessions.size} sessions")
            for (session in sessions) {
                logger.info(
                    "  session: aaSesId=${session.academicYearSessionId}, " +
                        "ses=${session.sessionCode}, des=${session.description}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getSessions filtered not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetSessionsByAcademicYearSession() {
        try {
            val allSessions = api.calesa.getSessions(courseOfStudyId = studentProfile.degreeCourseId)
            val firstSession = allSessions.firstOrNull()
            if (firstSession == null) {
                logger.warning("testGetSessionsByAcademicYearSession: no sessions available")
                return
            }
            val aaSesId = firstSession.academicYearSessionId?.toLong()
            if (aaSesId == null) {
                logger.warning("testGetSessionsByAcademicYearSession: first session has no academicYearSessionId")
                return
            }
            val result = api.calesa.getSessionsByAcademicYearSession(academicYearSessionId = aaSesId)
            assertNotNull(result)
            logger.info("getSessionsByAcademicYearSession(aaSesId=$aaSesId): found ${result.size} entries")
            for (s in result) {
                logger.info(
                    "  entry: cdsId=${s.courseOfStudyId}, cdsCod=${s.courseOfStudyCode}, " +
                        "des=${s.description}, startDate=${s.startDate}, endDate=${s.endDate}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getSessionsByAcademicYearSession not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetSessionsByAcademicYearSessionAndCourseOfStudy() {
        try {
            val allSessions = api.calesa.getSessions(courseOfStudyId = studentProfile.degreeCourseId)
            val firstSession = allSessions.firstOrNull()
            if (firstSession == null) {
                logger.warning("testGetSessionsByAcademicYearSessionAndCourseOfStudy: no sessions available")
                return
            }
            val aaSesId = firstSession.academicYearSessionId?.toLong()
            if (aaSesId == null) {
                logger.warning("testGetSessionsByAcademicYearSessionAndCourseOfStudy: first session has no aaSesId")
                return
            }
            val result = api.calesa.getSessionsByAcademicYearSessionAndCourseOfStudy(
                academicYearSessionId = aaSesId,
                courseOfStudyId = studentProfile.degreeCourseId
            )
            assertNotNull(result)
            logger.info(
                "getSessionsByAcademicYearSessionAndCourseOfStudy(aaSesId=$aaSesId, " +
                    "cdsId=${studentProfile.degreeCourseId}): found ${result.size} entries"
            )
            for (s in result) {
                logger.info(
                    "  entry: ses=${s.sessionCode}, des=${s.description}, " +
                        "startDate=${s.startDate}, endDate=${s.endDate}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getSessionsByAcademicYearSessionAndCourseOfStudy not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetActivitiesForExamCalls() {
        try {
            val activities = api.calesa.getActivitiesForExamCalls(
                matId = studentProfile.enrollmentId
            )
            assertNotNull(activities)
            logger.info("getActivitiesForExamCalls(matId=${studentProfile.enrollmentId}): found ${activities.size} activities")
            for (activity in activities) {
                logger.info(
                    "  activity: cdsDefAppId=${activity.courseOfStudyDefaultCallId}, " +
                        "adDefAppId=${activity.activityExamDefinitionId}, " +
                        "adCod=${activity.activityCode}, adDes=${activity.activityDescription}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getActivitiesForExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExamCallsAndCallDetails() {
        try {
            val activities = api.calesa.getActivitiesForExamCalls(
                matId = studentProfile.enrollmentId
            )
            if (activities.isEmpty()) {
                logger.warning("testGetExamCallsAndCallDetails: no activities available for exam calls")
                return
            }

            for (activity in activities.take(3)) {
                val cdsId = activity.courseOfStudyDefaultCallId
                val adId = activity.activityExamDefinitionId.toLong()

                logger.info("Testing exam calls for cdsId=$cdsId, adId=$adId (${activity.activityDescription})")
                try {
                    val calls = api.calesa.getExamCalls(
                        courseOfStudyId = cdsId,
                        activityId = adId
                    )
                    assertNotNull(calls)
                    logger.info("  getExamCalls: found ${calls.size} calls")

                    for (call in calls.take(3)) {
                        val callId = call.examCallId ?: continue
                        logger.info(
                            "    call: callId=$callId, state=${call.state}, " +
                                "startDate=${call.callStartDate}, enrolled=${call.enrolledNumber}"
                        )

                        try {
                            val callDetail = api.calesa.getExamCall(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(callDetail)
                            logger.info(
                                "      getExamCall detail: callId=${callDetail.examCallId}, " +
                                    "state=${callDetail.state}"
                            )
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getExamCall not authorized for callId=$callId: ${e.message}")
                        } catch (e: Esse3ValidationException) {
                            logger.warning("      getExamCall validation error for callId=$callId: ${e.message}")
                        }

                        // Test exam call types
                        try {
                            val examTypes = api.calesa.getExamCallTypes(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(examTypes)
                            logger.info("      getExamCallTypes: found ${examTypes.size} types")
                            for (examType in examTypes) {
                                logger.info(
                                    "        type: code=${examType.examTypeCode}, " +
                                        "des=${examType.examTypeDescription}"
                                )
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getExamCallTypes not authorized: ${e.message}")
                        }

                        // Test sessions for call
                        try {
                            val callSessions = api.calesa.getExamCallSessions(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(callSessions)
                            logger.info("      getExamCallSessions: found ${callSessions.size} sessions")
                            for (callSession in callSessions) {
                                logger.info(
                                    "        callSession: aaSesId=${callSession.academicYearSessionId}, " +
                                        "sesId=${callSession.sessionId}"
                                )
                                try {
                                    val aaSesId = callSession.academicYearSessionId ?: continue
                                    val sesId = callSession.sessionId ?: continue
                                    val singleSession = api.calesa.getExamCallSession(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        academicYearSessionId = aaSesId.toLong(),
                                        sessionId = sesId.toLong()
                                    )
                                    assertNotNull(singleSession)
                                    logger.info(
                                        "          getExamCallSession: aaSesId=${singleSession.academicYearSessionId}, " +
                                            "ses=${singleSession.sessionDescription}"
                                    )
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getExamCallSession not authorized: ${e.message}")
                                } catch (e: Esse3ValidationException) {
                                    logger.warning("          getExamCallSession validation error: ${e.message}")
                                }
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getExamCallSessions not authorized: ${e.message}")
                        }

                        // Test lecturers for exam call (requires TEACHER permission)
                        try {
                            val lecturers = api.calesa.getLecturersExamCall(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(lecturers)
                            logger.info("      getLecturersExamCall: found ${lecturers.size} lecturers")
                            for (lecturer in lecturers) {
                                logger.info(
                                    "        lecturer: id=${lecturer.lecturerId}, " +
                                        "name=${lecturer.lecturerName} ${lecturer.lecturerSurname}, " +
                                        "role=${lecturer.roleCode}"
                                )
                                // Test individual lecturer
                                val lecturerId = lecturer.lecturerId ?: continue
                                try {
                                    val singleLecturer = api.calesa.getLecturerExamCall(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        lecturerId = lecturerId
                                    )
                                    assertNotNull(singleLecturer)
                                    logger.info(
                                        "          getLecturerExamCall: id=${singleLecturer.lecturerId}, " +
                                            "name=${singleLecturer.lecturerName}"
                                    )
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getLecturerExamCall not authorized: ${e.message}")
                                }
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getLecturersExamCall not authorized: ${e.message}")
                        }

                        // Test common exams (esacom) for call
                        try {
                            val commonExams = api.calesa.getCommonExamsExamCall(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(commonExams)
                            logger.info("      getCommonExamsExamCall: found ${commonExams.size} common exams")
                            for (commonExam in commonExams) {
                                logger.info(
                                    "        commonExam: cdsId=${commonExam.courseOfStudyId}, " +
                                        "adId=${commonExam.activityId}"
                                )
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getCommonExamsExamCall not authorized: ${e.message}")
                        }

                        // Test shifts for call (requires TEACHER permission)
                        try {
                            val shifts = api.calesa.getExamCallShifts(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(shifts)
                            logger.info("      getExamCallShifts: found ${shifts.size} shifts")
                            for (shift in shifts.take(3)) {
                                logger.info(
                                    "        shift: appLogId=${shift.callLogId}, " +
                                        "des=${shift.description}"
                                )
                                val shiftCallLogId = shift.callLogId?.toLong() ?: continue
                                try {
                                    val singleShift = api.calesa.getExamCallShift(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        callLogId = shiftCallLogId
                                    )
                                    assertNotNull(singleShift)
                                    logger.info(
                                        "          getExamCallShift: appLogId=${singleShift.callLogId}, " +
                                            "des=${singleShift.description}"
                                    )
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getExamCallShift not authorized: ${e.message}")
                                }

                                // Test shift lecturers
                                try {
                                    val shiftLecturers = api.calesa.getLecturersShift(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        callLogId = shiftCallLogId
                                    )
                                    assertNotNull(shiftLecturers)
                                    logger.info("          getLecturersShift: found ${shiftLecturers.size} lecturers")
                                    for (shiftLecturer in shiftLecturers) {
                                        logger.info(
                                            "            shiftLecturer: docenteId=${shiftLecturer.lecturerId}, " +
                                                "name=${shiftLecturer.lecturerName} ${shiftLecturer.lecturerSurname}"
                                        )
                                        val shiftLecturerId = shiftLecturer.lecturerId ?: continue
                                        try {
                                            val singleShiftLecturer = api.calesa.getLecturerShift(
                                                courseOfStudyId = cdsId,
                                                activityId = adId,
                                                callId = callId,
                                                callLogId = shiftCallLogId,
                                                lecturerId = shiftLecturerId
                                            )
                                            assertNotNull(singleShiftLecturer)
                                            logger.info(
                                                "              getLecturerShift: docenteId=${singleShiftLecturer.lecturerId}"
                                            )
                                        } catch (e: Esse3NotAuthorizedException) {
                                            logger.warning("              getLecturerShift not authorized: ${e.message}")
                                        }
                                    }
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getLecturersShift not authorized: ${e.message}")
                                }
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getExamCallShifts not authorized: ${e.message}")
                        }

                        // Test enrolled list (requires TEACHER permission)
                        try {
                            val enrolledList = api.calesa.getExamCallEnrolledList(
                                courseOfStudyId = cdsId,
                                activityId = adId,
                                callId = callId
                            )
                            assertNotNull(enrolledList)
                            logger.info("      getExamCallEnrolledList: found ${enrolledList.size} enrollments")
                            for (enrolled in enrolledList.take(3)) {
                                logger.info(
                                    "        enrolled: stuId=${enrolled.studentId}, " +
                                        "applistaId=${enrolled.applicationListId}"
                                )
                                val stuId = enrolled.studentId?.toLong() ?: continue
                                try {
                                    val singleEnrolled = api.calesa.getEnrolledExamCall(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        studentId = stuId
                                    )
                                    assertNotNull(singleEnrolled)
                                    logger.info(
                                        "          getEnrolledExamCall: stuId=${singleEnrolled.studentId}, " +
                                            "applistaId=${singleEnrolled.applicationListId}"
                                    )
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getEnrolledExamCall not authorized: ${e.message}")
                                } catch (e: Esse3ValidationException) {
                                    logger.warning("          getEnrolledExamCall validation error: ${e.message}")
                                }

                                // Test tags for booking
                                val adsceId = enrolled.activityChoiceId?.toLong() ?: continue
                                try {
                                    val tags = api.calesa.getTagsByBooking(
                                        courseOfStudyId = cdsId,
                                        activityId = adId,
                                        callId = callId,
                                        activityChoiceId = adsceId
                                    )
                                    assertNotNull(tags)
                                    logger.info(
                                        "          getTagsByBooking(adsceId=$adsceId): found ${tags.size} tags"
                                    )
                                    for (tag in tags) {
                                        logger.info("            tag: code=${tag.tagCode}, des=${tag.tagDescription}")
                                    }
                                } catch (e: Esse3NotAuthorizedException) {
                                    logger.warning("          getTagsByBooking not authorized: ${e.message}")
                                } catch (e: Esse3ValidationException) {
                                    logger.warning("          getTagsByBooking validation error: ${e.message}")
                                }
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getExamCallEnrolledList not authorized: ${e.message}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getExamCalls not authorized for cdsId=$cdsId, adId=$adId: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getExamCalls validation error for cdsId=$cdsId, adId=$adId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetExamCallsAndCallDetails not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetBookingsByMatId() {
        try {
            val bookings = api.calesa.getBookingsByMatId(matId = studentProfile.enrollmentId)
            assertNotNull(bookings)
            logger.info("getBookingsByMatId(matId=${studentProfile.enrollmentId}): found ${bookings.size} bookings")
            for (booking in bookings) {
                logger.info(
                    "  booking: applistaId=${booking.applicationListId}, " +
                        "cdsId=${booking.courseOfStudyId}, adId=${booking.activityId}, " +
                        "appId=${booking.callId}"
                )
                assertTrue(booking.applicationListId != null || booking.callId != null, "Booking should have some identifier")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getBookingsByMatId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetBooking() {
        try {
            val bookings = api.calesa.getBookingsByMatId(matId = studentProfile.enrollmentId)
            if (bookings.isEmpty()) {
                logger.warning("testGetBooking: no bookings available")
                return
            }

            for (booking in bookings.take(3)) {
                val applistaId = booking.applicationListId ?: continue
                try {
                    val singleBooking = api.calesa.getBooking(
                        matId = studentProfile.enrollmentId,
                        applicationListId = applistaId
                    )
                    assertNotNull(singleBooking)
                    logger.info(
                        "getBooking(matId=${studentProfile.enrollmentId}, applistaId=$applistaId): " +
                            "applistaId=${singleBooking.applicationListId}, " +
                            "cdsId=${singleBooking.courseOfStudyId}, adId=${singleBooking.activityId}"
                    )
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getBooking not authorized for applistaId=$applistaId: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getBooking validation error for applistaId=$applistaId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetBooking: getBookingsByMatId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPresenceCertificateAndBookingStatino() {
        try {
            val bookings = api.calesa.getBookingsByMatId(matId = studentProfile.enrollmentId)
            if (bookings.isEmpty()) {
                logger.warning("testGetPresenceCertificateAndBookingStatino: no bookings available")
                return
            }

            for (booking in bookings.take(3)) {
                val cdsId = booking.courseOfStudyId ?: continue
                val adId = booking.activityId?.toLong() ?: continue
                val callId = booking.callId?.toLong() ?: continue
                val stuId = booking.studentId?.toLong() ?: continue

                try {
                    val certificate = api.calesa.getPresenceCertificate(
                        courseOfStudyId = cdsId,
                        activityId = adId,
                        callId = callId,
                        studentId = stuId
                    )
                    assertNotNull(certificate)
                    logger.info(
                        "getPresenceCertificate(cdsId=$cdsId, adId=$adId, callId=$callId, stuId=$stuId): " +
                            "result length=${certificate.length}"
                    )
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPresenceCertificate not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPresenceCertificate validation error: ${e.message}")
                }

                try {
                    val statino = api.calesa.getBookingStatino(
                        courseOfStudyId = cdsId,
                        activityId = adId,
                        callId = callId,
                        studentId = stuId
                    )
                    assertNotNull(statino)
                    logger.info(
                        "getBookingStatino(cdsId=$cdsId, adId=$adId, callId=$callId, stuId=$stuId): " +
                            "result length=${statino.length}"
                    )
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getBookingStatino not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getBookingStatino validation error: ${e.message}")
                }

                break
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetPresenceCertificateAndBookingStatino: getBookingsByMatId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLecturerAuthorizations() {
        try {
            val authorizations = api.calesa.getLecturerAuthorizations(
                courseOfStudyEnableId = studentProfile.degreeCourseId
            )
            assertNotNull(authorizations)
            logger.info(
                "getLecturerAuthorizations(cdsId=${studentProfile.degreeCourseId}): found ${authorizations.size} authorizations"
            )
            for (auth in authorizations.take(5)) {
                logger.info(
                    "  auth: aaAbilDocId=${auth.academicYearTeacherAuthorizationId}, " +
                        "cdsId=${auth.courseOfStudyId}, adId=${auth.activityId}, " +
                        "docenteId=${auth.lecturerId}, " +
                        "name=${auth.lecturerName} ${auth.lecturerSurname}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLecturerAuthorizations not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLecturerAuthorizationsAndByLecturer() {
        try {
            val authorizations = api.calesa.getLecturerAuthorizations(
                courseOfStudyEnableId = studentProfile.degreeCourseId,
                limit = 5
            )
            assertNotNull(authorizations)
            logger.info("getLecturerAuthorizations: found ${authorizations.size} authorizations")

            val firstAuth = authorizations.firstOrNull() ?: run {
                logger.warning("testGetLecturerAuthorizationsAndByLecturer: no authorizations available")
                return
            }
            val lecturerId = firstAuth.lecturerId

            // Test by lecturer ID
            val byLecturer = api.calesa.getLecturerAuthorizationsByLecturer(lecturerId = lecturerId)
            assertNotNull(byLecturer)
            logger.info("getLecturerAuthorizationsByLecturer(lecturerId=$lecturerId): found ${byLecturer.size} authorizations")
            for (auth in byLecturer.take(3)) {
                logger.info(
                    "  auth: aaAbilDocId=${auth.academicYearTeacherAuthorizationId}, " +
                        "cdsId=${auth.courseOfStudyId}, adId=${auth.activityId}"
                )
            }

            // Test by course teaching activity
            val cdsId = firstAuth.courseOfStudyId
            val adId = firstAuth.activityId.toLong()
            val byActivity = api.calesa.getLecturerAuthorizationsCourseTeachingActivity(
                courseOfStudyId = cdsId,
                activityId = adId,
                lecturerId = lecturerId
            )
            assertNotNull(byActivity)
            logger.info(
                "getLecturerAuthorizationsCourseTeachingActivity(cdsId=$cdsId, adId=$adId, lecturerId=$lecturerId): " +
                    "found ${byActivity.size} authorizations"
            )

            // Test single authorization
            val authId = firstAuth.academicYearTeacherAuthorizationId.toLong()
            try {
                val singleAuth = api.calesa.getLecturerAuthorization(
                    courseOfStudyId = cdsId,
                    activityId = adId,
                    lecturerId = lecturerId,
                    academicYearOfferTeacherAuthorizationId = authId
                )
                assertNotNull(singleAuth)
                logger.info(
                    "getLecturerAuthorization: aaAbilDocId=${singleAuth.academicYearTeacherAuthorizationId}, " +
                        "docenteId=${singleAuth.lecturerId}"
                )
            } catch (e: Esse3ValidationException) {
                logger.warning("getLecturerAuthorization validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetLecturerAuthorizationsAndByLecturer not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExamCallsByLecturerAuthorization() {
        try {
            val authorizations = api.calesa.getLecturerAuthorizations(
                courseOfStudyEnableId = studentProfile.degreeCourseId,
                limit = 3
            )
            assertNotNull(authorizations)
            if (authorizations.isEmpty()) {
                logger.warning("testGetExamCallsByLecturerAuthorization: no authorizations available")
                return
            }

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val now = LocalDate.now()
            val minDate = now.minusYears(1).format(dateFormatter)
            val maxDate = now.plusMonths(6).format(dateFormatter)

            for (auth in authorizations.take(2)) {
                val lecturerId = auth.lecturerId
                try {
                    val examCalls = api.calesa.getExamCallsByLecturerAuthorization(
                        lecturerId = lecturerId,
                        minCallDate = minDate,
                        maxCallDate = maxDate
                    )
                    assertNotNull(examCalls)
                    logger.info(
                        "getExamCallsByLecturerAuthorization(lecturerId=$lecturerId, min=$minDate, max=$maxDate): " +
                            "found ${examCalls.size} calls"
                    )
                    for (call in examCalls.take(3)) {
                        logger.info(
                            "  call: cdsId=${call.courseOfStudyId}, adId=${call.activityId}, " +
                                "appId=${call.callId}, startDate=${call.callStartDate}"
                        )
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getExamCallsByLecturerAuthorization not authorized for lecturerId=$lecturerId: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getExamCallsByLecturerAuthorization validation error for lecturerId=$lecturerId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetExamCallsByLecturerAuthorization not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetEsacom() {
        try {
            val currentYear = LocalDate.now().year
            val academicYearId = if (LocalDate.now().monthValue >= 9) currentYear else currentYear - 1

            val esacomList = api.calesa.getEsacomByAcademicYearId(academicYearId = academicYearId)
            assertNotNull(esacomList)
            logger.info("getEsacomByAcademicYearId(aaId=$academicYearId): found ${esacomList.size} esacom entries")

            for (esacom in esacomList.take(3)) {
                logger.info(
                    "  esacom: cdsEsaId=${esacom.courseOfStudyGraduationId}, adEsaId=${esacom.activityExamId}, " +
                        "aaId=${esacom.academicYearId}, cdsEsaCod=${esacom.courseOfStudyGraduationCode}"
                )
                val cdsEsaId = esacom.courseOfStudyGraduationId ?: continue
                val adEsaId = esacom.activityExamId?.toLong() ?: continue

                try {
                    val parentList = api.calesa.getEsacomParent(
                        academicYearId = academicYearId,
                        courseOfStudyGraduationId = cdsEsaId,
                        activityExamId = adEsaId
                    )
                    assertNotNull(parentList)
                    logger.info(
                        "  getEsacomParent(aaId=$academicYearId, cdsEsaId=$cdsEsaId, adEsaId=$adEsaId): found ${parentList.size} entries"
                    )

                    for (parent in parentList.take(2)) {
                        val childCdsId = parent.childCourseOfStudyId ?: continue
                        val childAdId = parent.childActivityId ?: continue
                        logger.info(
                            "    parent: childCdsId=$childCdsId, childAdId=$childAdId, " +
                                "cdsFiglioCod=${parent.childCourseOfStudyCode}"
                        )
                        try {
                            val child = api.calesa.getEsacomChild(
                                academicYearId = academicYearId,
                                courseOfStudyGraduationId = cdsEsaId,
                                activityExamId = adEsaId,
                                childCourseOfStudyId = childCdsId,
                                childActivityId = childAdId
                            )
                            assertNotNull(child)
                            logger.info(
                                "      getEsacomChild: childCdsId=${child.childCourseOfStudyId}, " +
                                    "childAdId=${child.childActivityId}"
                            )
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("      getEsacomChild not authorized: ${e.message}")
                        } catch (e: Esse3ValidationException) {
                            logger.warning("      getEsacomChild validation error: ${e.message}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getEsacomParent not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getEsacomParent validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("testGetEsacom not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitmentsByDate() {
        try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val today = LocalDate.now().format(dateFormatter)

            val commitments = api.calesa.getCommitmentsByDate(referenceDate = today)
            assertNotNull(commitments)
            logger.info("getCommitmentsByDate(referenceDate=$today): found ${commitments.size} commitments")
            for (commitment in commitments.take(5)) {
                logger.info(
                    "  commitment: code=${commitment.commitmentCode}, cdsId=${commitment.courseOfStudyId}, " +
                        "adId=${commitment.activityId}, callId=${commitment.callId}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitmentsByDate not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitmentsByDateWithoutFilter() {
        try {
            val commitments = api.calesa.getCommitmentsByDate()
            assertNotNull(commitments)
            logger.info("getCommitmentsByDate(no filter): found ${commitments.size} commitments")
            for (commitment in commitments.take(3)) {
                logger.info(
                    "  commitment: code=${commitment.commitmentCode}, cdsId=${commitment.courseOfStudyId}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitmentsByDate (no filter) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testExportSystemLog() {
        try {
            val exports = api.calesa.exportSystemLog(limit = 10)
            assertNotNull(exports)
            logger.info("exportSystemLog(limit=10): found ${exports.size} export entries")
            for (export in exports.take(3)) {
                logger.info(
                    "  export: elabId=${export.processingId}, des=${export.description}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("exportSystemLog not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testExportSystemLogByProcessingId() {
        try {
            val exports = api.calesa.exportSystemLog(limit = 5)
            if (exports.isEmpty()) {
                logger.warning("testExportSystemLogByProcessingId: no export entries available")
                return
            }

            val firstExport = exports.firstOrNull()
            val processingId = firstExport?.processingId
            if (processingId == null) {
                logger.warning("testExportSystemLogByProcessingId: first export has no processingId")
                return
            }

            val singleExport = api.calesa.exportSystemLogByProcessingId(processingId = processingId)
            assertNotNull(singleExport)
            logger.info(
                "exportSystemLogByProcessingId(processingId=$processingId): " +
                    "elabId=${singleExport.processingId}, des=${singleExport.description}"
            )
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("exportSystemLogByProcessingId not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("exportSystemLogByProcessingId validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testExportSystemLogEvents() {
        try {
            val exports = api.calesa.exportSystemLog(limit = 5)
            if (exports.isEmpty()) {
                logger.warning("testExportSystemLogEvents: no export entries available")
                return
            }

            val processingId = exports.firstOrNull()?.processingId ?: run {
                logger.warning("testExportSystemLogEvents: first export has no processingId")
                return
            }

            val events = api.calesa.exportSystemLogEvents(processingId = processingId, limit = 10)
            assertNotNull(events)
            logger.info(
                "exportSystemLogEvents(processingId=$processingId, limit=10): found ${events.size} events"
            )
            for (event in events.take(3)) {
                logger.info(
                    "  event: elabId=${event.processingId}, cdsId=${event.courseOfStudyId}, " +
                        "adId=${event.activityId}, des=${event.description}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("exportSystemLogEvents not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("exportSystemLogEvents validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testExportSystemLogSessions() {
        try {
            val exports = api.calesa.exportSystemLog(limit = 5)
            if (exports.isEmpty()) {
                logger.warning("testExportSystemLogSessions: no export entries available")
                return
            }

            val processingId = exports.firstOrNull()?.processingId ?: run {
                logger.warning("testExportSystemLogSessions: first export has no processingId")
                return
            }

            val logSessions = api.calesa.exportSystemLogSessions(processingId = processingId, limit = 10)
            assertNotNull(logSessions)
            logger.info(
                "exportSystemLogSessions(processingId=$processingId, limit=10): found ${logSessions.size} log sessions"
            )
            for (logSession in logSessions.take(3)) {
                logger.info(
                    "  logSession: elabId=${logSession.processingId}, " +
                        "aaSesId=${logSession.academicYearSessionId}, " +
                        "sesCod=${logSession.sessionCode}, des=${logSession.description}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("exportSystemLogSessions not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("exportSystemLogSessions validation error: ${e.message}")
        }
    }

    @Test
    @Disabled("Destructive: creates a new exam call in the system")
    suspend fun testPostExamCall() {
        // NOTE: Creating an exam call is irreversible without separate delete.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: modifies an existing exam call")
    suspend fun testPutExamCall() {
        // NOTE: Modifying an exam call may change production data.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: deletes an existing exam call")
    suspend fun testDeleteExamCall() {
        // NOTE: Deleting an exam call is irreversible.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: enrolls a student in an exam - irreversible without corresponding delete")
    suspend fun testPostExamCallEnrolledList() {
        // NOTE: This creates a booking (iscrizione) for a student in an exam.
        // Must not be executed in automated tests without explicit rollback.
    }

    @Test
    @Disabled("Destructive: modifies an existing exam booking")
    suspend fun testPutModifyBooking() {
        // NOTE: Modifying a booking changes enrollment details.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: removes a student's exam enrollment - may cause data loss")
    suspend fun testDeleteExamCallEnrolledList() {
        // NOTE: Deleting an enrollment removes the booking from the system.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: updates exam outcomes for a student - irreversible grade change")
    suspend fun testPutApplicationListOutcome() {
        // NOTE: This writes exam results to the system.
        // Must not be executed in automated tests.
    }

    @Test
    @Disabled("Destructive: marks that student has acknowledged their exam result")
    suspend fun testPutAcknowledgmentOfReceipt() {
        // NOTE: Sets the presaVisione (acknowledgment of receipt) flag for a student enrollment.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: marks that student has acknowledged their exam result via booking")
    suspend fun testPutAcknowledgmentOfReceiptApplicationList() {
        // NOTE: Sets the presaVisione flag via the booking endpoint.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: publishes exam call results - triggers email notifications")
    suspend fun testPutExamCallPublication() {
        // NOTE: This triggers publication of exam results and may send notifications.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: publishes shift results - triggers email notifications")
    suspend fun testPutShiftPublication() {
        // NOTE: This triggers publication of shift-level exam results and may send notifications.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: creates or updates ESACOM child association")
    suspend fun testPutEsacomChild() {
        // NOTE: This creates/updates a shared exam (esacom) child relationship.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: deletes ESACOM child association")
    suspend fun testDeleteEsacomChild() {
        // NOTE: This removes a shared exam child association from the system.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: imports external system log commitments")
    suspend fun testImportSystemLog() {
        // NOTE: This imports commitment data from external systems.
        // Disabled to prevent unintended side effects.
    }

    @Test
    @Disabled("Destructive: updates system log commitments in bulk")
    suspend fun testUpdateCommitment() {
        // NOTE: This bulk-updates commitment records in the external system log.
        // Disabled to prevent unintended side effects.
    }
}
