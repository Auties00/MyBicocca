package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.datasource.grade.ElearningGradeDataSource
import it.attendance100.mybicocca.data.model.course.CourseGrade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    private val dataSource: ElearningGradeDataSource,
    private val courseDao: CourseDao,
) {
    fun observeGrades(): Flow<List<CourseGrade>> = courseDao.observeGrades()

    suspend fun refresh(): Result<Unit> = runCatching {
        val courses = courseDao.observeAll().first()
        val courseNameMap = courses.associate { it.id to it.fullName }
        val grades = dataSource.getCourseGrades().map { grade ->
            grade.copy(courseName = courseNameMap[grade.courseId] ?: "")
        }
        courseDao.upsertGrades(grades)
    }
}
