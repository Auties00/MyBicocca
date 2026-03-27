package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.datasource.course.ElearningCourseDataSource
import it.attendance100.mybicocca.data.model.course.Course
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val dataSource: ElearningCourseDataSource,
    private val dao: CourseDao,
) {
    fun observeAll(): Flow<List<Course>> = dao.observeAll()

    suspend fun refresh(): Result<Unit> = runCatching {
        val courses = dataSource.getCourses()
        dao.upsertAll(courses)
    }
}
