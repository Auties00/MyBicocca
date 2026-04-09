package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.ui.component.card.SimpleCard

@Composable
fun BookedScreen(
    viewModel: BookedViewModel = hiltViewModel(),
) {
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    when {
        bookings.isEmpty() && isRefreshing -> {
            BookedSkeletonList()
        }

        bookings.isEmpty() && !isRefreshing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nessun esame prenotato",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = bookings,
                    key = { it.id },
                ) { booking ->
                    BookedExamCard(booking)
                }
            }
        }
    }
}

@Composable
fun BookedSkeletonList() {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    val skeletonColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(4) {
            SimpleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .shimmer(shimmerInstance),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(skeletonColor),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(28.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(skeletonColor),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(14.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(skeletonColor),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(skeletonColor),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(skeletonColor),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BookedExamCard(
    booking: ExamBooking,
) {
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Header with Course Name and ID
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "${booking.activityName} [${booking.id}]",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Date Time
            Text(
                text = booking.examDate ?: "-",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // Description & Type
            BookedDetailItem(Icons.Outlined.Description, booking.examDate ?: "Esame prenotato")
            BookedDetailItem(
                Icons.AutoMirrored.Outlined.Assignment,
                "Esame prenotato",
            )

            // Booking date
            booking.bookingDate?.let { date ->
                BookedDetailItem(Icons.Outlined.CalendarMonth, "Prenotato il: $date")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reservation number + registration date at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                booking.position?.let { pos ->
                    Text(
                        text = "N. Iscrizione: $pos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                booking.bookingDate?.let { date ->
                    Text(
                        text = "Prenotato il: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Cancel booking */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Cancella")
                }

                Button(
                    onClick = { /* TODO: Print */ },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Stampa")
                }
            }
        }
    }
}


@Composable
fun BookedDetailItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .offset(y = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
