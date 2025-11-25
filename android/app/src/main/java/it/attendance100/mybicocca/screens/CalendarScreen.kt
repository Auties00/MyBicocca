package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.hilt.navigation.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.EventDetailDialog
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*

enum class CalendarViewMode {
  LIST,    // List view with days
  WEEK     // Weekly grid view
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
  
  // Settimana visualizzata (può essere diversa da selectedDate in modalità WEEK)
  var displayedWeekStart by remember { mutableStateOf(selectedDate.with(DayOfWeek.MONDAY)) }
  
  // Counter per forzare il refresh del pager quando si preme "Oggi"
  var todayPressCount by remember { mutableIntStateOf(0) }
  
  // Stato per il dialog dell'evento selezionato
  var selectedEvent by remember { mutableStateOf<CourseEvent?>(null) }
  
  // Sincronizza displayedWeekStart quando cambia selectedDate (es. click su "Oggi" o selezione giorno)
  LaunchedEffect(selectedDate) {
    displayedWeekStart = selectedDate.with(DayOfWeek.MONDAY)
  }

  // Contenuto principale
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
      onToday = { 
        viewModel.goToToday()
        todayPressCount++
      },
      textColor = textColor,
      grayColor = grayColor,
      primaryColor = primaryColor
    )

    // Selettore giorni (sempre visibile)
    HorizontalDaySelector(
      selectedDate = selectedDate,
      currentMonth = currentMonth,
      viewMode = viewMode,
      todayPressCount = todayPressCount,
      onDateSelected = { date -> viewModel.selectDate(date) },
      onWeekChanged = { weekStartDate ->
        // Aggiorna il mese quando si scorre di settimana
        val weekMonth = YearMonth.from(weekStartDate)
        if (currentMonth != weekMonth) {
          viewModel.setCurrentMonth(weekMonth)
        }
        // Aggiorna la settimana visualizzata (per la griglia)
        displayedWeekStart = weekStartDate
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
            primaryColor = primaryColor,
            onEventClick = { event -> selectedEvent = event }
          )
        }
        CalendarViewMode.WEEK -> {
          WeekEventsGrid(
            displayedWeekStart = displayedWeekStart,
            events = eventsForCurrentMonth,
            isLoading = isLoading,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            onEventClick = { event -> selectedEvent = event }
          )
        }
      }
    }
  }
  
  // Dialog evento (Material 3 gestisce automaticamente scrim e blocco interazioni)
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
  todayPressCount: Int,
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

  // Sincronizza il pager quando cambia selectedDate o si preme "Oggi"
  LaunchedEffect(selectedDate, todayPressCount) {
    val weeksBetween = ChronoUnit.WEEKS.between(
      referenceDate.with(DayOfWeek.MONDAY),
      selectedDate.with(DayOfWeek.MONDAY)
    )
    val targetPage = 1000 + weeksBetween.toInt()
    if (pagerState.currentPage != targetPage) {
      pagerState.scrollToPage(targetPage)
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
  displayedWeekStart: LocalDate,
  events: List<CourseEvent>,
  isLoading: Boolean,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  onEventClick: (CourseEvent) -> Unit
) {
  // Calcola i giorni della settimana visualizzata
  val daysOfWeek = (0..6).map { displayedWeekStart.plusDays(it.toLong()) }

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
          onEventClick = onEventClick,
          primaryColor = primaryColor
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

@Composable
fun EventsList(
  events: List<CourseEvent>,
  isLoading: Boolean,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  onEventClick: (CourseEvent) -> Unit
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
              primaryColor = primaryColor,
              onClick = { onEventClick(event) }
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
  primaryColor: Color,
  onClick: () -> Unit = {}
) {
  val timeFormatter = CalendarUtils.timeFormatter
  val cardBackgroundColor = MaterialTheme.colorScheme.surface

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = cardBackgroundColor,
    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
    onClick = onClick
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

@Composable
private fun TodayIndicator(color: Color, size: Dp = 4.dp) {
  Box(
    modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(color)
  )
}
