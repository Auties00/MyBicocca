package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

/**
 * Registers the student's presence from a classified scan on the "Presenze" sub-screen:
 * session links self-mark the Moodle mod_attendance session, lesson codes certify through
 * EasyStaff EasyBadge. Writes the presence on the corresponding backend and reports the
 * outcome to render.
 */
class MarkPresenceUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(scan: PresenceScan, careerId: CareerId): PresenceMarkOutcome =
        repository.registerPresence(scan, careerId)
}
