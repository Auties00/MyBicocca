package it.attendance100.mybicocca.ui.component.calendar.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.EventCard
import it.attendance100.mybicocca.ui.component.calendar.EventCardVariant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Schermata di ricerca eventi.
 * Mostra la barra di ricerca e i risultati raggruppati per data.
 */
@Composable
fun SearchListScreen(
    allEvents: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onDateClick: (LocalDate) -> Unit,
    primaryColor: Color,
    textColor: Color,
    grayColor: Color,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val today = LocalDate.now()

    // Filtra solo eventi futuri (da oggi in poi)
    val futureEvents = remember(allEvents) {
        allEvents.filter { !it.date.isBefore(today) }
            .sortedBy { it.startDateTime }
    }

    // Cerca tra gli eventi futuri
    val searchResults = remember(searchQuery, futureEvents) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            futureEvents.filter { event ->
                listOfNotNull(
                    event.title,
                    event.teacherName,
                    event.location,
                    event.buildingCode,
                ).any { it.contains(searchQuery, ignoreCase = true) }
            }.take(20)
        }
    }

    // Raggruppa per data
    val resultsByDate = remember(searchResults) {
        searchResults.groupBy { it.date }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Titolo con barra verticale rossa - stile filtri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(primaryColor, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.calendar_global_search_title).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = textColor,
                lineHeight = 20.sp
            )
        }

        // Search bar - stile pill come in LocationScreen
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.calendar_global_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Surface(
                        onClick = { searchQuery = "" },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Risultati
        if (searchQuery.isNotEmpty()) {
            if (searchResults.isEmpty()) {
                // Nessun risultato
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = grayColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.calendar_search_no_upcoming),
                            color = grayColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Lista risultati raggruppati per data
                Text(
                    text = stringResource(R.string.calendar_search_upcoming_events),
                    style = MaterialTheme.typography.labelMedium,
                    color = grayColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    resultsByDate.forEach { (date, events) ->
                        // Data header
                        val dateFormatter = DateTimeFormatter.ofPattern(
                            "EEEE d MMMM",
                            Locale.getDefault()
                        )
                        Text(
                            text = date.format(dateFormatter).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onDateClick(date) }
                                .padding(vertical = 4.dp)
                        )

                        // Eventi per questa data
                        events.forEach { event ->
                            EventCard(
                                event = event,
                                variant = EventCardVariant.TIMELINE,
                                onClick = { onEventClick(event) },
                                trailingContent = {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = grayColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            // Suggerimento iniziale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.calendar_search_hint),
                    color = grayColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

