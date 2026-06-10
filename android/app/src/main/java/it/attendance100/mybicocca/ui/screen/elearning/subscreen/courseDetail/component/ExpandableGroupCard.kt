package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * The expandable group-card chrome shared by the Contenuti, Quiz and Compiti tabs: ordinal
 * badge that morphs into an expressive polygon on expand, animated corners and chevron, and
 * a body column that only composes while expanded. The number badge twirls as its container
 * morphs from a quiet rounded square into an expressive polygon; the digit itself stays
 * upright because only the backdrop rotates.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableGroupCard(
    ordinal: Int,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme

    val transition = updateTransition(expanded, label = "group-card")
    val cornerDp by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "corner",
    ) { if (it) 28.dp else 20.dp }
    val chevronRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "chevron",
    ) { if (it) 180f else 0f }
    val badgeRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "badge-rotation",
    ) { if (it) 120f else 0f }
    val badgeColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "badge-color",
    ) { if (it) scheme.primary else scheme.secondaryContainer }
    val badgeContentColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "badge-content-color",
    ) { if (it) scheme.onPrimary else scheme.onSecondaryContainer }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }
    val expandedShape = expandedBadgeShape(ordinal)
    val badgeShape = if (expanded) expandedShape else RoundedCornerShape(14.dp)

    Surface(
        shape = RoundedCornerShape(cornerDp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { rotationZ = badgeRotation }
                            .background(badgeColor, badgeShape),
                    )
                    Text(
                        text = ordinal.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content,
                )
            }
        }
    }
}

/** MaterialShapes getters are themselves @Composable, so this mapper has to be one too. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun expandedBadgeShape(ordinal: Int): Shape = when (ordinal % 6) {
    0 -> MaterialShapes.Sunny
    1 -> MaterialShapes.Cookie9Sided
    2 -> MaterialShapes.Clover4Leaf
    3 -> MaterialShapes.Pentagon
    4 -> MaterialShapes.Flower
    else -> MaterialShapes.Cookie6Sided
}.toShape()

/**
 * The signature M3 Expressive grouped-list silhouette: big corners cap the run,
 * small corners knit the inner seams together.
 */
fun stackShape(index: Int, count: Int): RoundedCornerShape {
    val big = 16.dp
    val small = 5.dp
    val top = if (index == 0) big else small
    val bottom = if (index == count - 1) big else small
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}
