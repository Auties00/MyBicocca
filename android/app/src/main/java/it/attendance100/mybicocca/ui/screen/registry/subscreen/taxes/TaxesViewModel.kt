package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetIseeDeclarationsUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaNoticeUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaReceiptUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPaymentStatusUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetTaxInvoicesUseCase
import it.attendance100.mybicocca.domain.usecase.tax.StartPagoPaPaymentUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxEvent
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// Hoisted in MainShell so the parallel fetch starts on shell load and the list / detail /
// ISEE screens all read the same in-memory result (no Room cache — see TaxRepository).
@HiltViewModel
class TaxesViewModel @Inject constructor(
    private val getTaxInvoices: GetTaxInvoicesUseCase,
    private val getIseeDeclarations: GetIseeDeclarationsUseCase,
    private val startPagoPaPayment: StartPagoPaPaymentUseCase,
    private val getPagoPaNotice: GetPagoPaNoticeUseCase,
    private val getPagoPaReceipt: GetPagoPaReceiptUseCase,
    private val getPaymentStatus: GetPaymentStatusUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _invoices = MutableStateFlow<Loadable<List<TaxInvoice>>>(Loadable.NotYetLoaded)
    val invoices: StateFlow<Loadable<List<TaxInvoice>>> = _invoices.asStateFlow()

    private val _isee = MutableStateFlow<Loadable<List<IseeDeclaration>>>(Loadable.NotYetLoaded)
    val isee: StateFlow<Loadable<List<IseeDeclaration>>> = _isee.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    private val _events = Channel<TaxEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { fetch(it) }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    fun invoice(invoiceId: InvoiceId): TaxInvoice? =
        _invoices.value.valueOrNull()?.firstOrNull { it.id == invoiceId }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            // ISEE is secondary: its failure must not blank the invoice list.
            coroutineScope {
                launch {
                    // Resolve to empty (not stuck NotYetLoaded) on failure so the ISEE
                    // screen shows its empty state rather than an endless spinner.
                    runCatching { getIseeDeclarations(careerId) }.fold(
                        onSuccess = { _isee.value = Loadable.Loaded(it) },
                        onFailure = { _isee.value = Loadable.Loaded(emptyList()) },
                    )
                }
                runCatching { getTaxInvoices(careerId) }.fold(
                    onSuccess = {
                        _invoices.value = Loadable.Loaded(it)
                        _syncStatus.value = SyncStatus.Idle
                    },
                    onFailure = { _syncStatus.value = SyncStatus.Failed(it) },
                )
            }
        } finally {
            refreshMutex.unlock()
        }
    }

    fun payInvoice(invoiceId: InvoiceId) = invoiceAction { careerId ->
        val url = startPagoPaPayment(careerId, invoiceId, PAGOPA_RETURN_URL)
        _events.send(TaxEvent.OpenUrl(url))
    }

    fun printNotice(invoiceId: InvoiceId) = invoiceAction { careerId ->
        val bytes = getPagoPaNotice(careerId, invoiceId)
        _events.send(TaxEvent.OpenPdf(bytes, "avviso_pagopa_${invoiceId.value}.pdf"))
    }

    fun printReceipt(invoiceId: InvoiceId) = invoiceAction { careerId ->
        val bytes = getPagoPaReceipt(careerId, invoiceId)
        _events.send(TaxEvent.OpenPdf(bytes, "quietanza_pagopa_${invoiceId.value}.pdf"))
    }

    fun checkPaymentStatus(invoiceId: InvoiceId) = invoiceAction { careerId ->
        val status = getPaymentStatus(careerId, invoiceId)
        _events.send(TaxEvent.ShowMessage(status.toStatusMessage()))
    }

    private fun invoiceAction(block: suspend (CareerId) -> Unit) {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch {
            _actionInProgress.value = true
            try {
                block(careerId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(TaxEvent.ShowMessage(e.message ?: "Operazione non disponibile."))
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    private companion object {
        const val PAGOPA_RETURN_URL = "https://s3w.si.unimib.it/esse3/"
    }
}

private val PAYMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)

private fun PaymentStatus?.toStatusMessage(): String {
    if (this == null) return "Nessuna transazione pagoPA trovata per questa fattura."
    val head = when (outcome) {
        PaymentOutcome.Completed -> "Pagamento eseguito"
        PaymentOutcome.Pending -> "Pagamento in corso"
        PaymentOutcome.Failed -> "Pagamento non riuscito"
        PaymentOutcome.Unknown -> description ?: "Stato del pagamento non disponibile"
    }
    val details = buildList {
        paymentDate?.let { add(it.format(PAYMENT_DATE_FORMAT)) }
        paidAmount?.takeIf { it > 0 }?.let { add("€ %.2f".format(Locale.ITALIAN, it)) }
    }
    return if (details.isEmpty()) head else "$head · ${details.joinToString(" · ")}"
}
