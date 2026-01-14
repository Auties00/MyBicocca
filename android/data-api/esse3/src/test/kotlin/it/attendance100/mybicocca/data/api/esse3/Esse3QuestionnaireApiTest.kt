package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3EvaluationCourseStatus
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Esse3QuestionnaireApiTest : Esse3TestBase() {

    @Test
    suspend fun getEvaluationCourses() {
        val courses = api.questionnaires.getEvaluationCourses()
        assertNotNull(courses)
    }

    @Test
    suspend fun getEvaluationPartitions() {
        val courses = api.questionnaires.getEvaluationCourses()
        for (course in courses) {
            val partitions = api.questionnaires.getEvaluationPartitions(course)
            assertNotNull(partitions)
            assertTrue(course.status !is Esse3EvaluationCourseStatus.Pending || partitions.isNotEmpty())
        }
    }

    @Test
    suspend fun navigateQuestionnaire() {
        val courses = api.questionnaires.getEvaluationCourses()
        for (course in courses) {
            val partitions = api.questionnaires.getEvaluationPartitions(course)
            for(partition in partitions) {
                val page = api.questionnaires.startQuestionnaire(partition)
                if(page != null) {
                    api.questionnaires.exitQuestionnaire(page)
                }
            }
        }
    }
}
