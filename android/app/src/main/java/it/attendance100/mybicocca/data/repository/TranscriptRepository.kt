package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.TranscriptDao
import it.attendance100.mybicocca.data.datasource.transcript.Esse3TranscriptDataSource
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptRepository @Inject constructor(
    private val dataSource: Esse3TranscriptDataSource,
    private val dao: TranscriptDao,
) {
    fun observeRows(careerId: Long): Flow<List<RecordBookRow>> = dao.observeRows(careerId)

    fun observeStats(careerId: Long): Flow<RecordBookStats?> = dao.observeStats(careerId)

    suspend fun refresh(careerId: Long): Result<Unit> = runCatching {
        val rows = dataSource.getRecordBookRows(careerId)
        dao.upsertAllRows(rows)
        val stats = dataSource.getRecordBookStats(careerId)
        dao.upsertStats(stats)
    }
}
