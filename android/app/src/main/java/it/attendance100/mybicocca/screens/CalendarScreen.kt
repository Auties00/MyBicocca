package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*

enum class CalendarViewMode {
  LIST,    // Vista lista con giorni
  WEEK     // Vista griglia settimanale
}

@Composable
fun CalendarScreen(
  viewModel: CalendarViewModel = hiltViewModel()
) {
  val backgroundColor = MaterialTheme.colorScheme.background
  val textColor = MaterialTheme.colorScheme.onBackground
  val primaryColor = MaterialTheme.colorScheme.primary
  val grayColor = if (backgroundColor == BackgroundColor) GrayColor else GrayColorLight

  val selectedDate by viewModel.selectedDate.observeAsState(LocalDate.now())
  val currentMonth by viewModel.currentMonth.observeAsState(YearMonth.now())
  val eventsForSelectedDate by viewModel.eventsForSelectedDate.observeAsState(emptyList())
  val eventsForCurrentMonth by viewModel.eventsForCurrentMonth.observeAsState(emptyList())
  val isLoading by viewModel.isLoading.observeAsState(false)

  var viewMode by remember { mutableStateOf(CalendarViewMode.LIST) }

  Column(
    modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
  ) {
    // Header con navigazione e toggle vista
    CalendarHeader(
      currentMonth = currentMonth,
      viewMode = viewMode,
      onViewModeChange = { viewMode = it },
      onPreviousMonth = { viewModel.previousMonth() },
      onNextMonth = { viewModel.nextMonth() },
      onToday = { viewModel.goToToday() },
      textColor = textColor,
      grayColor = grayColor,
      primaryColor = primaryColor
    )

    // Selettore giorni (sempre visibile)
    HorizontalDaySelector(
      selectedDate = selectedDate,
      currentMonth = currentMonth,
      viewMode = viewMode,
      onDateSelected = { date -> viewModel.selectDate(date) },
      onWeekChanged = { weekStartDate ->
        // Aggiorna il mese quando si scorre di settimana
        val weekMonth = YearMonth.from(weekStartDate)
        if (currentMonth != weekMonth) {
          viewModel.setCurrentMonth(weekMonth)
        }
      },
      textColor = textColor,
      grayColor = grayColor,
      primaryColor = primaryColor
    )

    HorizontalDivider(
      color = grayColor.copy(alpha = 0.2f),
    )

    // Visualizzazione eventi in base al modo selezionato
    AnimatedContent(
      targetState = viewMode,
      transitionSpec = {
        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
          slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 4 } togetherWith
          fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
          slideOutVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it / 4 }
      },
      label = "calendar_view_mode"
    ) { mode ->
      when (mode) {
        CalendarViewMode.LIST -> {
          EventsList(
            events = eventsForSelectedDate,
            isLoading = isLoading,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor
          )
        }
        CalendarViewMode.WEEK -> {
          WeekEventsGrid(
            selectedDate = selectedDate,
            events = eventsForCurrentMonth,
            isLoading = isLoading,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            backgroundColor = backgroundColor
          )
        }
      }
    }
  }
}

@Composable
fun CalendarHeader(
  currentMonth: YearMonth,
  viewMode: CalendarViewMode,
  onViewModeChange: (CalendarViewMode) -> Unit,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onToday: () -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  val locale = Locale.getDefault()
  val monthYearFormatter = CalendarUtils.monthYearFormatter(locale)

  // header
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Bottone Oggi
    FilledTonalButton(
      onClick = onToday,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = primaryColor.copy(alpha = 0.12f),
        contentColor = primaryColor
      )
    ) {
      Text(
        text = stringResource(R.string.calendar_today),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }

    // Navigazione mese al centro
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onPreviousMonth,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ChevronLeft,
          contentDescription = stringResource(R.string.calendar_previous_month),
          tint = textColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Text(
        text = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() },
        color = textColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 2.dp)
      )

      IconButton(
        onClick = onNextMonth,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = stringResource(R.string.calendar_next_month),
          tint = textColor,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    // Toggle Vista animato
    AnimatedViewToggle(
      viewMode = viewMode,
      onViewModeChange = onViewModeChange,
      primaryColor = primaryColor
    )
  }
}

