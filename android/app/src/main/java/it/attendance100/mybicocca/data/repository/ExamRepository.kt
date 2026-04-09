package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.ExamDao
import it.attendance100.mybicocca.data.datasource.exam.EasyStaffExamDataSource
import it.attendance100.mybicocca.data.datasource.exam.Esse3ExamDataSource
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    suspend fun refreshExamCalls(
        careerId: Long,
        programCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        matricolaId: Long? = null,
    ): Result<Unit> {
        val results = coroutineScope {
            listOf(
                async { runCatching { refreshBookableExams(careerId, matricolaId) } },
                async { runCatching { refreshScheduledExams(programCode, startDate, endDate) } },
            ).awaitAll()
        }
        return if (results.any { it.isSuccess }) Result.success(Unit)
        else Result.failure(results.first { it.isFailure }.exceptionOrNull()!!)
    }

    suspend fun refreshBookings(matricolaId: Long?): Result<Unit> = runCatching {
        val bookings = esse3Exam.getBookings(matricolaId)
        dao.deleteAllBookings()
        dao.upsertBookings(bookings)
    }

    private suspend fun refreshBookableExams(careerId: Long, matricolaId: Long? = null) {
        val calls = esse3Exam.getExamCalls(careerId, matricolaId)
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
