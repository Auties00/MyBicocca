package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.DegreeAwardDao
import it.attendance100.mybicocca.data.datasource.degreeaward.Esse3DegreeAwardDataSource
import it.attendance100.mybicocca.data.model.degreeaward.CommitteeApplication
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DegreeAwardRepository @Inject constructor(
    private val dataSource: Esse3DegreeAwardDataSource,
    private val dao: DegreeAwardDao,
) {
    fun observeAll(): Flow<List<CommitteeApplication>> = dao.observeAll()

    fun observeByStudent(studentId: Long): Flow<List<CommitteeApplication>> =
        dao.observeByStudent(studentId)

    suspend fun refresh(studentId: Long): Result<Unit> = runCatching {
        val applications = dataSource.getCommitteeApplications(studentId)
        dao.deleteAll()
        dao.upsertAll(applications)
    }
}
