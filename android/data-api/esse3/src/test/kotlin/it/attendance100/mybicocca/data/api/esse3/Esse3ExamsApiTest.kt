package it.attendance100.mybicocca.data.api.esse3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3ExamsApi].
 */
class Esse3ExamsApiTest : Esse3TestBase() {

    @Test
    fun `getAvailableExamSessions returns exam list`() {
        runBlocking {
            val sessions = api.exams.getAvailableExamSessions()

            assertNotNull(sessions, "Exam sessions should not be null")

            println("Available exam sessions: ${sessions.size}")
            sessions.take(10).forEach { exam ->
                println("  [${exam.courseCode}] ${exam.courseName}")
                exam.date?.let { println("    Date: $it") }
                println("    Type: ${exam.type}")
                exam.location?.let { println("    Location: $it") }
                exam.professor?.let { println("    Professor: $it") }
            }

            if (sessions.size > 10) {
                println("  ... and ${sessions.size - 10} more exam sessions")
            }
        }
    }

    @Test
    fun `getExamReservations returns current reservations`() {
        runBlocking {
            val reservations = api.exams.getExamReservations()

            assertNotNull(reservations, "Exam reservations should not be null")

            println("Current exam reservations: ${reservations.size}")
            reservations.forEach { reservation ->
                println("  [${reservation.courseCode}] ${reservation.courseName}")
                reservation.date?.let { println("    Date: $it") }
                println("    Type: ${reservation.type}")
                reservation.location?.let { println("    Location: $it") }
            }
        }
    }

    @Test
    fun `getExamResults returns past results`() {
        runBlocking {
            val results = api.exams.getExamResults()

            assertNotNull(results, "Exam results should not be null")

            println("Exam results: ${results.size}")
            results.take(10).forEach { result ->
                println("  [${result.courseCode}] ${result.courseName}")
                result.date?.let { println("    Date: $it") }
                println("    Grade: ${result.grade}")
                println("    Status: ${result.status}")
                result.professor?.let { println("    Professor: $it") }
            }

            if (results.size > 10) {
                println("  ... and ${results.size - 10} more results")
            }
        }
    }

    @Test
    fun `printReservation returns PDF bytes`() {
        runBlocking {
            val reservations = api.exams.getExamReservations()

            if (reservations.isNotEmpty()) {
                val firstReservation = reservations.first()
                val pdfBytes = api.exams.printReservation(firstReservation)

                assertNotNull(pdfBytes, "PDF bytes should not be null")
                assertTrue(pdfBytes.isNotEmpty(), "PDF bytes should not be empty")

                // Check PDF header %PDF-
                val isPdf = pdfBytes.size > 4 &&
                        pdfBytes[0] == 0x25.toByte() &&
                        pdfBytes[1] == 0x50.toByte() &&
                        pdfBytes[2] == 0x44.toByte() &&
                        pdfBytes[3] == 0x46.toByte()

                assertTrue(isPdf, "Should be a valid PDF file")

                println("Exam reservation PDF retrieved: ${pdfBytes.size} bytes")
            } else {
                println("No reservations available to test printing")
            }
        }
    }
}
