package it.attendance100.mybicocca.data.api.esse3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3CareerApi].
 */
class Esse3CareerApiTest : Esse3TestBase() {

    @Test
    fun `getAcademicRecord returns libretto data`() {
        runBlocking {
            val record = api.career.getAcademicRecord()

            assertNotNull(record, "Academic record should not be null")
            assertNotNull(record.courses, "Courses list should not be null")
            assertNotNull(record.statistics, "Statistics should not be null")

            println("Academic record retrieved successfully")
            println("Total courses: ${record.courses.size}")

            record.courses.take(10).forEach { course ->
                println("  [${course.code}] ${course.name}")
                println("    Year: ${course.year}, Credits: ${course.credits}, Status: ${course.status}")
                course.grade?.let { println("    Grade: $it (${course.examDate})") }
            }

            if (record.courses.size > 10) {
                println("  ... and ${record.courses.size - 10} more courses")
            }

            println("\nStatistics:")
            record.statistics.weightedAverage?.let { println("  Weighted Average: $it") }
            record.statistics.arithmeticAverage?.let { println("  Arithmetic Average: $it") }
            println("  Earned Credits: ${record.statistics.earnedCredits}/${record.statistics.totalCredits}")
        }
    }

    @Test
    fun `getStudyPlan returns plan data`() {
        runBlocking {
            val plan = api.career.getStudyPlan()

            if (plan != null) {
                println("Study plan retrieved successfully")
                println("Plan ID: ${plan.id}")
                println("Status: ${plan.status}")
                println("Type: ${plan.type}")
                println("Planned courses: ${plan.courses.size}")

                plan.courses.take(5).forEach { course ->
                    println("  [${course.code}] ${course.description} - ${course.credits} CFU")
                }
            } else {
                println("No study plan available for this student")
            }
        }
    }

    @Test
    fun `getCourseEvaluations returns pending questionnaires`() {
        runBlocking {
            val evaluations = api.career.getCourseEvaluations()

            assertNotNull(evaluations, "Course evaluations should not be null")

            println("Pending course evaluations: ${evaluations.size}")
            evaluations.forEach { eval ->
                println("  [${eval.courseCode}] ${eval.courseName}")
                println("    Year: ${eval.courseYear}, Credits: ${eval.credits}")
                eval.academicYear?.let { println("    Academic Year: $it") }
                eval.status?.let { println("    Status: $it") }
                if (eval.isRecognized) println("    [RECOGNIZED]")
                if (eval.questionnaireUrl != null) {
                    println("    Questionnaire available")
                }
            }
        }
    }

    @Test
    fun `printStudyPlan returns PDF bytes`() {
        runBlocking {
            val plan = api.career.getStudyPlan()

            if (plan != null && plan.id > 0) {
                val pdfBytes = api.career.printStudyPlan(plan.id)

                assertNotNull(pdfBytes, "PDF bytes should not be null")
                assertTrue(pdfBytes.isNotEmpty(), "PDF bytes should not be empty")

                // Check PDF header %PDF-
                val isPdf = pdfBytes.size > 4 &&
                        pdfBytes[0] == 0x25.toByte() &&
                        pdfBytes[1] == 0x50.toByte() &&
                        pdfBytes[2] == 0x44.toByte() &&
                        pdfBytes[3] == 0x46.toByte()

                assertTrue(isPdf, "Should be a valid PDF file")

                println("Study plan PDF retrieved: ${pdfBytes.size} bytes")
            } else {
                println("No study plan available to test printing")
            }
        }
    }
}
