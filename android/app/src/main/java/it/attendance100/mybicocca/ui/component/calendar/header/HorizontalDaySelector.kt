package it.attendance100.mybicocca.ui.component.calendar.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewMode
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

/**
 * Selettore orizzontale dei giorni della settimana con pager.
 */
@Composable
fun HorizontalDaySelector(
    pagerState: PagerState,
    selectedDate: LocalDate,
    referenceDate: LocalDate,
    viewMode: CalendarViewMode,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(CalendarConfig.Dimensions.DAY_SELECTOR_HEIGHT)
    ) { page ->
        val weekOffset = page - CalendarConfig.Pager.INITIAL_PAGE_OFFSET
        val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())

        WeekDaysRow(
            weekStart = weekStart,
            selectedDate = selectedDate,
            viewMode = viewMode,
            onDateSelected = onDateSelected,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor
        )
    }
}

@Composable
private fun WeekDaysRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (viewMode == CalendarViewMode.WEEK) {
            Spacer(modifier = Modifier.width(CalendarConfig.Dimensions.TIME_COLUMN_WIDTH))
        }

        (0..6).forEach { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val isSelected = date == selectedDate
            val isToday = CalendarUtils.isToday(date)
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DaySelectorItem(
                    date = date,
                    dayName = dayName,
                    isSelected = isSelected,
                    isToday = isToday,
                    onDateSelected = onDateSelected,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun DaySelectorItem(
    date: LocalDate,
    dayName: String,
    isSelected: Boolean,
    isToday: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 7.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) primaryColor else Color.Transparent)
            .clickable { onDateSelected(date) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName.take(3).uppercase(),
            color = when {
                isSelected -> Color.White.copy(alpha = 0.8f)
                isToday -> primaryColor
                else -> grayColor
            },
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                isToday -> primaryColor
                else -> textColor
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (isToday && !isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
        }
    }
}