@Composable
private fun AnimatedViewToggle(
  viewMode: CalendarViewMode,
  onViewModeChange: (CalendarViewMode) -> Unit,
  primaryColor: Color
) {
  // Animazione rotazione (0° -> 180°)
  val rotation by animateFloatAsState(
    targetValue = if (viewMode == CalendarViewMode.LIST) 0f else 180f,
    animationSpec = tween(durationMillis = 600),
    label = "view_toggle_rotation"
  )

  FilledTonalIconButton(
    onClick = {
      val newMode = if (viewMode == CalendarViewMode.LIST) {
        CalendarViewMode.WEEK
      } else {
        CalendarViewMode.LIST
      }
      onViewModeChange(newMode)
    },
    modifier = Modifier.size(40.dp),
    colors = IconButtonDefaults.filledTonalIconButtonColors(
      containerColor = if (viewMode == CalendarViewMode.LIST) primaryColor else Color.White,
      contentColor = if (viewMode == CalendarViewMode.LIST) Color.White else primaryColor
    )
  ) {
    Icon(
      imageVector = if (viewMode == CalendarViewMode.LIST) {
        Icons.Outlined.ViewAgenda
      } else {
        Icons.Outlined.CalendarViewWeek
      },
      contentDescription = if (viewMode == CalendarViewMode.LIST) {
        stringResource(R.string.calendar_list_view)
      } else {
        stringResource(R.string.calendar_week_view)
      },
      modifier = Modifier
          .size(20.dp)
          .graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
          }
    )
  }
}


@Composable
fun HorizontalDaySelector(
  selectedDate: LocalDate,
  currentMonth: YearMonth,
  viewMode: CalendarViewMode,
  onDateSelected: (LocalDate) -> Unit,
  onWeekChanged: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  // Data di riferimento fissa (epoca)
  val referenceDate = remember { LocalDate.of(2020, 1, 1) }
  
  // Calcola la pagina iniziale in base a selectedDate
  val initialPage = remember(selectedDate) {
    val weeksBetween = ChronoUnit.WEEKS.between(
      referenceDate.with(DayOfWeek.MONDAY),
      selectedDate.with(DayOfWeek.MONDAY)
    )
    1000 + weeksBetween.toInt()
  }
  
  val pagerState = rememberPagerState(
    initialPage = initialPage,
    pageCount = { 2000 }
  )

  // Sincronizza il pager quando cambia selectedDate (programmaticamente)
  LaunchedEffect(selectedDate) {
    val weeksBetween = ChronoUnit.WEEKS.between(
      referenceDate.with(DayOfWeek.MONDAY),
      selectedDate.with(DayOfWeek.MONDAY)
    )
    val targetPage = 1000 + weeksBetween.toInt()
    if (pagerState.currentPage != targetPage) {
      pagerState.animateScrollToPage(targetPage)
    }
  }

  // Traccia il cambio di pagina per aggiornare il mese
  LaunchedEffect(pagerState.currentPage) {
    val weekOffset = pagerState.currentPage - 1000
    val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
    onWeekChanged(weekStart)
  }

  HorizontalPager(
    state = pagerState,
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
  ) { page ->
    val weekOffset = page - 1000
    val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      weekDays.forEach { date ->
        val isSelected = date == selectedDate
        val isToday = CalendarUtils.isToday(date)
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        
        // In modalità griglia, permetti click solo se non è già selezionato
        // In modalità lista, permetti sempre il click
        val isClickable = viewMode == CalendarViewMode.LIST || !isSelected

        Column(
          modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(
                when {
                  isSelected -> primaryColor
                  isToday -> primaryColor.copy(alpha = 0.2f)
                  else -> grayColor.copy(alpha = 0.1f)
                }
              )
              .then(
                if (isClickable) {
                  Modifier.clickable { onDateSelected(date) }
                } else {
                  Modifier
                }
              )
              .padding(vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
          text = dayOfWeek.uppercase(),
          color = when {
            isSelected -> Color.White
            isToday -> primaryColor
            else -> grayColor
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = date.dayOfMonth.toString(),
          color = when {
            isSelected -> Color.White
            isToday -> primaryColor
            else -> textColor
          },
          fontSize = 18.sp,
          fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
        // Indicatore eventi
        if (isToday && !isSelected) {
          Spacer(modifier = Modifier.height(4.dp))
          TodayIndicator(color = primaryColor, size = 6.dp)
        }
      }
      }
    }
  }
}

// Vista Griglia Eventi Settimanale (senza selettore giorni)
@Composable
fun WeekEventsGrid(
  selectedDate: LocalDate,
  events: List<CourseEvent>,
  isLoading: Boolean,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  // Stato per l'evento selezionato (modale)
  var selectedEvent by remember { mutableStateOf<CourseEvent?>(null) }
  
  // Calcola la settimana corrente basata su selectedDate
  val weekStart = selectedDate.with(DayOfWeek.MONDAY)
  val daysOfWeek = (0..6).map { weekStart.plusDays(it.toLong()) }

  // Filtra eventi per questa settimana
  val weekEvents = events.filter { event ->
    val eventDate = event.startTime.toLocalDate()
    eventDate >= daysOfWeek.first() && eventDate <= daysOfWeek.last()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Griglia oraria con eventi flottanti
    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(40.dp),
          strokeWidth = 3.dp,
          color = primaryColor
        )
      }
    } else {
      // Scroll contenitore
      val scrollState = rememberScrollState()

      Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
      ) {
        // Griglia di sfondo con linee orarie
        WeekGridBackground(
          startHour = CalendarUtils.WEEK_START_HOUR,
          endHour = CalendarUtils.WEEK_END_HOUR,
          textColor = textColor,
          grayColor = grayColor
        )

        // Eventi flottanti sopra la griglia
        WeekEventsOverlay(
          daysOfWeek = daysOfWeek,
          events = weekEvents,
          startHour = CalendarUtils.WEEK_START_HOUR,
          onEventClick = { event -> selectedEvent = event },
          primaryColor = primaryColor
        )
      }
    }

    // Dialog modale per evento selezionato
    AnimatedVisibility(
      visible = selectedEvent != null,
      enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
        scaleIn(initialScale = 0.8f, animationSpec = tween(300, easing = FastOutSlowInEasing)),
      exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
        scaleOut(targetScale = 0.8f, animationSpec = tween(200, easing = FastOutSlowInEasing))
    ) {
      selectedEvent?.let { event ->
        EventDetailDialog(
          event = event,
          onDismiss = { selectedEvent = null },
          textColor = textColor,
          grayColor = grayColor,
          primaryColor = primaryColor,
          backgroundColor = backgroundColor
        )
      }
    }
  }
}

