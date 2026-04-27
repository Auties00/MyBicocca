package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.ui.component.card.SimpleCard

/**
 * Skeleton card for ExamResults and Questionnaires screens.
 * Layout: [Icon 28dp] [Title + subtitle lines] [Badge]
 */
@Composable
fun SkeletonIconBadgeCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
    subtitleLines: Int = 2,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerCircle(size = 28.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShimmerBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
                repeat(subtitleLines) {
                    ShimmerBox(Modifier.fillMaxWidth(if (it == 0) 0.5f else 0.4f).height(12.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            ShimmerBox(
                modifier = Modifier.width(60.dp).height(24.dp),
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

/**
 * Skeleton card for BookingScreen.
 * Layout: Title + description + info chips row
 */
@Composable
fun SkeletonBookingCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(Modifier.fillMaxWidth(0.8f).height(20.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(
                    modifier = Modifier.width(110.dp).height(28.dp),
                    shape = RoundedCornerShape(20.dp),
                )
                ShimmerBox(
                    modifier = Modifier.width(90.dp).height(28.dp),
                    shape = RoundedCornerShape(20.dp),
                )
            }
            ShimmerBox(Modifier.fillMaxWidth(0.5f).height(12.dp))
        }
    }
}

/**
 * Skeleton card for BookedScreen.
 * Layout: Title + date + divider + detail rows + action buttons
 */
@Composable
fun SkeletonBookedCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.height(220.dp).shimmer(shimmerInstance)) {
        Column {
            ShimmerBox(Modifier.fillMaxWidth(0.7f).height(20.dp))
            Spacer(Modifier.height(12.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.5f).height(28.dp))
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))
            repeat(3) {
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp))
                if (it < 2) Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = MaterialTheme.shapes.medium,
                )
                ShimmerBox(
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }
    }
}

/**
 * Skeleton card for StageScreen.
 * Layout: Title + company row + date row + status badge
 */
@Composable
fun SkeletonStageCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ShimmerBox(Modifier.fillMaxWidth(0.5f).height(18.dp))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(Modifier.size(16.dp), shape = RoundedCornerShape(4.dp))
                Spacer(Modifier.width(6.dp))
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(Modifier.size(16.dp), shape = RoundedCornerShape(4.dp))
                Spacer(Modifier.width(6.dp))
                ShimmerBox(Modifier.fillMaxWidth(0.5f).height(12.dp))
            }
            Spacer(Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier.width(80.dp).height(24.dp),
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

/**
 * Skeleton card for ReservationsScreen.
 * Layout: [Icon 28dp] [Title + date row + status]
 */
@Composable
fun SkeletonReservationCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerCircle(size = 28.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerBox(Modifier.size(14.dp), shape = RoundedCornerShape(3.dp))
                    Spacer(Modifier.width(4.dp))
                    ShimmerBox(Modifier.width(100.dp).height(12.dp))
                }
                ShimmerBox(Modifier.fillMaxWidth(0.4f).height(12.dp))
            }
        }
    }
}

/**
 * Wraps N skeleton cards in a scrollable Column with shimmer instance.
 */
@Composable
fun SkeletonCardList(
    itemCount: Int = 4,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    spacing: Dp = 12.dp,
    cardContent: @Composable (Shimmer) -> Unit,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(itemCount) {
            cardContent(shimmerInstance)
        }
    }
}
