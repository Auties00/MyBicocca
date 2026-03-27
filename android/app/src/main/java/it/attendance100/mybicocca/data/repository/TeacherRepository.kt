package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.TeacherDao
import it.attendance100.mybicocca.data.datasource.teacher.EasyStaffTeacherDataSource
import it.attendance100.mybicocca.data.model.teacher.Teacher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val dataSource: EasyStaffTeacherDataSource,
    private val dao: TeacherDao,
) {
    fun observeAll(): Flow<List<Teacher>> = dao.observeAll()

    fun search(query: String): Flow<List<Teacher>> = dao.search(query)

    suspend fun getByCode(code: String): Teacher? = dao.getByCode(code)

    suspend fun refresh(): Result<Unit> = runCatching {
        val teachers = dataSource.getTeachers()
        dao.deleteAll()
        dao.upsertAll(teachers)
    }
}
