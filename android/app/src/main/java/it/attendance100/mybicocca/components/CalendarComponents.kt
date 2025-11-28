package it.attendance100.mybicocca.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*
import java.time.*
import java.time.format.*
import java.util.*

/**
 * Componente riutilizzabile per mostrare una singola cella giorno
 */
@Composable
fun RowScope.DayCell(
  date: LocalDate,
  isSelected: Boolean,
  onDateSelected: (LocalDate) -> Unit,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  modifier: Modifier = Modifier
) {
  val isToday = CalendarUtils.isToday(date)
  val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

  Column(
    modifier = modifier
        .weight(1f)
        .clip(RoundedCornerShape(12.dp))
        .background(
          when {
            isSelected -> primaryColor
            isToday -> primaryColor.copy(alpha = 0.2f)
            else -> Color.Transparent
          }
        )
        .clickable { onDateSelected(date) }
        .padding(vertical = 10.dp),
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
      fontSize = 17.sp,
      fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
    )
  }
}

/**
 * Card riutilizzabile per mostrare un evento nella lista
 */
@Composable
fun EventListCard(
  event: CourseEvent,
  textColor: Color,
  grayColor: Color,
  primaryColor: Color,
  onClick: (() -> Unit)? = null
) {
  val timeFormatter = CalendarUtils.timeFormatter

  Surface(
    modifier = Modifier
        .fillMaxWidth()
        .then(
          if (onClick != null) Modifier.clickable(onClick = onClick)
          else Modifier
        ),
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
            .background(CalendarUtils.getEventColor(event.eventType, primaryColor))
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
    }
  }
}

/**
 * Badge riutilizzabile per il tipo di evento
 */
@Composable
fun EventTypeBadge(
  event: CourseEvent,
  modifier: Modifier = Modifier,
  textColor: Color = Color.White,
  backgroundColor: Color? = null
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = backgroundColor ?: Color.White.copy(alpha = 0.25f)
  ) {
    Text(
      text = stringResource(CalendarUtils.getEventTypeStringRes(event.eventType)),
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

/**
 * Cella compatta per mostrare un evento nella griglia settimanale
 */
@Composable
fun CompactEventCell(
  event: CourseEvent,
  primaryColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

  Surface(
    modifier = modifier.fillMaxSize(),
    shape = RoundedCornerShape(6.dp),
    color = eventColor,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(6.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Nome corso abbreviato
      Text(
        text = event.courseName.split(" ").take(2).joinToString(" "),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        lineHeight = 12.sp,
        overflow = TextOverflow.Ellipsis
      )

      // Info minime
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        // Orario
        Row(
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(10.dp)
          )
          Text(
            text = CalendarUtils.formatTime(event.startTime.hour, event.startTime.minute),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // Aula
        event.room?.let { room ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(10.dp)
            )
            Text(
              text = room,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 9.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

/**
 * Cella vuota per la griglia calendario
 */
@Composable
fun EmptyGridCell(
  grayColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
        .fillMaxSize()
        .border(
          width = 0.5.dp,
          color = grayColor.copy(alpha = 0.1f),
          shape = RoundedCornerShape(6.dp)
        )
  )
}

/**
 * Loading indicator riutilizzabile
 */
@Composable
fun CalendarLoadingIndicator(
  primaryColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(40.dp),
      strokeWidth = 3.dp,
      color = primaryColor
    )
  }
}
