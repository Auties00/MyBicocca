package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.ui.component.card.SimpleCard

/**
 * Skeleton for the ExamResults summary stats card.
 * Layout: Title + 2 rows of 2 stat items each.
 */
@Composable
fun SkeletonStatsCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(Modifier.width(100.dp).height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SkeletonStatItem(Modifier.weight(1f))
                SkeletonStatItem(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SkeletonStatItem(Modifier.weight(1f))
                SkeletonStatItem(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SkeletonStatItem(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ShimmerBox(Modifier.width(50.dp).height(22.dp))
        ShimmerBox(Modifier.width(80.dp).height(12.dp))
    }
}
