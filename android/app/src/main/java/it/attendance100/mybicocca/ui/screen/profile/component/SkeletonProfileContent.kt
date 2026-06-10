package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerBox

/**
 * First-load placeholder for the profile body: a section-header bar, a 2x2 grid of stat
 * tiles, and one wide trailing tile. A single window-scoped shimmer instance drives every
 * block so the sweep reads as one coherent pass.
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
        ShimmerBox(
            Modifier
                .width(100.dp)
                .height(16.dp)
        )

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

        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun SkeletonStatTile(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
    )
}
