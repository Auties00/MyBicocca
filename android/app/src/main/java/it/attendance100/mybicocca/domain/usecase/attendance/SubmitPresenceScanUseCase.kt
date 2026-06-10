package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import javax.inject.Inject

/**
 * Hands a mod_attendance link opened from outside the app to the Presenze flow. Invoked by the
 * activity when an incoming intent targets elearning.unimib.it's attendance.php.
 */
class SubmitPresenceScanUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(rawLink: String) = repository.submitPresenceScan(rawLink)
}
