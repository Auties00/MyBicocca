package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

class GetIseeDeclarationsUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<IseeDeclaration> =
        repository.getIseeDeclarations(careerId)
}
