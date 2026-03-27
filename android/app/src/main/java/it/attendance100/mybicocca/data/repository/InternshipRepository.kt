package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.InternshipDao
import it.attendance100.mybicocca.data.datasource.internship.Esse3InternshipDataSource
import it.attendance100.mybicocca.data.model.internship.InternshipApplication
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternshipRepository @Inject constructor(
    private val dataSource: Esse3InternshipDataSource,
    private val dao: InternshipDao,
) {
    fun observeAll(): Flow<List<InternshipApplication>> = dao.observeAll()

    fun observeByStudent(studentId: Long): Flow<List<InternshipApplication>> =
        dao.observeByStudent(studentId)

    suspend fun refresh(studentId: Long): Result<Unit> = runCatching {
        val applications = dataSource.getInternshipApplications(studentId)
        dao.deleteAll()
        dao.upsertAll(applications)
    }
}
