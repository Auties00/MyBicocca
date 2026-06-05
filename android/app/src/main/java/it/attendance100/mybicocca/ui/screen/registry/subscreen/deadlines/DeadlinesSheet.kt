package it.attendance100.mybicocca.ui.screen.registry.subscreen.deadlines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.registry.state.DeadlineUrgency
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryDeadline
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val NextFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)

// Scadenzario: the deadline spine rendered as a vertical timeline inside a modal sheet,
// opened from the Scadenze header. Tapping an entry routes to the owning sub-screen.
@Composable
fun DeadlinesSheet(
    deadlines: List<RegistryDeadline>,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now() }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
        ) {
            Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 8.dp)) {
                Text(
                    text = "Scadenzario",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.9).sp,
                    color = scheme.onSurface,
                )
                Text(
                    text = buildAnnotatedString {
                        if (deadlines.isEmpty()) {
                            append("Nessuna scadenza nei prossimi 30 giorni")
                        } else {
                            withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Bold)) {
                                append(if (deadlines.size == 1) "1 scadenza" else "${deadlines.size} scadenze")
                            }
                            append(" nei prossimi 30 giorni")
                        }
                    },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
            }

            if (deadlines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nessuna scadenza imminente.",
                        fontSize = 14.sp,
                        color = scheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 24.dp),
                ) {
                    deadlines.forEachIndexed { index, deadline ->
                        TimelineEvent(
                            deadline = deadline,
                            today = today,
                            isLast = index == deadlines.lastIndex,
                            onClick = {
                                deadline.onClick()
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEvent(
    deadline: RegistryDeadline,
    today: LocalDate,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    // Node/kicker accent follows the active palette: the primary accent for urgent items, a muted
    // neutral for the rest — so urgent deadlines pop in whichever theme is selected.
    val accent =
        if (deadline.urgency == DeadlineUrgency.Urgent) scheme.primary else scheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        // Date column.
        Column(
            modifier = Modifier
                .width(46.dp)
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "%02d".format(deadline.date.dayOfMonth),
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = scheme.onSurface,
            )
            Text(
                text = deadline.date.format(MonthFormat).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = scheme.onSurfaceVariant,
            )
        }

        // Node rail: continuous connector with a ringed node at the card's top.
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            // For the last event the connector stops at the node rather than trailing off.
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .then(if (isLast) Modifier.height(26.dp) else Modifier.fillMaxHeight())
                    .background(scheme.outlineVariant),
            )
            // Ringed node: soft outer glow, 4.dp accent border, surface centre.
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(scheme.surface),
                    )
                }
            }
        }

        // Card.
        Surface(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, top = 6.dp, bottom = 14.dp),
            shape = RoundedCornerShape(18.dp),
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(start = 15.dp, top = 13.dp, end = 12.dp, bottom = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${deadline.kicker} · ${relativeLabel(today, deadline.date, deadline.byDeadline)}".uppercase(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = deadline.title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    deadline.detail?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun relativeLabel(today: LocalDate, date: LocalDate, byDeadline: Boolean): String {
    val days = ChronoUnit.DAYS.between(today, date)
    return if (byDeadline) {
        // A cutoff to act by.
        when {
            days < 0L -> "Scaduta"
            days == 0L -> "Entro oggi"
            days == 1L -> "Entro 1 giorno"
            else -> "Entro $days giorni"
        }
    } else {
        when {
            days < 0L -> "Scaduta"
            days == 0L -> "Oggi"
            days == 1L -> "Domani"
            else -> "Tra $days giorni"
        }
    }
}

// Formats the soonest deadline for the header summary ("prossima il 2 giu").
fun nextDeadlineLabel(date: LocalDate): String = date.format(NextFormat)
