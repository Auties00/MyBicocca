package it.attendance100.mybicocca.ui.component.calendar.filter

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.calendar.BuildingWithRooms
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter

/**
 * Schermata selezione location - Material 3 Expressive design con sticky headers.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    buildings: List<BuildingWithRooms>,
    currentFilter: LocationFilter,
    onFilterChange: (LocationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val primaryColor = MaterialTheme.colorScheme.primary

    // Track expanded state for each building
    val expandedBuildings = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Search field - pill style
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
                            text = stringResource(R.string.calendar_location_search_hint),
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

        // Buildings list with sticky headers
        if (buildings.isEmpty()) {
            EmptyLocationState()
        } else {
            val filteredBuildings = buildings.filter { building ->
                searchQuery.isBlank() ||
                building.buildingName.contains(searchQuery, ignoreCase = true) ||
                building.rooms.any { it.contains(searchQuery, ignoreCase = true) }
            }

            // Auto-expand buildings with selections or when searching
            LaunchedEffect(currentFilter.selectedRooms, searchQuery) {
                filteredBuildings.forEach { building ->
                    val hasSelectedRooms = building.rooms.any { it in currentFilter.selectedRooms }
                    if (hasSelectedRooms || searchQuery.isNotBlank()) {
                        expandedBuildings[building.buildingName] = true
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                filteredBuildings.forEach { building ->
                    val isExpanded = expandedBuildings[building.buildingName] ?: false

                    // Sticky header for building
                    stickyHeader(key = "header_${building.buildingName}") {
                        BuildingStickyHeader(
                            building = building,
                            currentFilter = currentFilter,
                            onFilterChange = onFilterChange,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedBuildings[building.buildingName] = !isExpanded
                            }
                        )
                    }

                    // Room items with smooth animation
                    if (building.rooms.isNotEmpty()) {
                        val filteredRooms = building.rooms.filter {
                            searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
                        }

                        item(key = "rooms_${building.buildingName}") {
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 200)
                                ),
                                exit = shrinkVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeOut(
                                    animationSpec = tween(durationMillis = 150)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Chunk rooms into rows of 3
                                    filteredRooms.chunked(3).forEach { rowRooms ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowRooms.forEach { room ->
                                                RoomChip(
                                                    room = room,
                                                    isSelected = room in currentFilter.selectedRooms,
                                                    onToggle = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        val newRooms = if (room in currentFilter.selectedRooms) {
                                                            currentFilter.selectedRooms - room
                                                        } else {
                                                            currentFilter.selectedRooms + room
                                                        }
                                                        onFilterChange(currentFilter.copy(selectedRooms = newRooms))
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            // Fill remaining space if less than 3 items in row
                                            repeat(3 - rowRooms.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sticky header per edificio - rimane fisso in cima mentre si scorrono le aule.
 * Design consistente con MainFilterScreen cards.
 */
@Composable
private fun BuildingStickyHeader(
    building: BuildingWithRooms,
    currentFilter: LocationFilter,
    onFilterChange: (LocationFilter) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Selection state
    val selectedRoomsInBuilding = building.rooms.filter { it in currentFilter.selectedRooms }
    val allRoomsSelected = selectedRoomsInBuilding.size == building.rooms.size && building.rooms.isNotEmpty()
    val isActive = selectedRoomsInBuilding.isNotEmpty()

    // Rotation animation for chevron
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevronRotation"
    )

    // Background solido per coprire il contenuto sotto quando sticky
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .padding(vertical = 6.dp),
        color = surfaceColor
    ) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleExpand()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isActive) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
            border = BorderStroke(
                width = 1.dp,
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icon container - clickable per selezionare tutto l'edificio
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (allRoomsSelected) {
                            // Deselect all rooms
                            val newRooms = currentFilter.selectedRooms - building.rooms.toSet()
                            onFilterChange(currentFilter.copy(selectedRooms = newRooms))
                        } else {
                            // Select all rooms
                            val newRooms = currentFilter.selectedRooms + building.rooms.toSet()
                            onFilterChange(currentFilter.copy(selectedRooms = newRooms))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) primaryColor else primaryColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = stringResource(R.string.filter_select_all_rooms),
                            modifier = Modifier.size(18.dp),
                            tint = if (isActive) Color.White else primaryColor
                        )
                    }
                }

                // Building name
                Text(
                    text = building.buildingName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Room count and chevron
                if (building.rooms.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${selectedRoomsInBuilding.size}/${building.rooms.size} ${stringResource(R.string.filter_rooms)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = rotationAngle }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Room chip - pill style toggle.
 * Design consistente con screenshot: selected = solid primary, unselected = primary border only.
 */
@Composable
private fun RoomChip(
    room: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onToggle,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (isSelected) primaryColor else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = primaryColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = room,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyLocationState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Outlined.LocationOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.calendar_filter_no_locations),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
