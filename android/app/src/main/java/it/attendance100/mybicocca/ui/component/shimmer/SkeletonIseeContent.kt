package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.ui.component.card.SimpleCard

/**
 * Skeleton for IseeScreen.
 * Layout: Credit card placeholder + 2x2 info card grid.
 */
@Composable
fun SkeletonIseeContent(
    modifier: Modifier = Modifier,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(shimmerInstance),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Credit card placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f),
            shape = RoundedCornerShape(16.dp),
        )

        // 2x2 info grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonInfoCard(Modifier.weight(1f))
            SkeletonInfoCard(Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonInfoCard(Modifier.weight(1f))
            SkeletonInfoCard(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SkeletonInfoCard(modifier: Modifier = Modifier) {
    SimpleCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShimmerCircle(size = 48.dp)
            ShimmerBox(Modifier.width(60.dp).height(10.dp))
            ShimmerBox(Modifier.width(80.dp).height(24.dp))
        }
    }
}
