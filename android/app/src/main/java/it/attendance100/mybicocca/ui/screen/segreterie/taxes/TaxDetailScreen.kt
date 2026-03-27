package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.ui.component.ActionBottomBar
import it.attendance100.mybicocca.ui.component.tax.Invoice
import it.attendance100.mybicocca.ui.component.tax.InvoiceData
import it.attendance100.mybicocca.ui.component.tax.PagoPaInvoice
import it.attendance100.mybicocca.ui.theme.PagoPaColor
import it.attendance100.mybicocca.ui.theme.pagoPaBackgroundColor
import it.attendance100.mybicocca.util.rememberHapticManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaxDetailScreen(
    viewModel: TaxDetailViewModel = hiltViewModel(),
) {
    val charge by viewModel.charge.collectAsStateWithLifecycle()
    val isLoading by remember { mutableStateOf(false) }
    val haptic = rememberHapticManager()

    val scrollState = rememberScrollState()
    var areBarsVisible by remember { mutableStateOf(true) }

    val primary = MaterialTheme.colorScheme.primary
    val bgPrimary = MaterialTheme.colorScheme.surfaceContainerLowest
    val bgPagoPa = pagoPaBackgroundColor()

    val currentCharge = charge
    var printButtonColor by remember { mutableStateOf(primary) }
    var printButtonBGColor by remember {
        mutableStateOf(
            if (currentCharge == null || currentCharge.status == "NON_PAGATO")
                bgPrimary
            else
                bgPagoPa,
        )
    }

    val animatedPrintButtonColor by animateColorAsState(
        targetValue = printButtonColor,
        animationSpec = tween(durationMillis = 900),
        label = "animatedPrintButtonColor",
    )
    val animatedPrintButtonBGColor by animateColorAsState(
        targetValue = printButtonBGColor,
        animationSpec = tween(durationMillis = 900),
        label = "animatedPrintButtonBGColor",
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -0.5 && scrollState.canScrollForward) {
                    areBarsVisible = false
                } else if (available.y > 0.5) {
                    areBarsVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val density = LocalDensity.current
    LaunchedEffect(scrollState, density) {
        launch {
            snapshotFlow { scrollState.canScrollForward }
                .collect { canScrollForward ->
                    if (!canScrollForward) {
                        areBarsVisible = true
                    }
                }
        }

        launch {
            val threshold = with(density) { 240.5.dp.toPx() }
            snapshotFlow { scrollState.value }
                .collect { value ->
                    if (currentCharge != null) {
                        if (scrollState.maxValue - value < threshold) {
                            printButtonColor = PagoPaColor
                            printButtonBGColor = bgPagoPa
                        } else {
                            printButtonColor = primary
                            printButtonBGColor = bgPrimary
                        }
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = (64 + 32).dp,
                    bottom = if (currentCharge == null || isPaid(currentCharge) || currentCharge.status == "IN_ATTESA") 106.dp else 166.dp
                )
                .padding(16.dp),
        ) {
            if (currentCharge != null) {
                val invoiceData = currentCharge.toInvoiceData()
                Invoice(invoiceData)
                if (currentCharge.status == "PAGATO_CONFERMATO") {
                    PagoPaInvoice()
                }
            } else {
                Text("Fattura non trovata")
            }
        }

        // Bottom bar
        if (currentCharge != null) {
            val surfaceModifier = Modifier.align(Alignment.BottomCenter)

            ActionBottomBar(
                isBottomBarVisible = areBarsVisible || !isPaid(currentCharge),
                color = if (currentCharge.status == "NON_PAGATO" || currentCharge.status == "IN_ATTESA")
                    bgPrimary
                else
                    animatedPrintButtonBGColor,
                modifier = surfaceModifier,
            ) {
                // NON PAGATO - no PagoPA
                if (currentCharge.status == "NON_PAGATO" && !currentCharge.isPagoPaEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fattura non pagabile con PagoPA",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    return@ActionBottomBar
                }

                // IN ATTESA
                if (currentCharge.status == "IN_ATTESA") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fattura in attesa di pagamento",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // NON PAGATO - with PagoPA
                if (currentCharge.status == "NON_PAGATO") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Pay with PagoPA Button
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val cornerRadius by animateIntAsState(
                            targetValue = if (isPressed) 20 else 50,
                            label = "cornerRadius",
                        )
                        Button(
                            onClick = {
                                haptic.tap()
                                /* TODO: PagoPA payment */
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PagoPaColor,
                                contentColor = Color.White,
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
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paga con")
                            Icon(
                                painter = painterResource(R.drawable.pagopa_white),
                                contentDescription = "PagoPA",
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(y = 4.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Print Notice Button
                        val interactionSource2 = remember { MutableInteractionSource() }
                        val isPressed2 by interactionSource2.collectIsPressedAsState()
                        val cornerRadius2 by animateIntAsState(
                            targetValue = if (isPressed2) 20 else 50,
                            label = "cornerRadius2",
                        )
                        Button(
                            onClick = {
                                haptic.tap()
                                /* TODO: Print */
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = PagoPaColor,
                            ),
                            enabled = !isLoading,
                            modifier = Modifier
                                .height(52.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(percent = cornerRadius2),
                            interactionSource = interactionSource2,
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stampa Avviso")
                            Icon(
                                painter = painterResource(R.drawable.pagopa_white),
                                contentDescription = "PagoPA",
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(y = 4.dp),
                            )
                        }
                    }
                }

                // PAGATO
                if (isPaid(currentCharge)) {
                    Box {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val cornerRadius by animateIntAsState(
                            targetValue = if (isPressed) 20 else 50,
                            label = "cornerRadius",
                        )
                        Button(
                            onClick = {
                                haptic.tap()
                                /* TODO: Print */
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(percent = cornerRadius),
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animatedPrintButtonColor,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stampa Quietanza di Pagamento")
                            Icon(
                                painter = painterResource(R.drawable.pagopa_white),
                                contentDescription = "PagoPA",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(y = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isPaid(charge: TaxCharge): Boolean {
    return charge.status == "PAGATO" || charge.status == "PAGATO_CONFERMATO"
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
