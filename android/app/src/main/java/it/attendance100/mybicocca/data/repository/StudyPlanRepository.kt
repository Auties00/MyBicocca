package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.StudyPlanDao
import it.attendance100.mybicocca.data.datasource.studyplan.Esse3StudyPlanDataSource
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyPlanRepository @Inject constructor(
    private val dataSource: Esse3StudyPlanDataSource,
    private val dao: StudyPlanDao,
) {
    fun observeHeaders(studentId: Long): Flow<List<StudyPlanHeader>> =
        dao.observeHeaders(studentId)

    fun observeCourses(planId: Long): Flow<List<PlannedCourse>> = dao.observeCourses(planId)

    suspend fun refreshHeaders(studentId: Long): Result<Unit> = runCatching {
        val headers = dataSource.getStudyPlanHeaders(studentId)
        dao.upsertAllHeaders(headers)
    }

    suspend fun refreshCourses(studentId: Long, planId: Long): Result<Unit> = runCatching {
        val courses = dataSource.getPlannedCourses(studentId, planId)
        dao.upsertAllCourses(courses)
    }

    suspend fun isEditingWindowOpen(choiceRegulationId: Long): Boolean {
        val windows = dataSource.getCompilationWindows(choiceRegulationId)
        if (windows.isEmpty()) return false
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return windows.any { window ->
            val start = window.startDate?.take(10)
                ?.let { runCatching { LocalDate.parse(it, formatter) }.getOrNull() }
            val end = window.endDate?.take(10)
                ?.let { runCatching { LocalDate.parse(it, formatter) }.getOrNull() }
            val afterStart = start == null || !today.isBefore(start)
            val beforeEnd = end == null || !today.isAfter(end)
            afterStart && beforeEnd
        }
    }
}
