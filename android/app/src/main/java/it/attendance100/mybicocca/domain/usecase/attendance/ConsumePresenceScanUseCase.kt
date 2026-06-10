package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import javax.inject.Inject

/** Clears the pending presence scan once the Presenze flow has taken it over. */
class ConsumePresenceScanUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke() = repository.consumePresenceScan()
}
