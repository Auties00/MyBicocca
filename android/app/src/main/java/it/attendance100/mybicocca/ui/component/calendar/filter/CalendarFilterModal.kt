package it.attendance100.mybicocca.ui.component.calendar.filter

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.component.calendar.filter.FilterScreen
import it.attendance100.mybicocca.ui.component.calendar.filter.LocationScreen
import it.attendance100.mybicocca.ui.component.calendar.filter.MainFilterScreen
import it.attendance100.mybicocca.ui.component.calendar.filter.rememberFilterModalState
import it.attendance100.mybicocca.ui.component.calendar.BuildingWithRooms
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange

/**
 * ModalBottomSheet filtri calendario - unified design.
 * Tutti i filtri in un'unica vista senza quick/advanced split.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarFilterModal(
    onDismiss: () -> Unit,
    activeFilters: Set<EventType>,
    onFilterToggle: (EventType) -> Unit,
    timeRange: TimeRange?,
    onTimeRangeChange: (TimeRange?) -> Unit,
    locationFilter: LocationFilter,
    onLocationFilterChange: (LocationFilter) -> Unit,
    buildings: List<BuildingWithRooms>,
    onClearAll: () -> Unit,
    onJumpToNextExam: () -> Unit,
    onJumpToNextLesson: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val navigationState = rememberFilterModalState()

    // Calculate active filter count
    val activeFilterCount = remember(activeFilters, timeRange, locationFilter) {
        var count = activeFilters.size
        if (timeRange != null && !timeRange.isDefault) count++
        if (!locationFilter.isEmpty) count++
        count
    }

    ModalBottomSheet(
        onDismissRequest = {
            // Handle dismiss based on current screen
            if (!navigationState.isOnMain) {
                navigationState.goBack()
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            FilterHeader(
                currentScreen = navigationState.currentScreen,
                onBack = {
                    if (navigationState.isOnMain) {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    } else {
                        navigationState.goBack()
                    }
                },
                onClearAll = onClearAll,
                activeFilterCount = activeFilterCount,
                onClearLocation = { onLocationFilterChange(LocationFilter()) },
                hasLocationFilter = !locationFilter.isEmpty
            )

            // Content with navigation
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = navigationState.currentScreen,
                    transitionSpec = {
                        val direction = if (targetState == FilterScreen.Main) -1 else 1
                        slideInHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> direction * fullWidth }
                        ) + fadeIn(tween(200)) togetherWith slideOutHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -direction * fullWidth }
                        ) + fadeOut(tween(150))
                    },
                    label = "filterScreenTransition"
                ) { screen ->
                    when (screen) {
                        FilterScreen.Main -> MainFilterScreen(
                            activeFilters = activeFilters,
                            onFilterToggle = onFilterToggle,
                            timeRange = timeRange,
                            onTimeRangeChange = onTimeRangeChange,
                            locationFilter = locationFilter,
                            onNavigateToLocation = { navigationState.navigateTo(FilterScreen.Location) },
                            onJumpToNextExam = {
                                onJumpToNextExam()
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            },
                            onJumpToNextLesson = {
                                onJumpToNextLesson()
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }
                        )
                        FilterScreen.Location -> LocationScreen(
                            buildings = buildings,
                            currentFilter = locationFilter,
                            onFilterChange = onLocationFilterChange
                        )
                    }
                }
            }

            // Bottom padding for navigation bar
            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(16.dp)
            )
        }
    }
}

/**
 * Header for filter modal - unified design
 */
@Composable
private fun FilterHeader(
    currentScreen: FilterScreen,
    onBack: () -> Unit,
    onClearAll: () -> Unit,
    activeFilterCount: Int,
    onClearLocation: (() -> Unit)? = null,
    hasLocationFilter: Boolean = false
) {
    val isMain = currentScreen == FilterScreen.Main
    val primaryColor = MaterialTheme.colorScheme.primary

    val title = when (currentScreen) {
        FilterScreen.Main -> null // Use stacked title
        FilterScreen.Location -> stringResource(R.string.calendar_filter_location)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
    ) {
        if (isMain) {
            // Main screen: stacked title with colored bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Red Bar + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(46.dp)
                            .background(primaryColor, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.calendar_header_filter).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                // Right: Reset Button (pill style)
                AnimatedVisibility(
                    visible = activeFilterCount > 0,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Surface(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, primaryColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterAltOff,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.calendar_filter_reset),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        } else {
            // Sub-screens: header with back, red bar, title, and reset pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.arrow_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Red vertical bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(primaryColor, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = (title ?: "").uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Right: Reset Button for location screen
                if (currentScreen == FilterScreen.Location && onClearLocation != null) {
                    AnimatedVisibility(
                        visible = hasLocationFilter,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            onClick = onClearLocation,
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, primaryColor)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterAltOff,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.calendar_filter_reset),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
