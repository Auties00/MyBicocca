package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.InvoiceData
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.InvoiceItem
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double, locale: Locale): String {
    return NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance("EUR")
    }.format(amount)
}

private fun formatFullDate(date: LocalDate, locale: Locale): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    return date.format(formatter)
}

val DividerColor = Color(0xFF555555)

/**
 * A fattura rendered as a paper receipt: a double-bordered ticket card (sliced drawables via
 * DynamicCard, Bicocca wordmark stroke) headed by the university logo and the paid date — or
 * an oversized brand-red "Non pagata" — with dashed tear lines separating the notice/RPT/
 * expiry/total info section from the per-item tax breakdown.
 */
@Composable
fun Invoice(
    invoice: InvoiceData,
    modifier: Modifier = Modifier,
) {
    DynamicCard(
        topSliceRes = R.drawable.border_outer,
        midSliceRes = R.drawable.body_outer,
        bottomSliceRes = R.drawable.border_outer,
        topSliceRes2 = R.drawable.border_inner,
        midSliceRes2 = R.drawable.body_inner,
        bottomSliceRes2 = R.drawable.border_inner,
        fill = MaterialTheme.colorScheme.surfaceContainerLowest,
        stroke = BicoccaWordmarkAccent,
        sliceTopHeightDp = 10.dp,
        sliceBottomHeightDp = 10.dp,
        modifier = modifier,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                TicketHeader(invoice)

                DashedDivider()

                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(stringResource(R.string.taxes_section_info))

                InfoRow(label = stringResource(R.string.taxes_bulletin_code), value = invoice.bulletinCode ?: "-", verticalPadding = 6.dp)
                InfoRow(label = stringResource(R.string.taxes_modality), value = invoice.modalita, verticalPadding = 6.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        Text(
                            text = stringResource(R.string.taxes_date_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.taxes_deadline),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = formatFullDate(
                            invoice.expiryDate,
                            currentLocale()
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        textAlign = TextAlign.End,
                    )
                }

                InfoRow(
                    label = stringResource(R.string.taxes_total_amount),
                    value = formatCurrency(invoice.amount, currentLocale()),
                    isLarge = true,
                    verticalPadding = 6.dp,
                )

                Spacer(modifier = Modifier.height(24.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Tasse")

                Text(
                    text = invoice.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))
                DividerLine()

                invoice.items.forEach { item ->
                    TaxItemRow(item)
                    if (invoice.items.last() != item) DividerLine()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TicketHeader(invoice: InvoiceData) {
    val isDarkMode = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.size(67.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.logo_simple),
                contentDescription = stringResource(R.string.common_logo),
                colorFilter = if (isDarkMode) ColorFilter.tint(Color.White) else null,
                modifier = Modifier
                    .fillMaxSize()
                    .size(67.dp),
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (invoice.paymentDate != null) {
                    stringResource(
                        R.string.taxes_paid_on,
                        formatFullDate(
                            invoice.paymentDate,
                            currentLocale()
                        )
                    )
                } else {
                    stringResource(R.string.taxes_unpaid)
                },
                fontSize = if (invoice.paymentDate != null) 12.sp else 40.sp,
                color = if (invoice.paymentDate != null) Color.Unspecified else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                fontWeight = if (invoice.paymentDate != null) FontWeight.Normal else FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.taxes_invoice_label, invoice.invoiceNumber),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
private fun InfoRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isItalic: Boolean = false,
    isLarge: Boolean = false,
    verticalPadding: Dp = 12.dp,
    valueModifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = valueModifier,
            color = if (isLarge) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = if (isLarge) 20.sp else 14.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun TaxItemRow(item: InvoiceItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        InfoRow(label = "Voce", value = item.description, isItalic = true, verticalPadding = 0.dp)
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Rata", value = item.installment, verticalPadding = 0.dp)
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Anno", value = item.year, verticalPadding = 0.dp)
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(
            label = "Importo",
            value = formatCurrency(item.amount, currentLocale()),
            isLarge = true,
            verticalPadding = 0.dp
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 0.5.dp, color = DividerColor)
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = DividerColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = 2.dp.toPx(),
        )
    }
}
