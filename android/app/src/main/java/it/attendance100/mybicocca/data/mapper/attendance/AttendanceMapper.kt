package it.attendance100.mybicocca.data.mapper.attendance

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceRecord
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceStatus
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance

fun EasyStaffAttendanceRecord.toDomain(): ClassroomAttendance = ClassroomAttendance(
    attendancePercentage = attendancePercentage,
    lessonsAttended = lessonsAttended,
    hoursCompleted = totalHours,
    requirementProgressPercentage = requirementProgressPercentage,
    status = when (state) {
        EasyStaffAttendanceStatus.ATTENDING -> ClassroomAttendanceStatus.Attending
        EasyStaffAttendanceStatus.IN_PROGRESS -> ClassroomAttendanceStatus.InProgress
        EasyStaffAttendanceStatus.NOT_ATTENDING -> ClassroomAttendanceStatus.NotAttending
    },
)

fun ElearningAttendanceSummary.toDomain(label: String): SessionAttendance = SessionAttendance(
    label = label,
    attendedSessions = attendedSessions,
    recordedPercentage = takenSessionsPercentage,
    totalSessions = totalSessions,
    overallPercentage = allSessionsPercentage,
)
