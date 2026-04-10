package it.attendance100.mybicocca.data.datasource.studyplan

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulationWindow
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeader
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3StudyPlanDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getStudyPlanHeaders(studentId: Long): List<StudyPlanHeader> =
        withContext(ioDispatcher) {
            esse3Api.plans.getStudentPlanHeaders(studentId).map { it.toStudyPlanHeader(studentId) }
        }

    suspend fun getCompilationWindows(choiceRegulationId: Long): List<Esse3ChoiceRegulationWindow> =
        withContext(ioDispatcher) {
            esse3Api.choiceRules.getChoiceRegulationWindows(choiceRegulationId)
        }

    suspend fun getPlannedCourses(studentId: Long, planId: Long): List<PlannedCourse> =
        withContext(ioDispatcher) {
            val plan = esse3Api.plans.getStudentPlan(studentId, planId)
            plan.activity.mapNotNull { it.toPlannedCourse(planId) }
        }

    private fun Esse3StudyPlanHeader.toStudyPlanHeader(studentId: Long) = StudyPlanHeader(
        id = planId?.toLong() ?: 0L,
        studentId = studentId,
        description = stateDescription,
        statusCode = state?.value,
        statusDescription = stateDescription,
        choiceRegulationId = choiceRegulationId,
        schemaId = schemaId?.toLong(),
        planType = planType?.value,
    )

    private fun Esse3StudyPlanActivity.toPlannedCourse(planId: Long): PlannedCourse? {
        val courseId = activityChoiceId ?: return null
        return PlannedCourse(
            id = courseId,
            planId = planId,
            activityName = activityTranscriptDescription ?: "",
            activityCode = activityTranscriptCode,
            credits = weight ?: 0f,
            year = courseYear,
            statusCode = null,
            statusDescription = null,
        )
    }
}
