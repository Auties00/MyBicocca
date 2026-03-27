package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.ExamDao
import it.attendance100.mybicocca.data.datasource.exam.EasyStaffExamDataSource
import it.attendance100.mybicocca.data.datasource.exam.Esse3ExamDataSource
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepository @Inject constructor(
    private val esse3Exam: Esse3ExamDataSource,
    private val easyStaffExam: EasyStaffExamDataSource,
    private val dao: ExamDao,
) {
    fun observeExamCalls(): Flow<List<ExamCall>> = dao.observeExamCalls()

    fun observeExamCallsByCareer(careerId: Long): Flow<List<ExamCall>> =
        dao.observeExamCallsByCareer(careerId)

    fun observeBookings(): Flow<List<ExamBooking>> = dao.observeBookings()

    fun observeBookingsByCareer(careerId: Long): Flow<List<ExamBooking>> =
        dao.observeBookingsByCareer(careerId)

    suspend fun refreshExamCalls(
        careerId: Long,
        programCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<Unit> {
        val results = coroutineScope {
            listOf(
                async { runCatching { refreshBookableExams(careerId) } },
                async { runCatching { refreshScheduledExams(programCode, startDate, endDate) } },
            ).map { it.await() }
        }
        return if (results.any { it.isSuccess }) Result.success(Unit)
        else Result.failure(results.first { it.isFailure }.exceptionOrNull()!!)
    }

    suspend fun refreshBookings(careerId: Long): Result<Unit> = runCatching {
        val bookings = esse3Exam.getBookings(careerId)
        dao.deleteAllBookings()
        dao.upsertBookings(bookings)
    }

    private suspend fun refreshBookableExams(careerId: Long) {
        val calls = esse3Exam.getExamCalls(careerId)
        dao.deleteAllCalls()
        dao.upsertCalls(calls)
    }

    suspend fun getExamCallById(id: Long): ExamCall? = dao.getExamCallById(id)

    private suspend fun refreshScheduledExams(
        programCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        val calls = easyStaffExam.getScheduledExams(programCode, startDate, endDate)
        // Merge with existing calls -- EasyStaff calls have careerId=0 to distinguish
        dao.upsertCalls(calls)
    }
}
