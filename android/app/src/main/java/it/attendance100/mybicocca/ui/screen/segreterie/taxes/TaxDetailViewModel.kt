package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.tax.Invoice
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.TaxRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaxDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taxRepository: TaxRepository,
    careerRepository: CareerRepository,
) : ViewModel() {

    private val chargeId: Long = savedStateHandle["chargeId"] ?: 0L

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val charge: StateFlow<TaxCharge?> = activeCareer
        .flatMapLatest { career ->
            career?.let {
                taxRepository.observeCharges(it.studentId)
                    .map { charges -> charges.firstOrNull { item -> item.id == chargeId } }
            } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val invoice: StateFlow<Invoice?> = activeCareer
        .flatMapLatest { career ->
            career?.let {
                taxRepository.observeInvoices(it.studentId)
                    .map { invoices -> invoices.firstOrNull { item -> item.id == chargeId } }
            } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isActionInProgress = MutableStateFlow(false)
    val isActionInProgress: StateFlow<Boolean> = _isActionInProgress.asStateFlow()

    private val _events = MutableSharedFlow<it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent>()
    val events: SharedFlow<it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent> =
        _events.asSharedFlow()

    fun startPagoPaPayment() {
        withInvoiceAction { _, invoice ->
            val url = taxRepository.startPagoPaTransaction(
                invoiceId = invoice.id,
                returnUrl = PAGOPA_RETURN_URL,
            ).getOrThrow()
            if (url.isBlank()) error("Link di pagamento non disponibile")
            _events.emit(
                it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.OpenUrl(url)
            )
        }
    }

    fun printNotice() {
        withInvoiceAction { _, invoice ->
            val document = taxRepository.getPagoPaNotice(invoice.id).getOrThrow()
            _events.emit(
                it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.OpenDocument(
                    document
                )
            )
        }
    }

    fun printReceipt() {
        withInvoiceAction { _, invoice ->
            val document = taxRepository.getPagoPaReceipt(invoice.id).getOrThrow()
            _events.emit(
                it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.OpenDocument(
                    document
                )
            )
        }
    }

    private fun withInvoiceAction(block: suspend (Career, Invoice) -> Unit) {
        viewModelScope.launch {
            val career = activeCareer.value
            val currentInvoice = invoice.value
            if (career == null || currentInvoice == null) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        "Fattura non disponibile"
                    )
                )
                return@launch
            }

            _isActionInProgress.value = true
            try {
                block(career, currentInvoice)
            } catch (e: Exception) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        e.message ?: "Operazione non disponibile"
                    )
                )
            } finally {
                _isActionInProgress.value = false
            }
        }
    }

    private companion object {
        const val PAGOPA_RETURN_URL = "https://s3w.si.unimib.it/esse3/"
    }
}
