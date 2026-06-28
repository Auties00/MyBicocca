package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.subscreen.taxDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.Invoice
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.PagoPaInvoice
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openExternalUrl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.InvoiceData
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.InvoiceItem
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.theme.PagoPaColor
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Full-screen hero detail: the tapped fattura grows from its row in the Tasse modal ([origin]
 * = its bounds in absolute SCREEN px) to a true edge-to-edge page, over the modal and the
 * status bar. Hosted in its own full-screen Dialog window (decorFitsSystemWindows = false ⇒
 * anchored at screen 0,0, with the window's actual on-screen offset still captured in case an
 * OEM insets it), so the screen-space origin maps straight into the layer. One progress
 * spring drives a graphicsLayer container transform — the full-size page scaled + translated
 * down onto the row, anchored top-left — read in the draw phase so it stays smooth, plus a
 * scrim fade and a content fade held until the page has visibly started expanding, so the
 * early tiny-scaled frames read as the row rather than a shrunken page. Back / ✕ reverse the
 * morph before dismissing.
 *
 * The rendered fattura is re-derived live from the invoice list by id, so a background
 * refresh updates the open detail in place (the opening snapshot is the fallback if it gets
 * evicted). pagoPA action outcomes replace the detail with a result page until dismissed.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaxDetailOverlay(
    invoice: TaxInvoice,
    origin: Rect,
    viewModel: TaxesViewModel,
    onClose: () -> Unit,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val actionInProgress by viewModel.actionInProgress.collectAsStateWithLifecycle()
    val live = (invoicesState as? Loadable.Loaded)?.value?.firstOrNull { it.id == invoice.id } ?: invoice

    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }

    fun dismiss() {
        if (closing) return
        closing = true
        scope.launch {
            progress.animateTo(0f, spec)
            onClose()
        }
    }

    LaunchedEffect(Unit) { progress.animateTo(1f, spec) }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box {
            val context = LocalContext.current
            var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        is TaxEvent.OpenUrl -> openExternalUrl(context, event.url)
                        is TaxEvent.OpenPdf -> runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                            .onFailure { outcome = SheetOutcome.Error("Impossibile aprire il documento") }
                        is TaxEvent.ShowMessage -> outcome = SheetOutcome.Info(event.message)
                    }
                }
            }

            val seekableState =
                remember { androidx.compose.animation.core.SeekableTransitionState(outcome) }
            val transition = androidx.compose.animation.core.rememberTransition(
                seekableState,
                label = "tax_detail_result"
            )

            LaunchedEffect(outcome) {
                if (seekableState.targetState != outcome) {
                    seekableState.animateTo(outcome)
                }
            }

            androidx.activity.compose.PredictiveBackHandler(enabled = outcome != null) { progress ->
                try {
                    progress.collect { event ->
                        seekableState.seekTo(event.progress, targetState = null)
                    }
                    seekableState.animateTo(null)
                    outcome = null
                } catch (_: kotlinx.coroutines.CancellationException) {
                    seekableState.animateTo(outcome)
                }
            }

            val view = LocalView.current
            var windowOffset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        val loc = IntArray(2)
                        view.getLocationOnScreen(loc)
                        windowOffset = Offset(loc[0].toFloat(), loc[1].toFloat())
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.5f * progress.value.coerceIn(0f, 1f) }
                        .background(Color.Black),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = progress.value.coerceIn(0f, 1f)
                            val ox = origin.left - windowOffset.x
                            val oy = origin.top - windowOffset.y
                            val sx = if (size.width > 0f) origin.width / size.width else 1f
                            val sy = if (size.height > 0f) origin.height / size.height else 1f
                            scaleX = sx + (1f - sx) * p
                            scaleY = sy + (1f - sy) * p
                            transformOrigin = TransformOrigin(0f, 0f)
                            translationX = ox * (1f - p)
                            translationY = oy * (1f - p)
                            shadowElevation = 18.dp.toPx() * p
                            shape = RectangleShape
                            clip = true
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    val contentModifier = Modifier.graphicsLayer {
                        alpha = ((progress.value - 0.15f) / 0.85f).coerceIn(0f, 1f)
                    }
                    transition.AnimatedContent(contentKey = { it != null }) { current ->
                        if (current != null) {
                            TaxResultContent(
                                outcome = current,
                                onBack = { outcome = null },
                                modifier = contentModifier,
                            )
                        } else {
                            DetailContent(
                                invoice = live,
                                actionInProgress = actionInProgress,
                                onClose = { dismiss() },
                                onPay = { viewModel.payInvoice(live.id) },
                                onPrintNotice = { viewModel.printNotice(live.id) },
                                onPrintReceipt = { viewModel.printReceipt(live.id) },
                                onCheckStatus = { viewModel.checkPaymentStatus(live.id) },
                                modifier = contentModifier,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The detail page proper: a close bar with the fattura number over the receipt ticket, the
 * pagoPA success receipt when paid, and the pagoPA actions — pay, print notice, print
 * receipt, check payment status — each gated by the invoice's status and pagoPA capabilities.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DetailContent(
    invoice: TaxInvoice,
    actionInProgress: Boolean,
    onClose: () -> Unit,
    onPay: () -> Unit,
    onPrintNotice: () -> Unit,
    onPrintReceipt: () -> Unit,
    onCheckStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payable = invoice.status == TaxStatus.PENDING || invoice.status == TaxStatus.EXPIRED
    val isOnline = LocalIsOnline.current
    val haptic = rememberHapticManager()

    Column(
        modifier = modifier
            .testTag(TaxesTestTags.DETAIL_ROOT)
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { haptic.tap(); onClose() },
                modifier = Modifier.testTag(TaxesTestTags.DETAIL_CLOSE_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.common_close)
                )
            }
            Text(
                text = stringResource(R.string.taxes_invoice_number, invoice.id.value),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Invoice(invoice.toInvoiceData())

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
                    onClick = { haptic.tap(); onPay() },
                    enabled = !actionInProgress && isOnline,
                    colors = ButtonDefaults.buttonColors(containerColor = PagoPaColor, contentColor = Color.White),
                    modifier = Modifier
                        .testTag(TaxesTestTags.DETAIL_PAY_BUTTON)
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text(stringResource(R.string.taxes_pay_pagopa)) }
            }
            if (payable && invoice.pagoPaNotice) {
                FilledTonalButton(
                    onClick = { haptic.tap(); onPrintNotice() },
                    enabled = !actionInProgress && isOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text(stringResource(R.string.taxes_print_notice)) }
            }
            if (invoice.status == TaxStatus.PAID && invoice.pagoPaEnabled) {
                FilledTonalButton(
                    onClick = { haptic.tap(); onPrintReceipt() },
                    enabled = !actionInProgress && isOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text(stringResource(R.string.taxes_print_receipt)) }
            }
            if (invoice.pagoPaEnabled) {
                FilledTonalButton(
                    onClick = { haptic.tap(); onCheckStatus() },
                    enabled = !actionInProgress,
                    modifier = Modifier
                        .testTag(TaxesTestTags.DETAIL_CHECK_STATUS_BUTTON)
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text(stringResource(R.string.taxes_check_status)) }
            }
        }
    }
}

/**
 * Full-screen result view shown over the fattura detail after a pagoPA action: a back bar
 * above the shared result page. Back / the page button return to the detail, not close the
 * overlay.
 */
@Composable
private fun TaxResultContent(
    outcome: SheetOutcome,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { haptic.tap(); onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.common_back)
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SheetResultPage(outcome = outcome, onDismiss = onBack)
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
)