// Header settimana pulito
@Composable
fun WeekHeader(
  daysOfWeek: List<LocalDate>,
  selectedDate: LocalDate,
  onDateSelected: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(start = 60.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    daysOfWeek.forEach { date ->
      val isToday = CalendarUtils.isToday(date)
      val isSelected = date == selectedDate
      val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

      Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) primaryColor.copy(alpha = 0.15f)
              else Color.Transparent
            )
            .clickable { onDateSelected(date) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dayName.take(3).uppercase(),
          color = if (isSelected) primaryColor else grayColor,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = date.dayOfMonth.toString(),
          color = if (isSelected) primaryColor else textColor,
          fontSize = 15.sp,
          fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (isToday && !isSelected) {
          Spacer(modifier = Modifier.height(2.dp))
          TodayIndicator(color = primaryColor, size = 4.dp)
        }
      }
    }
  }
}

// Griglia di sfondo con linee orizzontali per ogni ora
@Composable
fun WeekGridBackground(
  startHour: Int,
  endHour: Int,
  textColor: Color,
  grayColor: Color
) {
  val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
  val totalHeight = ((endHour - startHour) * hourSlotHeight.value).dp

  Column(
    modifier = Modifier
        .fillMaxWidth()
        .height(totalHeight)
  ) {
    for (hour in startHour until endHour) {
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(hourSlotHeight)
      ) {
        // Colonna orario a sinistra
        Box(
          modifier = Modifier
              .width(50.dp)
              .fillMaxHeight(),
          contentAlignment = Alignment.TopStart
        ) {
          Text(
            text = String.format("%02d", hour),
            color = grayColor.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
          )
        }

        // Linea orizzontale che attraversa tutta la settimana
        Box(
          modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
        ) {
          HorizontalDivider(
            color = grayColor.copy(alpha = 0.15f),
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.TopCenter)
          )
        }
      }
    }
  }
}

