package it.attendance100.mybicocca.data.remote.esse3.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalDate

class Esse3ExamsApiTest : Esse3TestBase() {
    companion object {
        private const val MOCK_EXAM_NOTES = "TEST - PRENOTAZIONE AUTOMATICA"
    }

    @Test
    suspend fun getAvailableExamSessions() {
        val sessions = api.exams.getAvailableExamSessions()
        assertNotNull(sessions)
    }

    @Test
    suspend fun getExamSessionInfo() {
        val sessions = api.exams.getAvailableExamSessions()
        for (session in sessions) {
            val sessionInfo = api.exams.getExamSessionInfo(session)
            assertNotNull(sessionInfo.teachingActivity)
            assertNotNull(sessionInfo.description)
            assertNotNull(sessionInfo.type)
            assertNotNull(sessionInfo.datetime)
        }
    }

    @Test
    suspend fun getExamReservations() {
        val reservations = api.exams.getExamReservations()
        assertNotNull(reservations)
    }

    @Test
    suspend fun printExamReservation() {
        val reservations = api.exams.getExamReservations()
        for (reservation in reservations) {
            val pdfChannel = api.exams.printExamReservation(reservation)
            assertNotNull(pdfChannel)
        }
    }

    @Test
    suspend fun getExamReservationsHistory() {
        val history = api.exams.getExamReservationsHistory()
        assertNotNull(history)
    }

    @Test
    suspend fun manageExamReservation() {
        val today = LocalDate.now()
        val sessions = api.exams.getAvailableExamSessions()
        val bookableSessions = sessions.filter { session ->
            !session.registrationStartDate.isAfter(today) && !session.registrationEndDate.isBefore(today)
        }
        if (bookableSessions.isEmpty()) {
            return
        }

        var success = false
        for (session in bookableSessions) {
            if (success) break

            try {
                api.exams.reserveExamSession(session, MOCK_EXAM_NOTES)

                val reservations = api.exams.getExamReservations()
                val newReservation = reservations.find { it.teachingActivity.contains(session.courseName, ignoreCase = true) }
                    ?: continue

                assertTrue(reservations.isNotEmpty())

                api.exams.cancelExamReservation(newReservation)

                val verifyReservations = api.exams.getExamReservations()
                val cancelled = verifyReservations.none {
                    it.sessionId == newReservation.sessionId &&
                    it.teachingActivityId == newReservation.teachingActivityId
                }
                assertTrue(cancelled)

                success = true
            } catch (_: Throwable) {
                continue
            }
        }
        if (!success && bookableSessions.isNotEmpty()) {
            fail { "Cannot manage exam reservation: no valid session found for booking" }
        }
    }
}
