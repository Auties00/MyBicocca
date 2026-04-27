package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.ui.component.card.SimpleCard

/**
 * Skeleton for TaxesScreen cards.
 * Layout: Header (invoice# + amount) + description + footer (date + status).
 */
@Composable
fun SkeletonTaxCard(
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) {
    SimpleCard(modifier = modifier.shimmer(shimmerInstance)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(Modifier.width(120.dp).height(18.dp))
                ShimmerBox(Modifier.width(70.dp).height(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.8f).height(14.dp))
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ShimmerBox(Modifier.width(60.dp).height(10.dp))
                    ShimmerBox(Modifier.width(80.dp).height(12.dp))
                }
                ShimmerBox(Modifier.width(60.dp).height(12.dp))
            }
        }
    }
}