// Overlay con eventi flottanti posizionati in modo assoluto
@Composable
fun WeekEventsOverlay(
  daysOfWeek: List<LocalDate>,
  events: List<CourseEvent>,
  startHour: Int,
  onEventClick: (CourseEvent) -> Unit,
  primaryColor: Color
) {
  val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT

  Layout(
    content = {
      daysOfWeek.forEachIndexed { dayIndex, date ->
        // Eventi per questo giorno
        val dayEvents = events.filter { event ->
          event.startTime.toLocalDate() == date
        }

        dayEvents.forEach { event ->
          // Calcola posizione verticale basata sull'orario di inizio
          val startMinutes = event.startTime.hour * 60 + event.startTime.minute
          val startOffsetMinutes = startMinutes - (startHour * 60)

          // Calcola altezza basata sulla durata
          val durationMinutes = java.time.Duration.between(
            event.startTime,
            event.endTime
          ).toMinutes()
          val eventHeightDp = (durationMinutes / 60f * hourSlotHeight.value).dp

          FloatingEventBox(
            event = event,
            height = eventHeightDp,
            onClick = { onEventClick(event) },
            primaryColor = primaryColor,
            modifier = Modifier.layoutId("${dayIndex}_${event.id}")
          )
        }
      }
    },
    modifier = Modifier
        .fillMaxWidth()
        .padding(start = 50.dp)
  ) { measurables, constraints ->
    val totalDays = 7
    val dayWidth = constraints.maxWidth / totalDays

    // Calcola altezza massima necessaria per la griglia (8-20 = 12 ore)
    val totalHours = 12
    val maxLayoutHeight = (totalHours * hourSlotHeight.toPx()).toInt()

    val placeables = measurables.map { measurable ->
      val layoutId = measurable.layoutId as String
      val parts = layoutId.split("_")
      val dayIndex = parts[0].toInt()
      val eventId = parts[1].toLong()

      // Trova l'evento corrispondente per ottenere i dati di posizionamento
      val event = events.firstOrNull { it.id == eventId }
      event?.let {
        val startMinutes = it.startTime.hour * 60 + it.startTime.minute
        val startOffsetMinutes = startMinutes - (startHour * 60)
        val topOffsetPx = (startOffsetMinutes / 60f * hourSlotHeight.toPx()).toInt()

        val placeable = measurable.measure(
          constraints.copy(
            minWidth = 0,
            maxWidth = dayWidth - 4, // Padding orizzontale
            minHeight = 0
          )
        )

        Triple(placeable, dayIndex * dayWidth + 2, topOffsetPx)
      }
    }.filterNotNull()

    layout(constraints.maxWidth, maxLayoutHeight) {
      placeables.forEach { (placeable, x, y) ->
        placeable.place(x, y)
      }
    }
  }
}

