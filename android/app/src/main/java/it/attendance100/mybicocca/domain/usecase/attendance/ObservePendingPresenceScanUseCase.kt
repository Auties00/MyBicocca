package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the raw mod_attendance link captured from an externally-opened QR scan, or `null`
 * when there is none. The Presenze flow collects it, consumes it, and runs the marking flow.
 */
class ObservePendingPresenceScanUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(): Flow<String?> = repository.observePendingPresenceScan()
}
