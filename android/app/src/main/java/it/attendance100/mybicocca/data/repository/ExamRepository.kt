package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.ExamDao
import it.attendance100.mybicocca.data.datasource.exam.Esse3ExamDataSource
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepository @Inject constructor(
    private val esse3Exam: Esse3ExamDataSource,
    private val dao: ExamDao,
) {
    fun observeExamCalls(): Flow<List<ExamCall>> = dao.observeExamCalls()

    fun observeExamCallsByCareer(careerId: Long): Flow<List<ExamCall>> =
        dao.observeExamCallsByCareer(careerId)

    fun observeBookings(): Flow<List<ExamBooking>> = dao.observeBookings()

    suspend fun refreshExamCalls(
        careerId: Long,
        matricolaId: Long? = null,
    ): Result<Unit> = runCatching {
        val calls = esse3Exam.getExamCalls(careerId, matricolaId)
        dao.deleteAllCalls()
        dao.upsertCalls(calls)
    }

    suspend fun refreshBookings(matricolaId: Long?): Result<Unit> = runCatching {
        val bookings = esse3Exam.getBookings(matricolaId)
        dao.deleteAllBookings()
        dao.upsertBookings(bookings)
    }

    suspend fun getExamCallById(id: Long): ExamCall? = dao.getExamCallById(id)
}
