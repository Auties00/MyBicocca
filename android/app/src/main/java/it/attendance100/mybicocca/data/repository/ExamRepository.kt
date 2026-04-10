package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.model.document.AppDocument
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
        matricolaId: Long?
    ): Result<Unit> = runCatching {
        refreshBookableExams(careerId, matricolaId)
    }

    suspend fun refreshBookings(matricolaId: Long?): Result<Unit> = runCatching {
        val bookings = esse3Exam.getBookings(matricolaId)
        dao.deleteAllBookings()
        dao.upsertBookings(bookings)
    }

    private suspend fun refreshBookableExams(careerId: Long, matricolaId: Long?) {
        val calls = esse3Exam.getExamCalls(careerId, matricolaId)
        dao.deleteAllCalls()
        dao.upsertCalls(calls)
    }

    suspend fun getExamCallById(id: Long): ExamCall? = dao.getExamCallById(id)

    suspend fun getExamCallDetailById(id: Long): ExamCall? = runCatching {
        val call = dao.getExamCallById(id) ?: return@runCatching null
        val detailed = esse3Exam.getExamCallDetail(call)
        dao.upsertCalls(listOf(detailed))
        detailed
    }.getOrNull()

    suspend fun bookExam(call: ExamCall): Result<Unit> = runCatching {
        esse3Exam.bookExam(call)
    }

    suspend fun cancelBooking(booking: ExamBooking): Result<Unit> = runCatching {
        esse3Exam.cancelBooking(booking)
    }

    suspend fun getBookingStatino(booking: ExamBooking): Result<AppDocument> = runCatching {
        esse3Exam.getBookingStatino(booking)
    }

    suspend fun getPresenceCertificate(booking: ExamBooking): Result<AppDocument> = runCatching {
        esse3Exam.getPresenceCertificate(booking)
    }
}
