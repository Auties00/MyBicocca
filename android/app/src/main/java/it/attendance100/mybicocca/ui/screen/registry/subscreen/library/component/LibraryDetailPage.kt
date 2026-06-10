package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.occupancyColor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.occupancyLabel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val WeekdayFormat = DateTimeFormatter.ofPattern("EEEE", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * One library's rich page: live occupancy header, today's forecast graph, weekly opening hours
 * and contacts, over a pinned "Prenota un posto" button that enables once live data is loaded.
 * Shows the sheet loading indicator while fetching and an error-with-retry state on failure.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryDetailPage(
    library: Library?,
    liveStatus: Loadable<LibraryLiveStatus>,
    weekHours: Loadable<LibraryWeekHours>,
    detailStatus: SyncStatus,
    onPrenota: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            val context = LocalContext.current
            when (liveStatus) {
                Loadable.NotYetLoaded -> when (detailStatus) {
                    is SyncStatus.Failed -> SheetMessage(
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        title = context.getString(R.string.common_error_title),
                        body = context.getString(R.string.library_status_loading_failed),
                        action = { RetryButton(onClick = onRetry) },
                    )

                    else -> SheetLoadingIndicator(label = context.getString(R.string.common_loading))
                }

                is Loadable.Loaded -> {
                    OccupancyHeader(status = liveStatus.value)

                    if (liveStatus.value.todayForecast.isNotEmpty()) {
                        SectionCard(
                            icon = Icons.AutoMirrored.Outlined.ShowChart,
                            title = context.getString(R.string.library_today_occupancy)
                        ) {
                            ForecastGraph(points = liveStatus.value.todayForecast)
                        }
                    }

                    (weekHours as? Loadable.Loaded)?.value?.let { hours ->
                        SectionCard(
                            icon = Icons.Outlined.Schedule,
                            title = context.getString(R.string.library_opening_hours)
                        ) {
                            WeekHoursList(hours)
                        }
                    }

                    library?.address?.let { address ->
                        ContactRow(icon = Icons.Outlined.LocationOn, value = address)
                    }
                }
            }
            Spacer(Modifier.size(4.dp))
        }

        Button(
            onClick = onPrenota,
            enabled = liveStatus is Loadable.Loaded,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            Icon(Icons.Outlined.EventSeat, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.library_book_seat),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun OccupancyHeader(status: LibraryLiveStatus) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = scheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            val occupancy = status.occupancyPercentage
            if (occupancy != null && status.isOpen) {
                OccupancyRing(percentage = occupancy, diameter = 72.dp, strokeWidth = 8.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (status.isOpen) context.getString(R.string.library_open_now) else context.getString(
                        R.string.library_closed
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (status.isOpen) occupancyColor(0) else scheme.error,
                )
                val detail = when {
                    occupancy != null && status.isOpen -> occupancyLabel(occupancy)
                    else -> null
                }
                detail?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
                }
                status.statusText?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = scheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            }
            content()
        }
    }
}

@Composable
private fun WeekHoursList(hours: LibraryWeekHours) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        hours.days.forEach { day -> WeekHoursRow(day) }
    }
}

@Composable
private fun WeekHoursRow(day: LibraryDayHours) {
    val scheme = MaterialTheme.colorScheme
    val emphasis = if (day.isToday) FontWeight.Bold else FontWeight.Normal
    val color = if (day.isToday) scheme.onSurface else scheme.onSurfaceVariant
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = day.day.format(WeekdayFormat).replaceFirstChar { it.titlecase(Locale.ITALIAN) },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = emphasis,
            color = color,
        )
        val context = LocalContext.current
        Text(
            text = if (day.ranges.isEmpty()) {
                context.getString(R.string.library_closed)
            } else {
                day.ranges.joinToString("  ") { "${it.open.format(TimeFormat)}–${it.close.format(TimeFormat)}" }
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = emphasis,
            color = if (day.ranges.isEmpty()) scheme.onSurfaceVariant else color,
        )
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
    }
}
