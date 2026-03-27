package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.AttendanceDao
import it.attendance100.mybicocca.data.datasource.attendance.EasyStaffAttendanceDataSource
import it.attendance100.mybicocca.data.model.attendance.AttendanceRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val dataSource: EasyStaffAttendanceDataSource,
    private val dao: AttendanceDao,
) {
    fun observeAll(): Flow<List<AttendanceRecord>> = dao.observeAll()

    fun observeByCourse(courseCode: String): Flow<List<AttendanceRecord>> =
        dao.observeByCourse(courseCode)

    suspend fun refresh(studentId: String): Result<Unit> = runCatching {
        val records = dataSource.getAttendanceHistory(studentId)
        dao.deleteAll()
        dao.insertAll(records)
    }
}
