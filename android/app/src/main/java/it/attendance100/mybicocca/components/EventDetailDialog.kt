package it.attendance100.mybicocca.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.*
import java.util.*

// ============================================================================
// EVENT STATUS ENUM
// ============================================================================

/**
 * Enum per rappresentare lo status di un evento
 */
enum class EventStatus {
  CANCELLED,
  ENDED,
  IN_PROGRESS,
  UPCOMING;
  
  companion object {
    /**
     * Calcola lo status di un evento
     */
    fun fromEvent(event: CourseEvent): EventStatus {
      val now = LocalDateTime.now()
      return when {
        event.isCancelled -> CANCELLED
        now.isAfter(event.endTime) -> ENDED
        now.isAfter(event.startTime) && now.isBefore(event.endTime) -> IN_PROGRESS
        else -> UPCOMING
      }
    }
  }
}

// ============================================================================
// EVENT DETAIL DIALOG
// ============================================================================

/**
 * Dialog premium per mostrare i dettagli di un evento usando Material 3 BasicAlertDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailDialog(
  event: CourseEvent,
  onDismiss: () -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
  
  // Calcola status
  val eventStatus = remember(event) { EventStatus.fromEvent(event) }

  // === ANIMAZIONI ===
  var isVisible by remember { mutableStateOf(false) }
  var isClosing by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  
  LaunchedEffect(Unit) { isVisible = true }
  
  val animatedDismiss: () -> Unit = {
    if (!isClosing) {
      isClosing = true
      isVisible = false
      scope.launch {
        delay(300)
        onDismiss()
      }
    }
  }
  
  // Animazioni card
  val cardScale by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0.85f,
    animationSpec = if (isClosing) tween(250, easing = EaseInCubic)
    else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "cardScale"
  )
  
  val cardAlpha by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(if (isClosing) 200 else 350, easing = if (isClosing) EaseInCubic else EaseOutCubic),
    label = "cardAlpha"
  )
  
  val cardOffsetY by animateFloatAsState(
    targetValue = if (isVisible) 0f else 80f,
    animationSpec = if (isClosing) tween(250, easing = EaseInCubic)
    else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "cardOffsetY"
  )

  // Stagger animations
  val headerVisible by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(if (isClosing) 150 else 400, delayMillis = if (isClosing) 0 else 100),
    label = "headerVisible"
  )
  
  val timeCardVisible by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(if (isClosing) 150 else 400, delayMillis = if (isClosing) 0 else 200),
    label = "timeCardVisible"
  )
  
  val detailsVisible by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(if (isClosing) 150 else 400, delayMillis = if (isClosing) 0 else 300),
    label = "detailsVisible"
  )
  
  val buttonVisible by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(if (isClosing) 150 else 400, delayMillis = if (isClosing) 0 else 400),
    label = "buttonVisible"
  )

  // Gradient animato
  val infiniteTransition = rememberInfiniteTransition(label = "headerGradient")
  val gradientOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
    label = "gradientOffset"
  )

  // Padding uniforme per tutto il contenuto del dialog
  val dialogContentPadding = 16.dp

  // Material 3 BasicAlertDialog
  BasicAlertDialog(
    onDismissRequest = animatedDismiss,
    modifier = Modifier
        .fillMaxWidth(0.94f)
        .wrapContentHeight()
        .graphicsLayer {
          scaleX = cardScale
          scaleY = cardScale
          alpha = cardAlpha
          translationY = cardOffsetY
        },
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
      usePlatformDefaultWidth = false
    )
  ) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header Section
        EventDialogHeader(
          event = event,
          eventColor = eventColor,
          gradientOffset = gradientOffset,
          headerVisible = headerVisible,
          contentPadding = dialogContentPadding,
          onClose = animatedDismiss
        )

        // Time Section
        EventDialogTimeSection(
          event = event,
          eventColor = eventColor,
          eventStatus = eventStatus,
          textColor = textColor,
          grayColor = grayColor,
          timeCardVisible = timeCardVisible,
          contentPadding = dialogContentPadding
        )

        // Details Section
        EventDialogDetailsSection(
          event = event,
          eventColor = eventColor,
          textColor = textColor,
          grayColor = grayColor,
          detailsVisible = detailsVisible,
          contentPadding = dialogContentPadding
        )

        Spacer(modifier = Modifier.height(dialogContentPadding))

        // E-Learning Button
        EventDialogELearningButton(
          buttonVisible = buttonVisible,
          contentPadding = dialogContentPadding,
          onClick = { /* TODO: Navigate to e-learning */ }
        )
      }
    }
  }
}

// ============================================================================
// DIALOG HEADER SECTION
// ============================================================================

