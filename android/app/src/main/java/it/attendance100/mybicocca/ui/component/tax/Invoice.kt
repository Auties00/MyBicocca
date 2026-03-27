package it.attendance100.mybicocca.ui.component.tax

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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.ITALY).format(amount)
}

private fun formatFullDate(date: LocalDate, locale: Locale): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    return date.format(formatter)
}

val DividerColor = Color(0xFF555555)


data class InvoiceData(
    val id: String,
    val invoiceNumber: String,
    val description: String,
    val expiryDate: LocalDate,
    val amount: Double,
    val modalita: String,
    val bulletinCode: String? = null,
    val items: List<InvoiceItem> = emptyList(),
    val paymentDate: LocalDate? = null,
    val rptStatus: String? = null,
)

data class InvoiceItem(
    val year: String,
    val installment: String,
    val description: String,
    val amount: Double,
)


@Composable
fun Invoice(
    invoice: InvoiceData,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()

    DynamicCard(
        R.drawable.border_outer,
        R.drawable.body_outer,
        R.drawable.border_outer,
        R.drawable.border_inner,
        R.drawable.body_inner,
        R.drawable.border_inner,
        fill = MaterialTheme.colorScheme.surfaceContainerLowest,
        stroke = MaterialTheme.colorScheme.primary,
        sliceTopHeightDp = 10.dp,
        sliceBottomHeightDp = 10.dp,
        modifier = modifier,
        onClick = {
            haptic.tap()
        }
    ) {
        // Actual Content Overlay
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                TicketHeader(invoice)

                DashedDivider()

                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Informazioni")

                InfoRow(
                    label = "Codice Avviso",
                    value = invoice.bulletinCode ?: "-",
                    verticalPadding = 6.dp
                )
                InfoRow(
                    label = "Stato RPT",
                    value = invoice.rptStatus ?: "-",
                    verticalPadding = 6.dp
                )
                InfoRow(
                    label = "Modalità",
                    value = invoice.modalita,
                    verticalPadding = 6.dp
                )

                // Expiry Date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        Text(
                            text = "Data ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "Scadenza",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = formatFullDate(
                            invoice.expiryDate,
                            Locale.getDefault()
                        ),
                        color = GrayColor(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        textAlign = TextAlign.End,
                    )
                }

                // Amount
                InfoRow(
                    label = "Importo Totale",
                    value = formatCurrency(invoice.amount),
                    isLarge = true,
                    verticalPadding = 6.dp,
                )

                Spacer(modifier = Modifier.height(24.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Taxes")

                // Description
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
fun TicketHeader(invoice: InvoiceData) {
    val preferencesManager = rememberPreferencesManager()
    val isDarkMode = preferencesManager.isDarkMode ?: isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier.size(67.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_simple),
                contentDescription = "Logo",
                colorFilter = if (isDarkMode) ColorFilter.tint(Color.White) else null,
                modifier = Modifier
                    .fillMaxSize()
                    .size(67.dp),
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            // Status
            Text(
                text = if (invoice.paymentDate != null) "Pagata il ${
                    formatFullDate(
                        invoice.paymentDate,
                        Locale.getDefault()
                    )
                }" else "Non pagata",
                fontSize = if (invoice.paymentDate != null) 12.sp else 40.sp,
                color = if (invoice.paymentDate != null) Color.Unspecified else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                fontWeight = if (invoice.paymentDate != null) FontWeight.Normal else FontWeight.Black,
                fontFamily = if (invoice.paymentDate != null) null else FontFamily(Font(R.font.roboto_flex_variable)),
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Invoice Number
            Text(
                text = "Fattura ${invoice.invoiceNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
    }
}


@Composable
fun InfoRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isItalic: Boolean = false,
    isLarge: Boolean = false,
    verticalPadding: Dp = 12.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            color = if (isLarge) Color.Unspecified else GrayColor(),
            fontSize = if (isLarge) 20.sp else 14.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textAlign = TextAlign.End,
        )
    }
}


@Composable
fun TaxItemRow(item: InvoiceItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        // Voce
        InfoRow(
            label = "Voce",
            value = item.description,
            isItalic = true,
            verticalPadding = 0.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rata
        InfoRow(
            label = "Rata",
            value = item.installment,
            verticalPadding = 0.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Anno
        InfoRow(
            label = "Anno",
            value = item.year,
            verticalPadding = 0.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Importo
        InfoRow(
            label = "Importo",
            value = formatCurrency(item.amount),
            isLarge = true,
            verticalPadding = 0.dp,
        )
    }
}


@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}


@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 0.5.dp, color = DividerColor)
}


@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
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
