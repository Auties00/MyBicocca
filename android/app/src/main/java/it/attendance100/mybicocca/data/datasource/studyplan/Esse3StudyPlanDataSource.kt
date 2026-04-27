package it.attendance100.mybicocca.data.datasource.studyplan

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.dto.esse3.Esse3PlanType
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeader
import it.attendance100.mybicocca.data.model.document.AppDocument
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import it.attendance100.mybicocca.util.toAppDocument
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

    suspend fun getPlannedCourses(studentId: Long, planId: Long): List<PlannedCourse> =
        withContext(ioDispatcher) {
            val plan = esse3Api.plans.getStudentPlan(studentId, planId, optionalFields = "ALL")
            plan.activity.mapNotNull { it.toPlannedCourse(planId) }
        }

    private fun Esse3StudyPlanHeader.toStudyPlanHeader(studentId: Long): StudyPlanHeader {
        val resolvedPlanType = planType
        return StudyPlanHeader(
            id = planId?.toLong() ?: 0L,
            studentId = studentId,
            description = when (resolvedPlanType) {
                Esse3PlanType.Standard -> "Piano standard"
                Esse3PlanType.Individual -> "Piano individuale"
                is Esse3PlanType.Unknown -> "Piano ${resolvedPlanType.value}"
                null -> null
            },
            statusCode = state?.value,
            statusDescription = stateDescription,
            lastUpdated = lastStateChangeDate,
        )
    }

    private fun Esse3StudyPlanActivity.toPlannedCourse(planId: Long): PlannedCourse? {
        if (choiceFlag == 0) return null
        val courseId = activityChoiceId
            ?: choiceActivityId
            ?: teachingActivityChoiceId?.toLong()
            ?: return null
        val name = activityTranscriptDescription
            ?: contextualizedTeachingActivityKey?.activityDescription
            ?: return null
        return PlannedCourse(
            id = courseId,
            planId = planId,
            activityName = name,
            activityCode = activityTranscriptCode ?: contextualizedTeachingActivityKey?.activityCode,
            credits = weight ?: 0f,
            year = courseYear,
            partialCode = partitionKey?.partialCode?.takeIf { it.isNotBlank() },
        )
    }

    suspend fun getPlanPrint(studentId: Long, planId: Long): AppDocument = withContext(ioDispatcher) {
        esse3Api.plans.getPlanPrint(studentId, planId)
            .toAppDocument(fileName = "piano_studi_${planId}.pdf")
    }
}
