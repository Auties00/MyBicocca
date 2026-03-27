package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.component.calendar.EmptyStateType
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange

@Composable
fun CalendarEmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val (icon, title, description) = when (type) {
        EmptyStateType.DAY -> Triple(
            Icons.Outlined.WbSunny,
            stringResource(R.string.calendar_empty_day),
            stringResource(R.string.calendar_empty_day_desc)
        )
        EmptyStateType.WEEK -> Triple(
            Icons.Outlined.BeachAccess,
            stringResource(R.string.calendar_empty_week),
            stringResource(R.string.calendar_empty_week_desc)
        )
        EmptyStateType.MONTH -> Triple(
            Icons.Outlined.EventAvailable,
            stringResource(R.string.calendar_empty_month),
            stringResource(R.string.calendar_empty_month_desc)
        )
        EmptyStateType.SEARCH -> Triple(
            Icons.Outlined.SearchOff,
            stringResource(R.string.calendar_no_results),
            stringResource(R.string.calendar_no_results_desc)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

// UTILITY: Filter events by search query, type, time range, and location filters
fun filterEvents(
    events: List<CalendarEvent>,
    searchQuery: String,
    activeFilters: Set<EventType>,
    timeRange: TimeRange? = null,
    locationFilter: LocationFilter = LocationFilter()
): List<CalendarEvent> {
    return events.filter { event ->
        // Apply type filter
        val matchesTypeFilter = activeFilters.isEmpty() || event.eventType in activeFilters

        // Apply time range filter
        val matchesTimeFilter = timeRange == null || timeRange.isDefault ||
                (event.startTime != null && timeRange.contains(event.startTime))

        // Apply location filter (rooms only)
        val matchesLocationFilter = locationFilter.isEmpty ||
                (locationFilter.selectedRooms.isNotEmpty() &&
                        event.location?.let { it in locationFilter.selectedRooms } == true)

        // Apply search query
        val matchesSearch = searchQuery.isBlank() || listOfNotNull(
            event.title,
            event.teacherName,
            event.location,
            event.buildingCode,
            event.subjectCode
        ).any { it.contains(searchQuery, ignoreCase = true) }

        matchesTypeFilter && matchesTimeFilter && matchesLocationFilter && matchesSearch
    }
}
