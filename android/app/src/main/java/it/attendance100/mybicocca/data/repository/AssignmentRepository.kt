package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.AssignmentDao
import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.datasource.assignment.ElearningAssignmentDataSource
import it.attendance100.mybicocca.data.model.assignment.Assignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val dataSource: ElearningAssignmentDataSource,
    private val dao: AssignmentDao,
    private val courseDao: CourseDao,
) {
    fun observeAll(): Flow<List<Assignment>> = dao.observeAll()

    fun observeByCourse(courseId: Int): Flow<List<Assignment>> = dao.observeByCourse(courseId)

    suspend fun refresh(): Result<Unit> = runCatching {
        val courseIds = courseDao.observeAll().first().map { it.id }
        val assignments = dataSource.getAssignments(courseIds)
        dao.upsertAll(assignments)
    }
}
