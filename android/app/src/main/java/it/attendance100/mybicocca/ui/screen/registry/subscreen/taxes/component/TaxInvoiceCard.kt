package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatTaxDate
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent

/**
 * One fattura in the Tasse list: a ticket-styled card (sliced DynamicCard, brand stroke) with
 * the invoice number and amount on the header row, the title beneath, and the expiry date
 * opposite a color-coded status line (success green = paid, error red = due or expired,
 * muted = cancelled).
 */
@Composable
fun TaxInvoiceCard(
    invoice: TaxInvoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val taxId = invoice.id.value
    val haptic = rememberHapticManager()
    DynamicCard(
        topSliceRes = R.drawable.border_outer,
        midSliceRes = R.drawable.body_outer,
        bottomSliceRes = R.drawable.border_outer,
        topSliceRes2 = R.drawable.border_inner,
        midSliceRes2 = R.drawable.body_inner,
        bottomSliceRes2 = R.drawable.border_inner,
        sliceTopHeightDp = 10.dp,
        sliceBottomHeightDp = 10.dp,
        fill = scheme.surfaceContainerLowest,
        stroke = scheme.primary,
        onClick = { haptic.tap(); onClick() },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.taxes_invoice_number, taxId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = formatCurrency(invoice.amount, currentLocale()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = invoice.title,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.taxes_deadline),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = invoice.expiration?.formatTaxDate() ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = invoice.statusLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = invoice.statusColor(registryAccent(RegistryAccent.Success), scheme.error, scheme.onSurfaceVariant),
                )
            }
        }
    }
}

@Composable
private fun TaxInvoice.statusLabel(): String = when (status) {
    TaxStatus.PAID -> paymentDate?.let { "Pagata il ${it.formatTaxDate()}" } ?: "Pagata"
    TaxStatus.PENDING -> "Da pagare"
    TaxStatus.EXPIRED -> "Scaduta"
    TaxStatus.CANCELED -> "Annullata"
}

private fun TaxInvoice.statusColor(success: Color, error: Color, muted: Color): Color = when (status) {
    TaxStatus.PAID -> success
    TaxStatus.PENDING, TaxStatus.EXPIRED -> error
    TaxStatus.CANCELED -> muted
}
