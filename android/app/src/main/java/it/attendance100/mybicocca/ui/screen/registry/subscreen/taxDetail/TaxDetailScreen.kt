package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.Invoice
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.InvoiceData
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.InvoiceItem
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.PagoPaInvoice
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openExternalUrl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme.PagoPaColor
import java.time.LocalDate

@Composable
fun TaxDetailScreen(
    chargeId: Long,
    viewModel: TaxesViewModel,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val actionInProgress by viewModel.actionInProgress.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaxEvent.OpenUrl -> openExternalUrl(context, event.url)
                is TaxEvent.OpenPdf -> runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                    .onFailure { snackbar.showError("Impossibile aprire il documento.") }
                is TaxEvent.ShowMessage -> snackbar.showInfo(event.message)
            }
        }
    }

    val invoice = (invoicesState as? Loadable.Loaded)?.value?.firstOrNull { it.id.value == chargeId }
    when {
        invoice != null -> TaxDetailContent(
            invoice = invoice,
            actionInProgress = actionInProgress,
            onPay = { viewModel.payInvoice(invoice.id) },
            onPrintNotice = { viewModel.printNotice(invoice.id) },
            onPrintReceipt = { viewModel.printReceipt(invoice.id) },
            onCheckStatus = { viewModel.checkPaymentStatus(invoice.id) },
        )

        invoicesState is Loadable.NotYetLoaded ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

        else -> EmptyState(
            icon = Icons.Outlined.ReceiptLong,
            title = "Fattura non trovata",
            body = "Questa fattura non è più disponibile.",
        )
    }
}

@Composable
private fun TaxDetailContent(
    invoice: TaxInvoice,
    actionInProgress: Boolean,
    onPay: () -> Unit,
    onPrintNotice: () -> Unit,
    onPrintReceipt: () -> Unit,
    onCheckStatus: () -> Unit,
) {
    val payable = invoice.status == TaxStatus.PENDING || invoice.status == TaxStatus.EXPIRED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Invoice(invoice.toInvoiceData(), sharedTaxId = invoice.id.value)

        if (invoice.status == TaxStatus.PAID) {
            PagoPaInvoice()
        }

        if (actionInProgress) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (payable && invoice.pagoPaImmediate) {
            Button(
                onClick = onPay,
                enabled = !actionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = PagoPaColor, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Paga con pagoPA") }
        }
        if (payable && invoice.pagoPaNotice) {
            FilledTonalButton(
                onClick = onPrintNotice,
                enabled = !actionInProgress,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Stampa avviso pagoPA") }
        }
        if (invoice.status == TaxStatus.PAID && invoice.pagoPaEnabled) {
            FilledTonalButton(
                onClick = onPrintReceipt,
                enabled = !actionInProgress,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Stampa quietanza") }
        }
        if (invoice.pagoPaEnabled) {
            FilledTonalButton(
                onClick = onCheckStatus,
                enabled = !actionInProgress,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Verifica stato pagamento") }
        }
    }
}

private fun TaxInvoice.toInvoiceData(): InvoiceData = InvoiceData(
    id = id.value.toString(),
    invoiceNumber = id.value.toString(),
    description = title,
    expiryDate = expiration ?: LocalDate.now(),
    amount = amount,
    modalita = "Pagamento tramite pagoPA",
    bulletinCode = noticeCode,
    items = items.map { item ->
        InvoiceItem(
            year = academicYear?.let { "$it/${(it + 1) % 100}" } ?: "-",
            installment = item.installmentDescription ?: "-",
            description = item.description,
            amount = item.amount,
        )
    },
    paymentDate = paymentDate,
    rptStatus = iuv,
)
