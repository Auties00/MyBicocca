package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.ui.component.ActionBottomBar
import it.attendance100.mybicocca.ui.component.AutoScrollingFilterRow
import it.attendance100.mybicocca.ui.component.EmptyOfflineState
import it.attendance100.mybicocca.ui.component.EmptyState
import it.attendance100.mybicocca.ui.component.ErrorState
import it.attendance100.mybicocca.ui.component.NetworkStatusBar
import it.attendance100.mybicocca.ui.component.shape.DynamicCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonCardList
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonTaxCard
import it.attendance100.mybicocca.ui.component.tax.formatCurrency
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.util.UiFormatter
import it.attendance100.mybicocca.util.getCurrentLocale
import it.attendance100.mybicocca.util.rememberHapticManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TaxStatusFilter {
    All,
    Pagati,
    NonPagati,
    InAttesa,
}

@Composable
fun TaxStatusFilter.label(): String {
    return when (this) {
        TaxStatusFilter.All -> stringResource(R.string.segreterie_filter_all)
        TaxStatusFilter.Pagati -> stringResource(R.string.segreterie_taxes_status_paid)
        TaxStatusFilter.NonPagati -> stringResource(R.string.segreterie_taxes_status_unpaid)
        TaxStatusFilter.InAttesa -> stringResource(R.string.segreterie_taxes_status_waiting)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxesScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TaxesViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    var selectedStatus by remember { mutableStateOf(TaxStatusFilter.All) }
    val charges by viewModel.charges.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val shownCharges = remember(selectedStatus, charges) {
        when (selectedStatus) {
            TaxStatusFilter.All -> charges
            TaxStatusFilter.Pagati -> charges.filter { it.status == "PAID" }
            TaxStatusFilter.NonPagati -> charges.filter {
                it.status == "PENDING" || it.status == "EXPIRED"
            }

            TaxStatusFilter.InAttesa -> charges.filter { it.status == "PENDING" }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            // Filter Chips
            AutoScrollingFilterRow(
                contentPadding = PaddingValues(bottom = 4.dp),
                items = TaxStatusFilter.entries,
                selectedItem = selectedStatus,
                onSelectionChanged = { selectedStatus = it },
                labelProvider = { it.label() },
                leadingIcon = { item, isSelected, defaultIcon ->
                    {
                        when (item) {
                            TaxStatusFilter.NonPagati -> {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = null,
                                    )
                                }
                            }

                            TaxStatusFilter.InAttesa -> {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.HourglassBottom,
                                        contentDescription = null,
                                    )
                                }
                            }

                            else -> {
                                if (isSelected) {
                                    defaultIcon()
                                }
                            }
                        }
                    }
                },
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                indicator = {},
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    isRefreshing -> {
                        Column {
                            NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
                            SkeletonCardList(spacing = 16.dp) { shimmer ->
                                SkeletonTaxCard(shimmerInstance = shimmer)
                            }
                        }
                    }

                    shownCharges.isEmpty() && !isOnline -> EmptyOfflineState()
                    shownCharges.isEmpty() && error != null -> ErrorState(message = error ?: "Errore")
                    shownCharges.isEmpty() -> EmptyState(message = "Nessuna tassa disponibile")

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item {
                                NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
                            }

                            items(
                                items = shownCharges,
                                key = { it.id },
                            ) { charge ->
                                TaxCard(
                                    charge = charge,
                                    onClick = { onNavigateToDetail(charge.id) },
                                )
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        // Bottom action bar (offscreen, available for shared transitions)
        ActionBottomBar(
            isBottomBarVisible = true,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 140.dp)
                .height(140.dp),
        ) {
        }
    }
}

@Composable
fun TaxCard(
    charge: TaxCharge,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val currentLocale = getCurrentLocale()

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
        onClick = {
            onClick.invoke()
            haptic.tap()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Invoice Number
                        Text(
                            text = "Fattura ${charge.invoiceNumber ?: "#${charge.id}"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        // Amount
                        Text(
                            text = formatCurrency(charge.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = charge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            // Expiration Date
                            Text(
                                text = "Scadenza",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayColor(),
                            )
                            Text(
                                text = charge.dueDate?.let { dateStr ->
                                    runCatching {
                                        UiFormatter.getFullDate(
                                            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE),
                                            currentLocale
                                        )
                                    }.getOrDefault(dateStr)
                                } ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        // Status
                        Text(
                            text = when (charge.status) {
                                "PAID" -> if (charge.paymentDate != null) {
                                    val formattedDate = runCatching {
                                        UiFormatter.getFullDate(
                                            LocalDate.parse(charge.paymentDate, DateTimeFormatter.ISO_LOCAL_DATE),
                                            currentLocale
                                        )
                                    }.getOrDefault(charge.paymentDate)
                                    "Pagata il $formattedDate"
                                } else "Pagata"

                                "PENDING" -> "Da pagare"
                                "EXPIRED" -> "Scaduta"
                                "CANCELED" -> "Annullata"
                                else -> charge.status
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = when (charge.status) {
                                "PAID" -> Color(0xFF4CAF50)
                                "CANCELED" -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
        }
    }
}
