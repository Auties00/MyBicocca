package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CareerDao
import it.attendance100.mybicocca.data.datasource.career.Esse3CareerDataSource
import it.attendance100.mybicocca.data.model.career.Career
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerRepository @Inject constructor(
    private val dataSource: Esse3CareerDataSource,
    private val dao: CareerDao,
) {
    fun observeAll(): Flow<List<Career>> = dao.observeAll()

    suspend fun refresh(): Result<Unit> = runCatching {
        val careers = dataSource.getCareers()
        dao.upsertAll(careers)
    }
}
