package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.tax.TaxChargeItem
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxStatusPill
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatTaxDate
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openExternalUrl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxEvent

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
) {
    val scheme = MaterialTheme.colorScheme
    val payable = invoice.status == TaxStatus.PENDING || invoice.status == TaxStatus.EXPIRED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = invoice.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                TaxStatusPill(invoice.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatEuro(invoice.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            invoice.academicYear?.let {
                Text(
                    text = "Anno accademico $it/${(it + 1) % 100}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        if (invoice.items.isNotEmpty()) {
            DetailCard {
                SectionTitle("Dettaglio")
                invoice.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    ChargeItemRow(item)
                }
            }
        }

        DetailCard {
            SectionTitle("Pagamento")
            invoice.expiration?.let { DetailRow("Scadenza", it.formatTaxDate()) }
            invoice.paymentDate?.let { DetailRow("Pagata il", it.formatTaxDate()) }
            invoice.iuv?.let { DetailRow("IUV", it) }
            invoice.noticeCode?.let { DetailRow("Codice avviso", it) }
        }

        if (actionInProgress) {
            Box(Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (payable && invoice.pagoPaImmediate) {
            Button(
                onClick = onPay,
                enabled = !actionInProgress,
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
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun ChargeItemRow(item: TaxChargeItem) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
            )
            item.installmentDescription?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = formatEuro(item.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
