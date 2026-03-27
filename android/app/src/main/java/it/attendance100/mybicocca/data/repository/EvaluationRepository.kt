package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.EvaluationDao
import it.attendance100.mybicocca.data.datasource.evaluation.Esse3EvaluationDataSource
import it.attendance100.mybicocca.data.model.evaluation.EvaluationEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluationRepository @Inject constructor(
    private val dataSource: Esse3EvaluationDataSource,
    private val dao: EvaluationDao,
) {
    fun observeAll(): Flow<List<EvaluationEntry>> = dao.observeAll()

    fun observePending(): Flow<List<EvaluationEntry>> = dao.observePending()

    suspend fun refresh(matId: Long): Result<Unit> = runCatching {
        val entries = dataSource.getEvaluationEntries(matId)
        dao.deleteAll()
        dao.upsertAll(entries)
    }
}
