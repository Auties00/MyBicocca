package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAppealSession
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamBookingResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamsSessionsResponse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class BicoccappExamsApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getExams() {
        val exams = api.exams.getExams(profile.enrollmentId)
        assertNotNull(exams.career.exams)
    }

    @Test
    suspend fun getExamsSessions() {
        val sessions = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
        assertNotNull(sessions.career)
    }

    @Test
    suspend fun manageExamSession() {
        val sessions = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
        val career = sessions.career
        if (career.courses.isEmpty()) {
            return
        }

        var success = false
        for (course in career.courses) {
            if (success) break

            for (appeal in course.appeals) {
                if (success) break

                if (!appeal.isBookable) continue

                val cdsId = course.degreeCourseId
                val activityId = course.activityId
                val activityItemId = course.activityItemId
                val activityAppealId = appeal.activityAppealId

                // Test Add then Cancel
                val addResponse = api.exams.addExamSession(
                    cdsId, activityId, activityItemId, activityAppealId
                )
                if (!addResponse.isAcceptable()) continue

                // Verify
                val verify1 = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
                val updatedAppeal1 = findAppeal(verify1, activityAppealId)
                assertTrue(updatedAppeal1?.status != "P")

                // Rollback (Cancel)
                val cancelResponse = api.exams.cancelExamSession(
                    cdsId, activityId, activityItemId, activityAppealId, profile.studentId
                )
                if (!cancelResponse.isAcceptable()) continue

                // Verify Rollback
                val verify2 = api.exams.getExamsSessions(profile.personId, profile.enrollmentId)
                val updatedAppeal2 = findAppeal(verify2, activityAppealId)
                assertTrue(updatedAppeal2?.status == "P")

                success = true
            }
        }

        if (!success) {
            fail {
                "Cannot manage exam sessions: no valid entry"
            }
        }
    }

    private fun BicoccappExamBookingResponse.isAcceptable(): Boolean {
        return when (this) {
            is BicoccappExamBookingResponse.Success -> true
            is BicoccappExamBookingResponse.Error -> {
                if (redirectUrl != null) {
                    false
                } else {
                    fail {
                        "Cannot manage exam sessions: $message (status: $status, code: $code)"
                    }
                }
            }
        }
    }

    private fun findAppeal(
        response: BicoccappExamsSessionsResponse,
        appealId: Int
    ): BicoccappAppealSession? {
        response.career.courses.forEach { course ->
            course.appeals.forEach { appeal ->
                if (appeal.activityAppealId == appealId) return appeal
            }
        }
        return null
    }
}
