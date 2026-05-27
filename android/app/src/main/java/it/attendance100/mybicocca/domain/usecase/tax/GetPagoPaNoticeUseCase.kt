package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

class GetPagoPaNoticeUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId, invoiceId: InvoiceId): ByteArray =
        repository.getPagoPaNotice(careerId, invoiceId)
}
