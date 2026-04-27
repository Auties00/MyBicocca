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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/**
 * Skeleton for ProfileScreen.
 * Layout: Credit card + 2x2 stat grid + dashboard tiles.
 */
@Composable
fun SkeletonProfileContent(
    modifier: Modifier = Modifier,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(shimmerInstance),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Credit card placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f),
            shape = RoundedCornerShape(16.dp),
        )

        // Section title
        ShimmerBox(Modifier.width(120.dp).height(16.dp))

        // 2x2 stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonStatTile(Modifier.weight(1f))
            SkeletonStatTile(Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonStatTile(Modifier.weight(1f))
            SkeletonStatTile(Modifier.weight(1f))
        }

        // Section title
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.width(100.dp).height(16.dp))

        // Dashboard tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(
                modifier = Modifier.weight(2f).height(80.dp),
                shape = RoundedCornerShape(16.dp),
            )
            ShimmerBox(
                modifier = Modifier.weight(1f).height(80.dp),
                shape = RoundedCornerShape(16.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(
                modifier = Modifier.weight(1f).height(80.dp),
                shape = RoundedCornerShape(16.dp),
            )
            ShimmerBox(
                modifier = Modifier.weight(2f).height(80.dp),
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
}

@Composable
private fun SkeletonStatTile(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
    )
}
