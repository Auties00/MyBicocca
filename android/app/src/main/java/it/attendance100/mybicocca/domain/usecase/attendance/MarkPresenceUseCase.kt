package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

class MarkPresenceUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(scan: PresenceScan, careerId: CareerId): PresenceMarkOutcome =
        repository.registerPresence(scan, careerId)
}