// Box evento flottante
@Composable
fun FloatingEventBox(
  event: CourseEvent,
  height: Dp,
  onClick: () -> Unit,
  primaryColor: Color,
  modifier: Modifier = Modifier
) {
  val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

  Surface(
    modifier = modifier.height(height),
    shape = RoundedCornerShape(8.dp),
    color = eventColor,
    shadowElevation = 2.dp,
    onClick = onClick
  ) {
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(6.dp),
      contentAlignment = Alignment.TopStart
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        // Nome corso
        Text(
          text = event.courseName,
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 3,
          lineHeight = 12.sp,
          overflow = TextOverflow.Ellipsis
        )

        // Aula (se c'è spazio)
        if (height > 60.dp) {
          event.room?.let { room ->
            Text(
              text = room,
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

// Dialog modale con tutte le informazioni dell'evento
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

  // Background scrim
  Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(
          onClick = onDismiss,
          indication = null,
          interactionSource = remember { MutableInteractionSource() }
        ),
    contentAlignment = Alignment.Center
  ) {
    // Dialog card
    Surface(
      modifier = Modifier
          .fillMaxWidth(0.9f)
          .clickable(
            onClick = {},
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          ),
      shape = RoundedCornerShape(20.dp),
      color = backgroundColor,
      shadowElevation = 8.dp
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Header colorato
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = eventColor,
          shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
          Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
          ) {
            // Badge tipo
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.White.copy(alpha = 0.25f)
            ) {
              Text(
                text = stringResource(CalendarUtils.getEventTypeStringRes(event.eventType)),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nome corso
            Text(
              text = event.courseName,
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              lineHeight = 24.sp
            )

            // Codice corso
            event.courseCode?.let { code ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = code,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Dettagli evento
        Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Data e orario
          DetailRow(
            icon = Icons.Default.CalendarMonth,
            label = stringResource(R.string.event_detail_date_time),
            value = buildString {
              append(event.startTime.toLocalDate().format(CalendarUtils.fullDateFormatter()))
              append("\n")
              append(CalendarUtils.formatTime(event.startTime.hour, event.startTime.minute))
              append(" - ")
              append(CalendarUtils.formatTime(event.endTime.hour, event.endTime.minute))
            },
            textColor = textColor,
            primaryColor = primaryColor
          )

          // Professore
          event.professor?.let { professor ->
            DetailRow(
              icon = Icons.Default.Person,
              label = stringResource(R.string.event_detail_professor),
              value = professor,
              textColor = textColor,
              primaryColor = primaryColor
            )
          }

          // Aula e edificio
          if (event.room != null || event.building != null) {
            DetailRow(
              icon = Icons.Default.LocationOn,
              label = stringResource(R.string.event_detail_location),
              value = buildString {
                event.room?.let { append(it) }
                if (event.room != null && event.building != null) append("\n")
                event.building?.let { append(it) }
              },
              textColor = textColor,
              primaryColor = primaryColor
            )
          }

          // Note
          event.notes?.let { notes ->
            DetailRow(
              icon = Icons.AutoMirrored.Outlined.Notes,
              label = stringResource(R.string.event_detail_notes),
              value = notes,
              textColor = textColor,
              primaryColor = primaryColor
            )
          }

          // Bottone chiudi
          Spacer(modifier = Modifier.height(8.dp))
          TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
          ) {
            Text(
              text = stringResource(R.string.event_detail_close),
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = primaryColor
            )
          }
        }
      }
    }
  }
}

@Composable
fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String,
  textColor: Color,
  primaryColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Icona
    CircularIconBackground(
      icon = icon,
      backgroundColor = primaryColor.copy(alpha = 0.1f),
      iconTint = primaryColor,
      size = 40.dp,
      iconSize = 20.dp
    )

    // Testo
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = label,
        color = textColor.copy(alpha = 0.6f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        color = textColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
      )
    }
  }
}

@Composable
fun RowScope.ImprovedDayHeaderCell(
  date: LocalDate,
  isSelected: Boolean,
  onDateSelected: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  val isToday = CalendarUtils.isToday(date)
  val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

  Column(
    modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(12.dp))
        .background(
          when {
            isSelected -> primaryColor
            isToday -> primaryColor.copy(alpha = 0.15f)
            else -> Color.Transparent
          }
        )
        .clickable { onDateSelected(date) }
        .padding(vertical = 10.dp, horizontal = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = dayOfWeek.uppercase(),
      color = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> grayColor
      },
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = date.dayOfMonth.toString(),
      color = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> textColor
      },
      fontSize = 17.sp,
      fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
    )
  }
}

