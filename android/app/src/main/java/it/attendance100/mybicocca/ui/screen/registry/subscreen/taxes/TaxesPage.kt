package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.input.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxInvoiceCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.groupByFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.subscreen.taxDetail.TaxDetailOverlay
import kotlinx.coroutines.launch

/**
 * Tasse as a single sheet entry, in the same modal language as ISEE / Rimborsi. The sheet
 * container and the pinned "Tasse" header are owned by BottomSheetSceneStrategy; this body is
 * the Da pagare / Pagate pager plus the full-screen hero detail ([TaxDetailOverlay]) a tapped
 * fattura row expands into, growing from the row's on-screen bounds.
 *
 * Reads the hoisted, shell-scoped [TaxesViewModel] (the same in-memory fetch as the rest of
 * the tax feature), so a re-open shows the cached snapshot instantly while a background
 * refresh runs.
 */
@Suppress("AssignedValueIsNeverRead")
@Composable
fun TaxesPage(
    viewModel: TaxesViewModel,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        if (viewModel.invoices.value is Loadable.Loaded) viewModel.refresh()
    }

    val allInvoices = invoicesState.valueOrNull()
    val grouped = remember(allInvoices) { allInvoices?.groupByFilter() }

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

/**
 * Swaps between the error, loading (held for a beat so quick fetches don't flash it), empty
 * and pager states, animating its height change as content lands so the modal grows to the
 * new size rather than snapping to it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TaxesBody(
    grouped: Map<TaxFilter, List<TaxInvoice>>?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenDetail: (TaxInvoice, Rect) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = grouped == null)
    val settled = grouped != null && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .testTag(TaxesTestTags.ROOT)
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && grouped == null -> Box(modifier = Modifier.testTag(TaxesTestTags.STATE_ERROR)) {
                val haptic = rememberHapticManager()
                SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.common_load_failed),
                    body = stringResource(failure.cause.taxFriendlyMessage()),
                    action = { RetryButton(onClick = { haptic.tap(); onRetry() }) },
                )
            }

            grouped == null || !settled -> Box(modifier = Modifier.testTag(TaxesTestTags.STATE_LOADING)) {
                SheetLoadingIndicator(label = stringResource(R.string.taxes_loading))
            }

            grouped.values.all { it.isEmpty() } -> Box(
                modifier = Modifier
                    .testTag(TaxesTestTags.STATE_EMPTY)
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp),
            ) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.taxes_empty_title),
                    body = stringResource(R.string.taxes_empty_body),
                )
            }

            else -> TaxesPager(grouped = grouped, onOpenDetail = onOpenDetail)
        }
    }
}

/**
 * Swipeable Da pagare / Pagate pager driven by the footer segmented switch (the CFU/Voti
 * pattern), landing on the first tab that has anything — preferring Da pagare, which is index
 * 0. The pager is height-fixed so the modal keeps one size across tabs and the swipe doesn't
 * snap at the end when the two pages' content heights differ, and overscroll is disabled —
 * leftover fling velocity at the page bound would trigger the stretch effect, which reads as
 * the content deforming at the end of every swipe. The snap is a decelerating tween: the
 * stock spring is a stiff critically-damped one (reads as a hard snap) and the expressive
 * spatial spec is underdamped (the page overshoots and wobbles at the edge); the tween glides
 * onto the page with neither artifact. The switch tracks the pager's `targetPage` (not
 * `currentPage`) so it flips as soon as a swipe commits.
 */
@Composable
private fun TaxesPager(
    grouped: Map<TaxFilter, List<TaxInvoice>>,
    onOpenDetail: (TaxInvoice, Rect) -> Unit,
) {
    val filters = TaxFilter.entries
    val initialPage = remember(grouped) {
        filters.indexOfFirst { grouped[it].orEmpty().isNotEmpty() }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { filters.size }
    val scope = rememberCoroutineScope()

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    )

    Column(modifier = Modifier
        .testTag(TaxesTestTags.STATE_CONTENT)
        .fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            flingBehavior = flingBehavior,
            overscrollEffect = null,
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

        Box(
            modifier = Modifier
                .testTag(TaxesTestTags.FILTER_SWITCH)
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

/**
 * Tracks its own row bounds so a tap can hand the hero its exact starting rectangle, in
 * absolute SCREEN coordinates — the hero lives in a separate full-screen Dialog window, so
 * window-relative bounds would be off by the sheet window's own offset.
 */
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
        modifier = Modifier
            .testTag(TaxesTestTags.invoiceCard(invoice.id.value))
            .onGloballyPositioned { coords = it },
    )
}

/** Pinned-header subtitle: the outstanding total while anything is due, else the invoice count. */
@Composable
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
