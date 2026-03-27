package it.attendance100.mybicocca.data.datasource.attendance

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.model.attendance.AttendanceRecord
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffAttendanceDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getAttendanceHistory(studentId: String): List<AttendanceRecord> = withContext(ioDispatcher) {
        val histories = easyStaffApi.attendance.getAttendanceHistory(studentId)
        histories.flatMap { history ->
            history.records.map { record ->
                AttendanceRecord(
                    courseCode = record.courseCode,
                    courseName = record.courseName,
                    attendancePercentage = record.attendancePercentage.toFloat(),
                    lessonsAttended = record.lessonsAttended,
                    totalHours = record.totalHours.toFloat(),
                    status = record.state.name,
                )
            }
        }
    }
}
