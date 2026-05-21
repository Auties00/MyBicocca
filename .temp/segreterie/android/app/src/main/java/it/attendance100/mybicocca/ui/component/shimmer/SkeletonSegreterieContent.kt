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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/**
 * Skeleton for SegreterieScreen dashboard.
 * Layout: Filter chips + list of long tiles.
 */
@Composable
fun SkeletonSegreterieContent(
    modifier: Modifier = Modifier,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(shimmerInstance),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Filter chip row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(
                modifier = Modifier.width(70.dp).height(32.dp),
                shape = RoundedCornerShape(16.dp),
            )
            ShimmerBox(
                modifier = Modifier.width(90.dp).height(32.dp),
                shape = RoundedCornerShape(16.dp),
            )
            ShimmerBox(
                modifier = Modifier.width(80.dp).height(32.dp),
                shape = RoundedCornerShape(16.dp),
            )
        }

        Spacer(Modifier.height(4.dp))

        // Long tiles
        repeat(6) {
            SkeletonLongTile()
        }
    }
}

@Composable
private fun SkeletonLongTile() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
    )
}
