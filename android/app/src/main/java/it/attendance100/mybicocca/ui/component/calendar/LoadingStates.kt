package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.*

/**
 * Stato di caricamento generico per il calendario.
 * Usato da DayTimelineView, WeekGridView, MonthGridView.
 */
@Composable
fun CalendarLoadingState(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = colorScheme.primary
        )
    }
}

/**
 * Skeleton card singola per evento in caricamento.
 */
@Composable
fun SkeletonEventCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val skeletonColor = colorScheme.outlineVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .shimmer(shimmerInstance),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color bar skeleton
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(skeletonColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(skeletonColor)
                )

                // Time skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(skeletonColor)
                )

                // Location skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(skeletonColor)
                )
            }
        }
    }
}

/**
 * Lista di skeleton cards in caricamento.
 */
@Composable
fun SkeletonLoadingList(
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SkeletonEventCard(shimmerInstance = shimmerInstance)
        }
    }
}
