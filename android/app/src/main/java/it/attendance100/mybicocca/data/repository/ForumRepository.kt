package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.database.dao.ForumDao
import it.attendance100.mybicocca.data.datasource.forum.ElearningForumDataSource
import it.attendance100.mybicocca.data.model.forum.Forum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepository @Inject constructor(
    private val dataSource: ElearningForumDataSource,
    private val dao: ForumDao,
    private val courseDao: CourseDao,
) {
    fun observeAll(): Flow<List<Forum>> = dao.observeAll()

    fun observeByCourse(courseId: Int): Flow<List<Forum>> = dao.observeByCourse(courseId)

    suspend fun refresh(): Result<Unit> = runCatching {
        val courseIds = courseDao.observeAll().first().map { it.id }
        val forums = dataSource.getForums(courseIds)
        dao.upsertAll(forums)
    }
}
