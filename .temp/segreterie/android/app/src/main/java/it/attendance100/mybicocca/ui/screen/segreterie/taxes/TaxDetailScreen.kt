package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.ui.component.ActionBottomBar
import it.attendance100.mybicocca.ui.component.tax.Invoice
import it.attendance100.mybicocca.ui.component.tax.InvoiceData
import it.attendance100.mybicocca.ui.component.tax.PagoPaInvoice
import it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEventEffect
import it.attendance100.mybicocca.ui.theme.PagoPaColor
import it.attendance100.mybicocca.util.rememberHapticManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaxDetailScreen(
    viewModel: TaxDetailViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val charge by viewModel.charge.collectAsStateWithLifecycle()
    val invoice by viewModel.invoice.collectAsStateWithLifecycle()
    val isLoading by viewModel.isActionInProgress.collectAsStateWithLifecycle()
    val haptic = rememberHapticManager()

    SegreterieActionEventEffect(viewModel.events)

    val currentCharge = charge
    val currentInvoice = invoice
    val canPayNow = currentCharge != null &&
            (currentCharge.status == "PENDING" || currentCharge.status == "EXPIRED") &&
            currentInvoice?.isPagoPaImmediate == true
    val canPrintNotice = currentCharge != null &&
            (currentCharge.status == "PENDING" || currentCharge.status == "EXPIRED") &&
            currentInvoice?.isPagoPaNotice == true
    val canPrintReceipt = currentCharge != null &&
            currentCharge.status == "PAID" &&
            currentInvoice?.isPagoPaEnabled == true
    val showUnavailableMessage = currentCharge != null &&
            (currentCharge.status == "PENDING" || currentCharge.status == "EXPIRED") &&
            !canPayNow &&
            !canPrintNotice

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = (64 + 32).dp, bottom = if (canPayNow || canPrintNotice || canPrintReceipt || showUnavailableMessage) 170.dp else 24.dp)
                .padding(16.dp),
        ) {
            if (currentCharge != null) {
                Invoice(currentCharge.toInvoiceData())
                if (currentCharge.status == "PAID") {
                    Spacer(modifier = Modifier.height(12.dp))
                    PagoPaInvoice()
                }
            } else {
                Text("Fattura non trovata")
            }
        }

        if (currentCharge != null && currentCharge.status != "CANCELED" &&
            (canPayNow || canPrintNotice || canPrintReceipt || showUnavailableMessage)
        ) {
            ActionBottomBar(
                isBottomBarVisible = true,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                when {
                    showUnavailableMessage -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Nessuna azione disponibile per questa fattura",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    canPrintReceipt -> {
                        PagoPaActionButton(
                            text = "Stampa Quietanza di Pagamento",
                            isLoading = isLoading,
                            primary = true,
                            onClick = {
                                haptic.tap()
                                viewModel.printReceipt()
                            },
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (canPayNow) {
                                PagoPaActionButton(
                                    text = "Paga con",
                                    isLoading = isLoading,
                                    primary = true,
                                    onClick = {
                                        haptic.tap()
                                        viewModel.startPagoPaPayment()
                                    },
                                )
                            }

                            if (canPrintNotice) {
                                PagoPaActionButton(
                                    text = "Stampa Avviso",
                                    isLoading = isLoading,
                                    primary = false,
                                    onClick = {
                                        haptic.tap()
                                        viewModel.printNotice()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PagoPaActionButton(
    text: String,
    isLoading: Boolean,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = MutableInteractionSource()
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        label = "cornerRadius",
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) PagoPaColor else Color.White,
            contentColor = if (primary) Color.White else PagoPaColor,
        ),
        enabled = !isLoading,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(percent = cornerRadius),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(16.dp),
    ) {
        Icon(
            imageVector = if (primary && text.startsWith("Paga")) Icons.Default.AccountBalance
            else Icons.Default.Print,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
        Icon(
            painter = painterResource(R.drawable.pagopa_white),
            contentDescription = "PagoPA",
            tint = if (primary) Color.White else PagoPaColor,
            modifier = Modifier
                .size(40.dp)
                .offset(y = 4.dp),
        )
    }
}

private fun TaxCharge.toInvoiceData(): InvoiceData {
    val parsedDueDate = dueDate?.let {
        runCatching {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()
    } ?: LocalDate.now()

    val parsedPaymentDate = paymentDate?.let {
        runCatching {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()
    }

    return InvoiceData(
        id = id.toString(),
        invoiceNumber = invoiceNumber ?: id.toString(),
        description = description,
        expiryDate = parsedDueDate,
        amount = amount,
        modalita = modalita ?: "Pagamento tramite pagoPA",
        bulletinCode = bulletinCode,
        items = emptyList(),
        paymentDate = parsedPaymentDate,
        rptStatus = rptStatus,
    )
}
