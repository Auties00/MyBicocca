package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Loads the student's fee refunds ("rimborsi") when the registry "Rimborsi" sub-screen opens
 * or is refreshed. Fetches live from Esse3.
 */
class GetRefundsUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<Refund> =
        repository.getRefunds(careerId)
}
