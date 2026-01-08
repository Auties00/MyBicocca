package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3EvaluationCourseStatus
import it.attendance100.mybicocca.data.dto.esse3.Esse3EvaluationPartitionStatus
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
        val pendingCourse = courses.firstOrNull {
            it.status is Esse3EvaluationCourseStatus.Pending ||
            it.status is Esse3EvaluationCourseStatus.Completed
        }
        if (pendingCourse == null) return

        val partitions = api.questionnaires.getEvaluationPartitions(pendingCourse)
        assertNotNull(partitions)
        assertTrue(partitions.isNotEmpty())
    }

    @Test
    suspend fun navigateQuestionnaire() {
        val courses = api.questionnaires.getEvaluationCourses()
        val pendingCourse = courses.firstOrNull { it.status is Esse3EvaluationCourseStatus.Pending }
        if (pendingCourse == null) return

        val partitions = api.questionnaires.getEvaluationPartitions(pendingCourse)
        val pendingPartition = partitions.firstOrNull { it.status is Esse3EvaluationPartitionStatus.Pending }
        if (pendingPartition == null) return

        val page = api.questionnaires.startQuestionnaire(pendingPartition)
        assertNotNull(page.navigation)
        assertNotNull(page.questions)

        api.questionnaires.exitQuestionnaire(page)
    }
}