@Composable
private fun EventDialogHeader(
  event: CourseEvent,
  eventColor: Color,
  gradientOffset: Float,
  headerVisible: Float,
  contentPadding: Dp,
  onClose: () -> Unit
) {
  val locale = Locale.getDefault()
  val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
  val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

  Box(
    modifier = Modifier
        .fillMaxWidth()
        .graphicsLayer { alpha = headerVisible }
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              eventColor,
              eventColor.copy(alpha = 0.85f),
              Color(
                red = (eventColor.red * 0.8f + gradientOffset * 0.1f).coerceIn(0f, 1f),
                green = (eventColor.green * 0.9f).coerceIn(0f, 1f),
                blue = (eventColor.blue * 1.1f).coerceIn(0f, 1f),
                alpha = 1f
              )
            ),
            start = Offset(0f, 0f),
            end = Offset(500f + gradientOffset * 200f, 500f)
          )
        )
  ) {
    // Overlay decorativo
    Box(
      modifier = Modifier
          .matchParentSize()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color.White.copy(alpha = 0.05f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.1f)
              )
            )
          )
    )
    
    Column(
      modifier = Modifier.padding(contentPadding),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Top row: type chip + close
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Event type chip
        EventTypeChip(event.eventType)
        // Close button
        FilledIconButton(
          onClick = onClose,
          modifier = Modifier.size(32.dp),
          colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.event_detail_close),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Course name
      Text(
        text = event.courseName,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp
      )

      // Date
      Text(
        text = buildString {
          append(event.startTime.format(dayOfWeekFormatter).replaceFirstChar { it.uppercase() })
          append(", ")
          append(event.startTime.format(dateFormatter))
        },
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
private fun EventTypeChip(eventType: EventType) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White.copy(alpha = 0.2f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = when (eventType) {
          EventType.LECTURE -> Icons.Outlined.School
          EventType.LAB -> Icons.Outlined.Science
          EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
          EventType.OFFICE_HOURS -> Icons.Outlined.People
          EventType.OTHER -> Icons.Outlined.Event
        },
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(12.dp)
      )
      Text(
        text = stringResource(CalendarUtils.getEventTypeStringRes(eventType)),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

// ============================================================================
// DIALOG TIME SECTION
// ============================================================================

@Composable
private fun EventDialogTimeSection(
  event: CourseEvent,
  eventColor: Color,
  eventStatus: EventStatus,
  textColor: Color,
  grayColor: Color,
  timeCardVisible: Float,
  contentPadding: Dp
) {
  val timeFormatter = CalendarUtils.timeFormatter
  val durationText = CalendarUtils.formatDuration(event.startTime, event.endTime)

  // Status text
  val statusText = getStatusText(event, eventStatus)

  Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = contentPadding)
        .offset(y = (-8).dp)
        .graphicsLayer {
          alpha = timeCardVisible
          translationY = (1f - timeCardVisible) * 30f
        },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
  ) {
    // Calcolo automatico dell'allineamento
    val density = LocalDensity.current
    val labelHeight = with(density) { 11.sp.toDp() }
    val spacerHeight = 6.dp
    val timeTextHeight = with(density) { 17.sp.toDp() }
    val durationIndicatorTopOffset = labelHeight + spacerHeight + (timeTextHeight / 2)

    Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(contentPadding + 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Time row with duration indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        // Start time column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = stringResource(R.string.event_detail_start),
            color = grayColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(spacerHeight))
          Text(
            text = event.startTime.format(timeFormatter),
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Duration indicator
        Box(
          modifier = Modifier
              .weight(1f)
              .padding(top = durationIndicatorTopOffset),
          contentAlignment = Alignment.TopCenter
        ) {
          DurationIndicator(
            durationText = durationText,
            eventColor = eventColor,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // End time column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = stringResource(R.string.event_detail_end),
            color = grayColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(spacerHeight))
          Text(
            text = event.endTime.format(timeFormatter),
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Progress bar (solo se in corso)
      if (eventStatus == EventStatus.IN_PROGRESS) {
        Spacer(modifier = Modifier.height(12.dp))
        EventProgressBar(event = event, eventColor = eventColor, grayColor = grayColor)
      }

      // Status badge centered below
      Spacer(modifier = Modifier.height(16.dp))
      EventStatusBadge(
        statusText = statusText,
        eventColor = eventColor
      )
    }
  }
}

@Composable
private fun getStatusText(event: CourseEvent, eventStatus: EventStatus): String {
  return when (eventStatus) {
    EventStatus.CANCELLED -> stringResource(R.string.event_status_cancelled)
    EventStatus.ENDED -> {
      val elapsed = Duration.between(event.endTime, LocalDateTime.now())
      val elapsedMins = elapsed.toMinutes()
      when {
        elapsedMins < 60 -> stringResource(R.string.event_status_ended_mins, elapsedMins)
        elapsedMins < 1440 -> stringResource(R.string.event_status_ended_hours, elapsedMins / 60)
        elapsedMins < 10080 -> stringResource(R.string.event_status_ended_days, elapsedMins / 1440)
        else -> stringResource(R.string.event_status_ended)
      }
    }
    EventStatus.IN_PROGRESS -> {
      val elapsed = Duration.between(event.startTime, LocalDateTime.now())
      val elapsedMins = elapsed.toMinutes()
      if (elapsedMins >= 60) stringResource(R.string.event_status_in_progress_hours, elapsedMins / 60, elapsedMins % 60)
      else stringResource(R.string.event_status_in_progress_mins, elapsedMins)
    }
    EventStatus.UPCOMING -> {
      val until = Duration.between(LocalDateTime.now(), event.startTime)
      val untilMins = until.toMinutes()
      when {
        untilMins < 60 -> stringResource(R.string.event_status_starts_mins, untilMins)
        untilMins < 1440 -> stringResource(R.string.event_status_starts_hours, untilMins / 60)
        else -> stringResource(R.string.event_status_starts_days, untilMins / 1440)
      }
    }
  }
}

@Composable
private fun EventStatusBadge(
  statusText: String,
  eventColor: Color
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = eventColor.copy(alpha = 0.15f)
  ) {
    Text(
      text = statusText,
      color = eventColor,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@Composable
private fun DurationIndicator(
  durationText: String,
  eventColor: Color,
  modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
          .weight(1f)
          .height(2.dp)
          .padding(horizontal = 8.dp)
          .background(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, eventColor.copy(alpha = 0.5f))),
            shape = RoundedCornerShape(1.dp)
          )
    )
    AssistChip(
      onClick = {},
      label = { Text(durationText, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
      },
      colors = AssistChipDefaults.assistChipColors(
        containerColor = eventColor.copy(alpha = 0.15f),
        labelColor = eventColor,
        leadingIconContentColor = eventColor
      ),
      border = null
    )
    Box(
      modifier = Modifier
          .weight(1f)
          .height(2.dp)
          .padding(horizontal = 8.dp)
          .background(
            brush = Brush.horizontalGradient(listOf(eventColor.copy(alpha = 0.5f), Color.Transparent)),
            shape = RoundedCornerShape(1.dp)
          )
    )
  }
}

@Composable
private fun EventProgressBar(
  event: CourseEvent,
  eventColor: Color,
  grayColor: Color
) {
  val progress = remember { CalendarUtils.calculateEventProgress(event) }
  
  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(1000, easing = EaseOutCubic),
    label = "animatedProgress"
  )
  
  LinearProgressIndicator(
    progress = { animatedProgress },
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 16.dp)
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
    color = eventColor,
    trackColor = grayColor.copy(alpha = 0.2f)
  )
}

