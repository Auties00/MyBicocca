package it.attendance100.mybicocca.ui.component.calendar.filter

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

/**
 * Configurazione animazioni stagger per le sezioni
 */
private object MainFilterAnimConfig {
    const val STAGGER_DELAY_MS = 60
    const val ANIMATION_DURATION_MS = 350
    const val INITIAL_OFFSET_Y = 30f
}

/**
 * Schermata principale dei filtri.
 * Unified design - no quick/advanced split.
 */
@Composable
fun MainFilterScreen(
    activeFilters: Set<EventType>,
    onFilterToggle: (EventType) -> Unit,
    timeRange: TimeRange?,
    onTimeRangeChange: (TimeRange?) -> Unit,
    locationFilter: LocationFilter,
    onNavigateToLocation: () -> Unit,
    onJumpToNextExam: () -> Unit,
    onJumpToNextLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Stagger animation state
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    // Capture primary color for use in non-composable lambda
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === SEZIONE 1: TIPO EVENTO ===
        AnimatedSection(
            visible = animationTriggered,
            index = 0
        ) {
            EventTypeCardGrid(
                activeFilters = activeFilters,
                onFilterToggle = onFilterToggle,
                getEventColor = { eventType ->
                    CalendarUtils.getEventColor(eventType, primaryColor)
                }
            )
        }

        // === SEZIONE 2: TIME RANGE ===
        AnimatedSection(
            visible = animationTriggered,
            index = 1
        ) {
            InlineTimeRangeSelector(
                currentRange = timeRange,
                onRangeChange = onTimeRangeChange
            )
        }

        // === SEZIONE 3: LOCATION ===
        AnimatedSection(
            visible = animationTriggered,
            index = 2
        ) {
            LocationNavigationCard(
                selectedCount = locationFilter.count,
                onClick = onNavigateToLocation
            )
        }

        // === SEZIONE 4: QUICK ACTIONS ===
        AnimatedSection(
            visible = animationTriggered,
            index = 3
        ) {
            QuickActionsSection(
                onJumpToNextLesson = onJumpToNextLesson,
                onJumpToNextExam = onJumpToNextExam
            )
        }
    }
}

/**
 * Sezione azioni rapide: Next Lesson e Next Exam
 */
@Composable
private fun QuickActionsSection(
    onJumpToNextLesson: () -> Unit,
    onJumpToNextExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionPill(
                label = stringResource(R.string.filter_preset_next_lesson),
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onJumpToNextLesson()
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            QuickActionPill(
                label = stringResource(R.string.filter_preset_next_exam),
                icon = Icons.Outlined.School,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onJumpToNextExam()
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Pill per azione rapida
 */
@Composable
private fun QuickActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = primaryColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Wrapper per animazione stagger delle sezioni
 */
@Composable
private fun AnimatedSection(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    val delay = index * MainFilterAnimConfig.STAGGER_DELAY_MS

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = MainFilterAnimConfig.ANIMATION_DURATION_MS,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "sectionAlpha$index"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else MainFilterAnimConfig.INITIAL_OFFSET_Y,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sectionOffset$index"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        }
    ) {
        content()
    }
}

