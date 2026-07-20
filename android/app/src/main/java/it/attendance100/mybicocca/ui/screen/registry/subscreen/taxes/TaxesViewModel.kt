package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.R
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Backs the whole tax feature: the Tasse list and detail plus the ISEE sheet. Hoisted in
 * MainShell so the parallel fetch starts on shell load and every consumer reads the same
 * in-memory result (no Room cache — see `TaxRepository`).
 *
 * [invoices] and [isee] are independent [Loadable] snapshots, [syncStatus] tracks the invoice
 * fetch, [actionInProgress] gates the pagoPA actions and [events] emits their one-shot
 * outcomes. [refresh] re-fetches both lists; [payInvoice], [printNotice], [printReceipt] and
 * [checkPaymentStatus] run the pagoPA flows for one fattura; [invoice] looks one up in the
 * current snapshot.
 */
@HiltViewModel
class TaxesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
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

    /**
     * ISEE declarations are secondary to the invoices: a failed fetch resolves to an empty
     * list (not a stuck [Loadable.NotYetLoaded]) so the ISEE sheet shows its empty state
     * rather than an endless spinner, and never blanks the invoice list.
     */
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
            coroutineScope {
                launch {
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
        _events.send(TaxEvent.ShowMessage(status.toStatusMessage(appContext)))
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

private fun PaymentStatus?.toStatusMessage(context: Context): String {
    if (this == null) return context.getString(R.string.taxes_no_pagopa_transaction)
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val paymentDateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    val head = when (outcome) {
        PaymentOutcome.Completed -> context.getString(R.string.taxes_payment_completed)
        PaymentOutcome.Pending -> context.getString(R.string.taxes_payment_pending)
        PaymentOutcome.Failed -> context.getString(R.string.taxes_payment_failed)
        PaymentOutcome.Unknown -> description ?: context.getString(R.string.taxes_payment_status_unavailable)
    }
    val details = buildList {
        paymentDate?.let { add(it.format(paymentDateFormat)) }
        paidAmount?.takeIf { it > 0 }?.let { add("€ %.2f".format(locale, it)) }
    }
    return if (details.isEmpty()) head else "$head · ${details.joinToString(" · ")}"
}
