package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxInvoiceCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.groupByFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.subscreen.taxDetail.TaxDetailOverlay
import kotlinx.coroutines.launch

// Tasse as a modal sheet, same modal language as ISEE / Rimborsi: a pinned header over a body
// that splits into Da pagare / Pagate / Annullate via a swipeable pager driven by a footer
// segmented switch (the CFU/Voti pattern). Reads the hoisted TaxesViewModel (the same in-memory
// fetch as the rest of the tax feature), so re-opening never re-hits the network. Tapping a
// fattura expands it from its row to a full-screen hero detail (TaxDetailOverlay).
// Tasse as a single sheet entry. The container and the pinned "Tasse" header are owned by
// BottomSheetSceneStrategy; this renders the Da pagare / Pagate pager plus the full-screen hero
// detail dialog (TaxDetailOverlay) that a fattura row expands into.
@Composable
fun TaxesPage(
    viewModel: TaxesViewModel,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // The VM outlives the sheet (it is shell-scoped): a re-open shows the cached snapshot
    // instantly while this kicks a background refresh.
    LaunchedEffect(viewModel) {
        if (viewModel.invoices.value is Loadable.Loaded) viewModel.refresh()
    }

    val allInvoices = invoicesState.valueOrNull()
    val grouped = remember(allInvoices) { allInvoices?.groupByFilter() }

    // The fattura whose hero detail is expanded, with the row bounds it grows from.
    var expanded by remember { mutableStateOf<Pair<TaxInvoice, Rect>?>(null) }

    TaxesBody(
        grouped = grouped,
        syncStatus = syncStatus,
        onRetry = viewModel::refresh,
        onOpenDetail = { invoice, rect -> expanded = invoice to rect },
    )

    expanded?.let { (invoice, rect) ->
        TaxDetailOverlay(
            invoice = invoice,
            origin = rect,
            viewModel = viewModel,
            onClose = { expanded = null },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TaxesBody(
    grouped: Map<TaxFilter, List<TaxInvoice>>?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenDetail: (TaxInvoice, Rect) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = grouped == null)
    val settled = grouped != null && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet only grows/shrinks vertically as content lands — animate the height change
    // here instead of letting the modal snap to the new size.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && grouped == null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = failure.cause.taxFriendlyMessage(),
                action = { RetryButton(onClick = onRetry) },
            )

            grouped == null || !settled -> SheetLoadingIndicator(label = "Caricamento tasse…")

            grouped.values.all { it.isEmpty() } -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp),
            ) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = "Nessuna tassa",
                    body = "Non risultano fatture per la tua carriera.",
                )
            }

            else -> TaxesPager(grouped = grouped, onOpenDetail = onOpenDetail)
        }
    }
}

@Composable
private fun TaxesPager(
    grouped: Map<TaxFilter, List<TaxInvoice>>,
    onOpenDetail: (TaxInvoice, Rect) -> Unit,
) {
    val filters = TaxFilter.entries
    // Land on the first tab that has anything — preferring Da pagare, which is index 0.
    val initialPage = remember(grouped) {
        filters.indexOfFirst { grouped[it].orEmpty().isNotEmpty() }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { filters.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            // Fixed height so the modal stays the same size across both tabs and the swipe
            // doesn't snap at the end when the two pages' content heights differ.
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val filter = filters[page]
            val items = grouped[filter].orEmpty()
            if (items.isEmpty()) {
                EmptyState(
                    icon = filter.emptyIcon(),
                    title = filter.emptyTitle(),
                    body = filter.emptyBody(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = items, key = { "${page}_${it.id.value}" }) { invoice ->
                        HeroInvoiceCard(invoice = invoice, onOpenDetail = onOpenDetail)
                    }
                }
            }
        }

        // targetPage (not currentPage) so the switch tracks the slide as soon as a swipe commits.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(100)),
        ) {
            SegmentedSwitch(
                options = filters,
                selected = filters[pagerState.targetPage],
                onSelected = { filter ->
                    scope.launch { pagerState.animateScrollToPage(filters.indexOf(filter)) }
                },
                label = { it.label },
                borderColor = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

// Tracks its own row bounds so a tap can hand the hero its exact starting rectangle, in
// absolute SCREEN coordinates — the hero lives in a separate full-screen Dialog window, so
// window-relative bounds would be off by the sheet window's own offset.
@Composable
private fun HeroInvoiceCard(
    invoice: TaxInvoice,
    onOpenDetail: (TaxInvoice, Rect) -> Unit,
) {
    val view = LocalView.current
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    TaxInvoiceCard(
        invoice = invoice,
        onClick = {
            val c = coords
            if (c != null && c.isAttached) {
                val b = c.boundsInWindow()
                val loc = IntArray(2)
                view.getLocationOnScreen(loc)
                onOpenDetail(
                    invoice,
                    Rect(b.left + loc[0], b.top + loc[1], b.right + loc[0], b.bottom + loc[1]),
                )
            }
        },
        modifier = Modifier.onGloballyPositioned { coords = it },
    )
}

fun taxesHeaderSubtitle(invoices: List<TaxInvoice>): String? {
    if (invoices.isEmpty()) return null
    val due = invoices
        .filter { it.status == TaxStatus.PENDING || it.status == TaxStatus.EXPIRED }
        .sumOf { it.amount }
    if (due > 0.0) return "${formatEuro(due)} da pagare"
    return if (invoices.size == 1) "1 fattura" else "${invoices.size} fatture"
}

private fun TaxFilter.emptyIcon(): ImageVector = when (this) {
    TaxFilter.ToPay -> Icons.Outlined.Payments
    TaxFilter.Paid -> Icons.AutoMirrored.Outlined.ReceiptLong
}

private fun TaxFilter.emptyTitle(): String = when (this) {
    TaxFilter.ToPay -> "Nessuna tassa da pagare"
    TaxFilter.Paid -> "Nessuna tassa pagata"
}

private fun TaxFilter.emptyBody(): String = when (this) {
    TaxFilter.ToPay -> "Non hai pagamenti in sospeso."
    TaxFilter.Paid -> "Non risultano fatture pagate."
}
