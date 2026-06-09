package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

class MarkOpenSessionUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(
        session: OpenAttendanceSession,
        statusId: String?,
        password: String?,
    ): PresenceMarkOutcome =
        repository.markSession(session.module, session.sessionId, statusId, password)
}