@Composable
fun ImprovedWeekGridRow(
  hour: Int,
  daysOfWeek: List<LocalDate>,
  events: List<CourseEvent>,
  expandedEventId: Long?,
  onEventClick: (Long) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 70.dp)
        .padding(vertical = 2.dp)
  ) {
    // Colonna orario con design migliorato
    Box(
      modifier = Modifier
          .width(56.dp)
          .fillMaxHeight()
          .padding(end = 12.dp, top = 4.dp),
      contentAlignment = Alignment.TopEnd
    ) {
      Column(
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = String.format("%02d:00", hour),
          color = grayColor,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Celle eventi per ogni giorno con padding
    daysOfWeek.forEach { date ->
      val eventsForHour = events.filter { event ->
        event.startTime.toLocalDate() == date &&
        event.startTime.hour <= hour &&
        event.endTime.hour > hour
      }

      Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 3.dp)
      ) {
        if (eventsForHour.isNotEmpty()) {
          eventsForHour.firstOrNull()?.let { event ->
            ImprovedEventGridCell(
              event = event,
              isExpanded = expandedEventId == event.id,
              onClick = { onEventClick(event.id) },
              textColor = textColor,
              grayColor = grayColor,
              primaryColor = primaryColor,
              backgroundColor = backgroundColor
            )
          }
        } else {
          // Cella vuota con bordo sottile
          Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                  width = 0.5.dp,
                  color = grayColor.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp)
                )
                .background(
                  color = backgroundColor,
                  shape = RoundedCornerShape(8.dp)
                )
          )
        }
      }
    }
  }
}

