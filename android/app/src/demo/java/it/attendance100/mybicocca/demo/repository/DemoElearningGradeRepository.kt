package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItemType
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoElearningGradeRepository @Inject constructor() : ElearningGradeRepository {

    // Homework, quiz and mid-term marks out of 30; null where nothing has been graded yet.
    private val marks = mapOf(
        DemoCourses.algorithms to listOf(27.0, 30.0, 26.0),
        DemoCourses.databases to listOf(28.0, 25.0, 29.0),
        DemoCourses.operatingSystems to listOf(24.0, 27.0, null),
        DemoCourses.statistics to listOf(30.0, 28.0, 27.0),
        DemoCourses.languages to listOf(26.0, null, null),
        DemoCourses.programming to listOf(29.0, 30.0, 28.0),
        DemoCourses.softwareEngineering to listOf(null, null, null),
    )

    override fun observeCourseGradeItems(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<GradeItem>>> {
        val course = DemoElearning.course(courseId) ?: return flowOf(Loadable.Loaded(emptyList()))
        return flowOf(Loadable.Loaded(items(course)))
    }

    override fun observeAllCourseGrades(accountId: AccountId): Flow<Loadable<List<CourseGradeOverview>>> =
        flowOf(
            Loadable.Loaded(
                DemoCourses.all.map { course ->
                    val average = average(course)
                    CourseGradeOverview(
                        courseId = CourseId(course.moodleCourseId),
                        courseName = DemoElearning.enrolled.first { it.id.value == course.moodleCourseId }.fullName,
                        grade = average,
                        maxGrade = MAX,
                        gradeFormatted = average?.let(::format),
                    )
                },
            ),
        )

    override suspend fun refreshCourseGradeItems(accountId: AccountId, courseId: CourseId, force: Boolean) = Unit

    override suspend fun refreshAllCourseGrades(accountId: AccountId, force: Boolean) = Unit

    override suspend fun clearForAccount(accountId: AccountId) = Unit

    private fun average(course: DemoCourse): Double? =
        marks.getValue(course).filterNotNull().takeIf { it.isNotEmpty() }?.average()

    private fun items(course: DemoCourse): List<GradeItem> {
        val names = listOf(
            "Esercitazioni in aula" to "assign",
            "Quiz di autovalutazione" to "quiz",
            "Prova in itinere" to "assign",
        )
        val activities = names.mapIndexed { index, (name, activity) ->
            val grade = marks.getValue(course)[index]
            GradeItem(
                id = course.moodleCourseId * 10L + index,
                name = name,
                type = GradeItemType.Activity,
                activityType = activity,
                grade = grade,
                maxGrade = MAX,
                percentage = grade?.let { it / MAX * 100 },
                gradeFormatted = grade?.let(::format),
                feedback = if (grade != null && index == 0) "Buon lavoro, soluzione chiara e ben commentata." else null,
                gradedAt = grade?.let { DemoClock.instant(DemoClock.now.minusDays(7L * (3 - index))) },
            )
        }
        val average = average(course)
        val total = GradeItem(
            id = course.moodleCourseId * 10L + 9,
            name = "Totale del corso",
            type = GradeItemType.Course,
            activityType = null,
            grade = average,
            maxGrade = MAX,
            percentage = average?.let { it / MAX * 100 },
            gradeFormatted = average?.let(::format),
            feedback = null,
            gradedAt = null,
        )
        return activities + total
    }

    private fun format(grade: Double): String = "%.2f".format(Locale.ITALY, grade)

    private companion object {
        const val MAX = 30.0
    }
}
