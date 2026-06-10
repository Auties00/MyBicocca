package it.attendance100.mybicocca.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * Solid placeholder block for skeleton layouts. Carries no animation of its own: wrap a group
 * in `Modifier.shimmer(rememberShimmer(...))` so the sweep animates across all of them
 * coherently rather than each block animating on its own.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/** Circular counterpart of ShimmerBox, for avatar/badge placeholders; same group-shimmer contract. */
@Composable
fun ShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