@Composable
fun ImprovedEventGridCell(
  event: CourseEvent,
  isExpanded: Boolean,
  onClick: () -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

  Surface(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(),
    shape = RoundedCornerShape(8.dp),
    color = eventColor,
    shadowElevation = if (isExpanded) 4.dp else 1.dp,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Titolo sempre visibile
      Text(
        text = event.courseName,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
        lineHeight = 14.sp,
        overflow = TextOverflow.Ellipsis
      )

      // Informazioni base sempre visibili
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AccessTime,
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.9f),
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = "${CalendarUtils.formatTime(event.startTime.hour, event.startTime.minute)}-${CalendarUtils.formatTime(event.endTime.hour, event.endTime.minute)}",
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
      }

      // Aula sempre visibile se disponibile
      event.room?.let { room ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = room,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Dettagli espansi
      if (isExpanded) {
        HorizontalDivider(
          color = Color.White.copy(alpha = 0.3f),
          modifier = Modifier.padding(vertical = 4.dp)
        )

        // Professore
        event.professor?.let { professor ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = professor,
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Edificio
        event.building?.let { building ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Apartment,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = building,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 11.sp
            )
          }
        }

        // Codice corso
        event.courseCode?.let { code ->
          Text(
            text = code,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        // Badge tipo evento
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.White.copy(alpha = 0.2f),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Text(
            text = stringResource(CalendarUtils.getEventTypeStringRes(event.eventType)),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

@Composable
fun RowScope.DayHeaderCell(
  date: LocalDate,
  isSelected: Boolean,
  onDateSelected: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  val isToday = CalendarUtils.isToday(date)
  val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

  Column(
    modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(8.dp))
        .background(
          when {
            isSelected -> primaryColor
            isToday -> primaryColor.copy(alpha = 0.2f)
            else -> Color.Transparent
          }
        )
        .clickable { onDateSelected(date) }
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = dayOfWeek.uppercase(),
      color = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> grayColor
      },
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = date.dayOfMonth.toString(),
      color = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> textColor
      },
      fontSize = 16.sp,
      fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
fun WeekGridRow(
  hour: Int,
  daysOfWeek: List<LocalDate>,
  events: List<CourseEvent>,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
  ) {
    // Colonna orario
    Box(
      modifier = Modifier
          .width(48.dp)
          .fillMaxHeight()
          .padding(end = 8.dp),
      contentAlignment = Alignment.TopEnd
    ) {
      Text(
        text = String.format("%02d", hour),
        color = grayColor,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    // Celle eventi per ogni giorno
    daysOfWeek.forEach { date ->
      val eventsForHour = events.filter { event ->
        event.startTime.toLocalDate() == date &&
        event.startTime.hour <= hour &&
        event.endTime.hour > hour
      }

      Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
      ) {
        if (eventsForHour.isNotEmpty()) {
          eventsForHour.firstOrNull()?.let { event ->
            EventGridCell(
              event = event,
              textColor = textColor,
              primaryColor = primaryColor
            )
          }
        } else {
          // Cella vuota con bordo
          Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                  color = grayColor.copy(alpha = 0.05f),
                  shape = RoundedCornerShape(4.dp)
                )
          )
        }
      }
    }
  }
}

@Composable
fun EventGridCell(
  event: CourseEvent,
  textColor: Color,
  primaryColor: Color
) {
  val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

  Surface(
    modifier = Modifier.fillMaxSize(),
    shape = RoundedCornerShape(6.dp),
    color = eventColor,
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(6.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = event.courseName.take(15) + if (event.courseName.length > 15) "..." else "",
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        lineHeight = 12.sp
      )
      event.room?.let { room ->
        Text(
          text = room,
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 9.sp,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
fun MonthCalendar(
  currentMonth: YearMonth,
  selectedDate: LocalDate,
  onDateSelected: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  val locale = Locale.getDefault()
  val daysOfWeek = remember {
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    (0..6).map<Int, DayOfWeek> { offset -> firstDayOfWeek.plus(offset.toLong()) }
  }

  Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
  ) {
    // Intestazione giorni della settimana
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      daysOfWeek.forEach { dayOfWeek ->
        Text(
          text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(),
          color = grayColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Griglia dei giorni
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val daysOffset = (firstDayOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val totalCells = daysOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      for (row in 0 until rows) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          for (col in 0..6) {
            val cellIndex = row * 7 + col
            val dayOfMonth = cellIndex - daysOffset + 1

            if (dayOfMonth in 1..daysInMonth) {
              val date = currentMonth.atDay(dayOfMonth)
              val isSelected = date == selectedDate
              val isToday = CalendarUtils.isToday(date)

              DayCell(
                day = dayOfMonth,
                isSelected = isSelected,
                isToday = isToday,
                onClick = { onDateSelected(date) },
                textColor = textColor,
                grayColor = grayColor,
                primaryColor = primaryColor,
                backgroundColor = backgroundColor
              )
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

@Composable
fun RowScope.DayCell(
  day: Int,
  isSelected: Boolean,
  isToday: Boolean,
  onClick: () -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  backgroundColor: Color
) {
  Box(
    modifier = Modifier
        .weight(1f)
        .aspectRatio(1f)
        .padding(2.dp)
        .clip(CircleShape)
        .background(
          when {
            isSelected -> primaryColor
            isToday -> primaryColor.copy(alpha = 0.2f)
            else -> Color.Transparent
          }
        )
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = day.toString(),
      color = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> textColor
      },
      fontSize = 14.sp,
      fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
fun EventsList(
  events: List<CourseEvent>,
  isLoading: Boolean,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
  ) {
    Text(
      text = stringResource(R.string.calendar_events),
      color = primaryColor,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    when {
      isLoading -> {
        Box(
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = primaryColor
          )
        }
      }

      events.isEmpty() -> {
        Box(
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.EventBusy,
              contentDescription = null,
              tint = grayColor,
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = stringResource(R.string.calendar_no_events),
              color = grayColor,
              fontSize = 16.sp
            )
          }
        }
      }

      else -> {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(events) { event ->
            EventCard(
              event = event,
              textColor = textColor,
              grayColor = grayColor,
              primaryColor = primaryColor
            )
          }
        }
      }
    }
  }
}

@Composable
fun EventCard(
  event: CourseEvent,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color
) {
  val timeFormatter = CalendarUtils.timeFormatter

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = primaryColor.copy(alpha = 0.1f),
    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Barra colorata laterale
      Box(
        modifier = Modifier
            .width(4.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(primaryColor)
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Nome corso
        Text(
          text = event.courseName,
          color = textColor,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )

        // Orario
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = grayColor,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}",
            color = grayColor,
            fontSize = 14.sp
          )
        }

        // Aula e edificio
        if (event.room != null || event.building != null) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = grayColor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = buildString {
                if (event.room != null) append(event.room)
                if (event.building != null) {
                  if (event.room != null) append(" - ")
                  append(event.building)
                }
              },
              color = grayColor,
              fontSize = 14.sp
            )
          }
        }

        // Professore
        if (event.professor != null) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = grayColor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = event.professor,
              color = grayColor,
              fontSize = 14.sp
            )
          }
        }
      }

      // Icona tipo evento
      Icon(
        imageVector = when (event.eventType) {
          EventType.LECTURE -> Icons.Outlined.School
          EventType.LAB -> Icons.Outlined.Science
          EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
          EventType.OFFICE_HOURS -> Icons.Outlined.People
          EventType.OTHER -> Icons.Outlined.Event
        },
        contentDescription = null,
        tint = primaryColor,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
