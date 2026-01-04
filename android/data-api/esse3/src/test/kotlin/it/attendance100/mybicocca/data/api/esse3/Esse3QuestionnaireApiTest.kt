package it.attendance100.mybicocca.data.api.esse3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3QuestionnaireApi].
 */
class Esse3QuestionnaireApiTest : Esse3TestBase() {

    @Test
    fun `getPendingQuestionnaires returns questionnaire list`() {
        runBlocking {
            val questionnaires = api.questionnaires.getPendingQuestionnaires()

            assertNotNull(questionnaires, "Questionnaires should not be null")

            println("Pending questionnaires: ${questionnaires.size}")
            questionnaires.forEach { quest ->
                println("  - ${quest.title}")
                println("    ID: ${quest.id}, Composition: ${quest.compositionId}")
                println("    Status: ${quest.status}")
                if (quest.required) println("    [REQUIRED]")
                quest.courseCode?.let { println("    Course: [$it] ${quest.courseName}") }
                quest.professor?.let { println("    Professor: $it") }
            }
        }
    }

    @Test
    fun `getCourseEvaluations returns evaluation list`() {
        runBlocking {
            val evaluations = api.questionnaires.getCourseEvaluations()

            assertNotNull(evaluations, "Course evaluations should not be null")

            println("Course evaluations: ${evaluations.size}")
            evaluations.forEach { eval ->
                println("  - [${eval.courseCode}] ${eval.courseName}")
                println("    Year: ${eval.courseYear}, Credits: ${eval.credits}")
                eval.academicYear?.let { println("    Academic Year: $it") }
                if (eval.isRecognized) println("    [RECOGNIZED]")
                if (eval.questionnaireUrl != null) {
                    println("    Questionnaire available")
                }
            }
        }
    }

    @Test
    fun `startQuestionnaire loads first page`() {
        runBlocking {
            val questionnaires = api.questionnaires.getPendingQuestionnaires()

            if (questionnaires.isNotEmpty()) {
                val quest = questionnaires.first()
                println("Starting questionnaire: ${quest.title}")

                val page = api.questionnaires.startQuestionnaire(quest)

                if (page != null) {
                    println("Questionnaire page loaded:")
                    println("  Page ID: ${page.id}")
                    println("  Page Number: ${page.pageNumber}")
                    page.title?.let { println("  Title: $it") }
                    println("  Questions: ${page.questions.size}")

                    page.questions.take(3).forEach { question ->
                        println("    - ${question.text}")
                        println("      Type: ${question::class.simpleName}")
                        println("      Field: ${question.fieldName}")
                        if (question.required) println("      [REQUIRED]")
                    }

                    if (page.questions.size > 3) {
                        println("    ... and ${page.questions.size - 3} more questions")
                    }
                } else {
                    println("Could not load questionnaire page")
                }
            } else {
                println("No pending questionnaires to test")
            }
        }
    }
}
