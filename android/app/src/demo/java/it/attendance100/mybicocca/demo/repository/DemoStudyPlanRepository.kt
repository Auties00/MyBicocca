package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathFacet
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject
import javax.inject.Singleton

/** An approved standard plan listing [DemoCareer.activities]; the editing window is closed. */
@Singleton
class DemoStudyPlanRepository @Inject constructor() : StudyPlanRepository {

    private val common = StudyPathFacet(code = "GGG", description = "Percorso comune")

    override suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse> =
        DemoCourses.current.map { course ->
            PlannedCourse(
                easyStaffSubjectId = "EC${course.moodleCourseId}",
                easyStaffSubjectCode = course.code,
                activityCode = course.code,
                name = course.name,
                normalizedName = normalizeSubjectName(course.name),
                teacherName = course.teacher,
                periodId = "S2",
                semester = Semester.Second,
                studyYear = StudyYear(course.year),
                cfu = course.cfu,
            )
        }

    override suspend fun getStudyPlan(careerId: CareerId): StudyPlan = StudyPlan(
        planId = PLAN_ID,
        studentId = careerId.value,
        type = StudyPlanType.Standard,
        statusDescription = "Approvato",
        lastUpdated = DemoClock.now.toLocalDate().minusMonths(8),
        choiceRegulationId = REGULATION_ID,
        schemaId = SCHEMA_ID,
        courses = DemoCareer.activities.map {
            StudyPlanCourse(id = it.id, name = it.name, code = it.code, credits = it.credits, year = StudyYear(it.year))
        },
    )

    override suspend fun getStudyPath(careerId: CareerId): StudyPath = StudyPath(
        percorso = common,
        orientamento = null,
        profilo = null,
        partTime = null,
        choiceRegulationId = REGULATION_ID,
        currentSchemaId = SCHEMA_ID,
        options = listOf(
            StudyPathOption(
                schemaId = SCHEMA_ID,
                schemaCode = "E3101Q-GGG",
                schemaDescription = "Piano statutario",
                percorso = common,
                orientamento = null,
                profilo = null,
                partTime = null,
                isCurrent = true,
                approval = PlanApprovalType.Automatic,
                languages = listOf("Italiano"),
            ),
        ),
        editingOpen = false,
        choiceAvailable = false,
    )

    override suspend fun getStudyPlanPrint(careerId: CareerId, planId: Long): ByteArray = DemoCareer.pdf(
        "Piano di studio",
        DemoCareer.activities.map { "${it.year}° anno · ${it.name} · ${it.credits.toInt()} CFU" },
    )

    override suspend fun getStudyPlanDraft(
        careerId: CareerId,
        planId: Long?,
        choiceRegulationId: Long,
        schemaId: Long,
    ): List<EditableRule> = emptyList()

    override suspend fun submitStudyPlan(careerId: CareerId, rules: List<EditableRule>, chosenPath: StudyPathOption?) = Unit

    private companion object {
        const val PLAN_ID = 810001L
        const val REGULATION_ID = 820001L
        const val SCHEMA_ID = 830001L
    }
}
