package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/**
 * Skeleton for PianoCarrieraScreen.
 * Layout: Plan info card + 2 year sections each with header + 3 course cards.
 */
@Composable
fun SkeletonExpandableList(
    modifier: Modifier = Modifier,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(shimmerInstance),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Plan info card skeleton
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(Modifier.width(80.dp).height(12.dp))
                    ShimmerBox(Modifier.width(140.dp).height(16.dp))
                }
                ShimmerBox(Modifier.width(70.dp).height(16.dp))
            }
        }

        // Year sections
        repeat(2) {
            Spacer(Modifier.height(4.dp))
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(Modifier.width(100.dp).height(18.dp))
                ShimmerCircle(size = 28.dp)
            }
            // Course cards
            repeat(3) {
                SkeletonPlannedCourseCard()
            }
        }
    }
}

@Composable
private fun SkeletonPlannedCourseCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                ShimmerBox(Modifier.weight(1f).height(18.dp))
                Spacer(Modifier.width(12.dp))
                ShimmerBox(
                    modifier = Modifier.width(80.dp).height(24.dp),
                    shape = RoundedCornerShape(8.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ShimmerBox(Modifier.width(100.dp).height(14.dp))
                ShimmerBox(Modifier.width(50.dp).height(14.dp))
            }
        }
    }
}
