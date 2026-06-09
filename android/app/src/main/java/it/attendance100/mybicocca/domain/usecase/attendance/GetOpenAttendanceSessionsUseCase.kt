package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetOpenAttendanceSessionsUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(modules: List<AttendanceModuleRef>): List<OpenAttendanceSession> =
        repository.getOpenSessions(modules)
}
