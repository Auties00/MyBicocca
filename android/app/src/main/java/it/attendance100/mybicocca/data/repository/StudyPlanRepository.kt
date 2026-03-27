package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.StudyPlanDao
import it.attendance100.mybicocca.data.datasource.studyplan.Esse3StudyPlanDataSource
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import kotlinx.coroutines.flow.Flow
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
}
