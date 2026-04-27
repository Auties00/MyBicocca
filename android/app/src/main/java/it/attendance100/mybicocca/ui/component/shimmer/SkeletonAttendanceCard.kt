package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.ui.component.card.SimpleCard

/**
 * Skeleton for AttendanceScreen cards.
 * Layout: Course name + code + progress bar + 2 rows of 2 info items.
 */
@Composable
fun SkeletonAttendanceCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Column {
            ShimmerBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.4f).height(12.dp))
            Spacer(Modifier.height(12.dp))
            ShimmerBox(Modifier.fillMaxWidth().height(4.dp), shape = RoundedCornerShape(2.dp))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SkeletonAttendanceInfo(Modifier.weight(1f))
                SkeletonAttendanceInfo(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SkeletonAttendanceInfo(Modifier.weight(1f))
                SkeletonAttendanceInfo(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SkeletonAttendanceInfo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ShimmerBox(Modifier.size(14.dp), shape = RoundedCornerShape(3.dp))
        Spacer(Modifier.width(6.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.7f).height(12.dp))
    }
}
