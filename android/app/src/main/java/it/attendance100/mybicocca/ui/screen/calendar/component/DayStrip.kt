package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.calendar.ext.visibleWeekDays
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ItalianLocale = Locale.ITALIAN

@Composable
fun DayStrip(
    weekStart: LocalDate,
    onSelect: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp),
    leadingSpacerWidth: Dp = 32.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val days = remember(weekStart) { visibleWeekDays(weekStart) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (leadingSpacerWidth > 0.dp) {
            Spacer(Modifier.width(leadingSpacerWidth))
        }
        days.forEach { day ->
            val handler = onSelect
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .then(if (handler != null) Modifier.clickable { handler(day) } else Modifier)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = dayShort(day),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

private fun dayShort(day: LocalDate): String =
    day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, ItalianLocale)
        .uppercase(ItalianLocale)
