package it.attendance100.mybicocca.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.model.EventType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility object per funzioni e costanti riutilizzabili nel calendario
 */
object CalendarUtils {
  
  // Costanti per colori eventi
  private const val COLOR_LECTURE = 0xFF8B5CF6      // Viola
  private const val COLOR_LAB = 0xFF06B6D4          // Cyan
  private const val COLOR_EXAM = 0xFFEF4444         // Rosso
  private const val COLOR_OFFICE_HOURS = 0xFFF59E0B // Amber
  
  // Colore brand Bicocca
  const val BICOCCA_BRAND_COLOR = "#9C0C35"
  
  // Range ID per dati di test (1001-9999 riservati per mock data)
  const val TEST_EVENT_ID_START = 1001L
  const val TEST_EVENT_ID_END = 9999L
  
  // Costanti UI per il calendario
  val HOUR_SLOT_HEIGHT = 80.dp       // Altezza di uno slot orario nella vista griglia
  val WEEK_START_HOUR = 8            // Ora di inizio visualizzazione settimana
  val WEEK_END_HOUR = 20             // Ora di fine visualizzazione settimana
  val WEEK_TOTAL_HOURS = WEEK_END_HOUR - WEEK_START_HOUR // 12 ore totali

  
  /**
   * Formattatore per orari (HH:mm)
   */
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  
  /**
   * Formattatore per date complete (EEEE d MMMM yyyy)
   */
  fun fullDateFormatter(locale: Locale = Locale.ITALIAN): DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)
  
  /**
   * Formattatore per mese e anno (MMMM yyyy)
   */
  fun monthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM", locale)
  
  /**
   * Restituisce il colore associato a un tipo di evento
   */
  fun getEventColor(eventType: EventType, primaryColor: Color): Color {
    return when (eventType) {
      EventType.LECTURE -> Color(COLOR_LECTURE)
      EventType.LAB -> Color(COLOR_LAB)
      EventType.EXAM -> Color(COLOR_EXAM)
      EventType.OFFICE_HOURS -> Color(COLOR_OFFICE_HOURS)
      EventType.OTHER -> primaryColor
    }
  }
  
  /**
   * Restituisce l'ID della risorsa string per un tipo di evento
   */
  fun getEventTypeStringRes(eventType: EventType): Int {
    return when (eventType) {
      EventType.LECTURE -> it.attendance100.mybicocca.R.string.event_type_lecture
      EventType.LAB -> it.attendance100.mybicocca.R.string.event_type_lab
      EventType.EXAM -> it.attendance100.mybicocca.R.string.event_type_exam
      EventType.OFFICE_HOURS -> it.attendance100.mybicocca.R.string.event_type_office_hours
      EventType.OTHER -> it.attendance100.mybicocca.R.string.event_type_other
    }
  }
  
  /**
   * Verifica se una data è oggi
   */
  fun isToday(date: LocalDate): Boolean = date == LocalDate.now()
  
  /**
   * Formatta un orario nel formato HH:mm
   */
  fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
  }
}

/**
 * Composable riutilizzabile per icone circolari con background
 */
@Composable
fun CircularIconBackground(
  icon: ImageVector,
  backgroundColor: Color,
  iconTint: Color,
  size: Dp = 40.dp,
  iconSize: Dp = 20.dp,
  contentDescription: String? = null
) {
  Box(
    modifier = Modifier
      .size(size)
      .clip(CircleShape)
      .background(backgroundColor),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Composable per indicatore "oggi" (pallino)
 */
@Composable
fun TodayIndicator(
  color: Color,
  size: Dp = 4.dp
) {
  Box(
    modifier = Modifier
      .size(size)
      .clip(CircleShape)
      .background(color)
  )
}