// ============================================================================
// DIALOG DETAILS SECTION
// ============================================================================

@Composable
private fun EventDialogDetailsSection(
  event: CourseEvent,
  eventColor: Color,
  textColor: Color,
  grayColor: Color,
  detailsVisible: Float,
  contentPadding: Dp
) {
  Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = contentPadding)
        .padding(top = 8.dp)
        .graphicsLayer {
          alpha = detailsVisible
          translationY = (1f - detailsVisible) * 30f
        },
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Location
    CalendarUtils.formatEventLocation(event.room, event.building)?.let { location ->
      DetailInfoRow(
        icon = Icons.Outlined.LocationOn,
        label = stringResource(R.string.event_detail_location),
        value = location,
        accentColor = eventColor,
        textColor = textColor,
        grayColor = grayColor
      )
    }

    // Professor
    event.professor?.let { professor ->
      DetailInfoRow(
        icon = Icons.Outlined.Person,
        label = stringResource(R.string.event_detail_professor),
        value = professor,
        accentColor = eventColor,
        textColor = textColor,
        grayColor = grayColor
      )
    }

    // Notes
    if (!event.notes.isNullOrBlank()) {
      DetailInfoRow(
        icon = Icons.Outlined.Notes,
        label = stringResource(R.string.event_detail_notes),
        value = event.notes,
        accentColor = eventColor,
        textColor = textColor,
        grayColor = grayColor
      )
    }
  }
}

@Composable
private fun DetailInfoRow(
  icon: ImageVector,
  label: String,
  value: String,
  accentColor: Color,
  textColor: Color,
  grayColor: Color
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon container
      Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }

      // Text content
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Text(
          text = label,
          color = grayColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = value,
          color = textColor,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

// ============================================================================
// DIALOG E-LEARNING BUTTON
// ============================================================================

@Composable
private fun EventDialogELearningButton(
  buttonVisible: Float,
  contentPadding: Dp,
  onClick: () -> Unit
) {
  val buttonColor = MaterialTheme.colorScheme.primaryContainer
  Surface(
    onClick = onClick,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = contentPadding)
        .padding(bottom = contentPadding)
        .graphicsLayer {
          alpha = buttonVisible
          translationY = (1f - buttonVisible) * 30f
        },
    shape = RoundedCornerShape(16.dp),
    color = buttonColor,
    shadowElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon container
      Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.School,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
      }

      // Text content
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Text(
          text = stringResource(R.string.event_action_elearning),
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = stringResource(R.string.event_action_elearning_subtitle),
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.7f),
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
