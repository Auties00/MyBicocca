package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

class GetTaxSummaryUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId): TaxSummary =
        repository.getSummary(careerId)
}
