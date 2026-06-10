package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatTaxDate
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxFriendlyMessage

/**
 * Root page of the Rimborsi sheet (the refunds list), in the same modal language as ISEE /
 * percorso. The sheet container, pinned morphing header and the list -> detail page
 * transition are owned by BottomSheetSceneStrategy; the refund detail is a separate
 * back-stack entry (SheetRoute.RefundDetail rendering [RefundDetailPage]). Reads its own
 * [RefundsViewModel] (no local cache — the Esse3 lista-rimborsi is fetched live), refreshing
 * in the background when re-opened on an already-loaded snapshot.
 */
@Composable
fun RefundsListPage(
    viewModel: RefundsViewModel,
    onOpenDetail: (refundKey: Long) -> Unit,
) {
    val data by viewModel.refunds.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        if (viewModel.refunds.value is Loadable.Loaded) viewModel.refresh()
    }

    RefundsListBody(
        refunds = data.valueOrNull(),
        syncStatus = syncStatus,
        onRetry = viewModel::refresh,
        onOpenDetail = { onOpenDetail(it.refundKey()) },
    )
}

/** "3 rimborsi · 2 in lavorazione" — count line in place of a sub-page title. */
fun refundsHeaderSubtitle(refunds: List<Refund>): String? {
    if (refunds.isEmpty()) return null
    val total = if (refunds.size == 1) "1 rimborso" else "${refunds.size} rimborsi"
    val pending = refunds.count { !it.refunded }
    return if (pending == 0) total else "$total · $pending in lavorazione"
}

/**
 * Swaps between the error, loading (held for a beat so quick fetches don't flash it), empty
 * and list states, animating its height change as content lands so the modal grows to the
 * new size rather than snapping to it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RefundsListBody(
    refunds: List<Refund>?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenDetail: (Refund) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = refunds == null)
    val settled = refunds != null && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = sizeSpec)) {
        when {
            failure != null && refunds == null -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = failure.cause.taxFriendlyMessage(),
                action = { RetryButton(onClick = onRetry) },
            )

            !settled || refunds == null -> SheetLoadingIndicator(label = "Caricamento rimborsi…")

            refunds.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp),
            ) {
                EmptyState(
                    icon = Icons.Outlined.CurrencyExchange,
                    title = "Nessun rimborso",
                    body = "Non risultano rimborsi sulla tua carriera.",
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val refundedTotal = refunds.filter { it.refunded }.sumOf { it.amount ?: 0.0 }
                if (refundedTotal > 0.0) {
                    item(key = "summary") {
                        RefundsSummaryCard(
                            refundedTotal = refundedTotal,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                }
                itemsIndexed(
                    items = refunds,
                    key = { _, refund -> refund.refundKey() },
                ) { index, refund ->
                    RefundRow(
                        refund = refund,
                        isFirst = index == 0,
                        isLast = index == refunds.lastIndex,
                        onClick = { onOpenDetail(refund) },
                    )
                }
            }
        }
    }
}

/** Hero strip atop the list: the total already credited back, in the brand-tinted container. */
@Composable
private fun RefundsSummaryCard(
    refundedTotal: Double,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.primaryContainer)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "TOTALE RIMBORSATO",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = scheme.onPrimaryContainer,
        )
        Text(
            text = formatEuro(refundedTotal),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimaryContainer,
        )
    }
}

/**
 * Segmented group row like the buildings / ISEE lists: 28dp corners cap the group's ends, 6dp
 * where rows touch. A status-tinted icon tile leads, the amount + a.a. follow, a status pill
 * trails.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RefundRow(
    refund: Refund,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val tileColor = if (refund.refunded) scheme.primaryContainer else scheme.surfaceContainerHighest
    val tileTint = if (refund.refunded) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = tileColor) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.CurrencyExchange,
                        contentDescription = null,
                        tint = tileTint,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = refund.amount?.let(::formatEuro) ?: "—",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                refund.academicYearLabel()?.let { year ->
                    Text(
                        text = "Anno accademico $year",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            RefundStatusPill(refunded = refund.refunded)
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RefundStatusPill(refunded: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val container = if (refunded) scheme.tertiaryContainer else scheme.surfaceContainerHighest
    val content = if (refunded) scheme.onTertiaryContainer else scheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = if (refunded) "Rimborsato" else "In lavorazione",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

/**
 * One refund as a headerless page inside the sheet pager: the morphing header carries the
 * amount + a.a., the hero card restates the amount with its status pill, and the sections
 * below list every field Esse3 returns for the refund.
 */
@Composable
fun RefundDetailPage(
    refund: Refund,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(scheme.surfaceContainerHigh)
                    .padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "IMPORTO RIMBORSO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = scheme.primary,
                )
                Text(
                    text = refund.amount?.let(::formatEuro) ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                RefundStatusPill(refunded = refund.refunded)
            }
        }

        item {
            DetailSection(
                title = "Stato",
                rows = buildList {
                    refund.creditDate?.let { add("Accredito" to it.formatTaxDate()) }
                    refund.mandateNumber?.let { add("Mandato" to it) }
                    refund.collectedBy?.let { add("Incasso" to it) }
                },
            )
        }

        item {
            DetailSection(
                title = "Dettagli",
                rows = buildList {
                    refund.reasonCode?.let { add("Causale" to it) }
                    refund.description?.let { add("Descrizione" to it) }
                    refund.issueDate?.let { add("Emissione" to it.formatTaxDate()) }
                    refund.processingDate?.let { add("Elaborazione" to it.formatTaxDate()) }
                    refund.paymentDate?.let { add("Pagamento" to it.formatTaxDate()) }
                    refund.note?.let { add("Nota" to it) }
                },
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = scheme.primary,
        )
        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.3f),
                )
            }
        }
    }
}

/**
 * Refunds with a null invoiceId are rare but possible; identity is the fallback so each row
 * still gets a stable selection/list key.
 */
fun Refund.refundKey(): Long = invoiceId ?: hashCode().toLong()

/** academicYear 2024 -> "2024/25". */
private fun Refund.academicYearLabel(): String? =
    academicYear?.let { "$it/${"%02d".format((it + 1) % 100)}" }

/**
 * Detail header text (paired with [refundHeaderSubtitle]), exposed for the sheet entry's
 * pinned header in MainShell.
 */
fun refundHeaderTitle(refund: Refund): String = refund.amount?.let(::formatEuro) ?: "Rimborso"

fun refundHeaderSubtitle(refund: Refund): String? =
    refund.academicYearLabel()?.let { "Anno accademico $it" } ?: "Dettaglio rimborso"
