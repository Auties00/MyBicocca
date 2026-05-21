package it.attendance100.mybicocca.data.remote.bicoccapp.api

import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappAppealSession
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappExamBookingResult
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappExamSession
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappExamsApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getExams() {
        val exams = api.exams.getExams(profile.enrollmentId)
        assertNotNull(exams.notations)
        assertTrue(exams.notations.isNotEmpty())
        assertNotNull(exams.exams)
        assertTrue(exams.exams.isNotEmpty())
        assertNotNull(exams.remainingExams)
        assertTrue(exams.remainingExams.isNotEmpty())
    }


    @Test
    suspend fun getExamsSessions() {
        val sessions = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
        assertNotNull(sessions)
        assertTrue(sessions.isNotEmpty())
    }

    @Test
    suspend fun manageExamSession() {
        val courses = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
        for (course in courses) {
            for (appeal in course.appeals) {
                if (!appeal.isBookable) continue

                val cdsId = course.degreeCourseId
                val activityId = course.activityId
                val activityItemId = course.activityItemId
                val activityAppealId = appeal.activityAppealId

                // Test Add then Cancel
                val addResponse = api.exams.addExamSession(
                    cdsId, activityId, activityItemId, activityAppealId
                )
                if (addResponse !is BicoccappExamBookingResult.Success) continue

                // Verify
                val verify1 = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
                val updatedAppeal1 = findAppeal(verify1, activityAppealId)
                assertTrue(updatedAppeal1?.status != "P")

                // Rollback (Cancel)
                val cancelResponse = api.exams.cancelExamSession(
                    cdsId, activityId, activityItemId, activityAppealId, profile.studentId
                )
                if (cancelResponse !is BicoccappExamBookingResult.Success) continue

                // Verify Rollback
                val verify2 = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
                val updatedAppeal2 = findAppeal(verify2, activityAppealId)
                assertTrue(updatedAppeal2?.status == "P")
            }
        }
    }

    private fun findAppeal(
        session: List<BicoccappExamSession>,
        appealId: Int
    ): BicoccappAppealSession? {
        return session.firstNotNullOfOrNull { session ->
            session.appeals.firstOrNull { appeal ->
                appeal.activityAppealId == appealId
            }
        }
    }
}
