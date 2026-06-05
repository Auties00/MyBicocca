package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import it.attendance100.mybicocca.ui.component.text.HtmlBody

// The connected-segment primitives this page borrows from the Piano di Studi compiler:
// surfaceContainer tiles split by 2dp gaps with 20dp outer / 4dp inner corners, circle
// status pills, and the circle-to-sunny knob.

internal fun segmentShape(isFirst: Boolean, isLast: Boolean): RoundedCornerShape {
    val large = 20.dp
    val small = 4.dp
    return RoundedCornerShape(
        topStart = if (isFirst) large else small,
        topEnd = if (isFirst) large else small,
        bottomStart = if (isLast) large else small,
        bottomEnd = if (isLast) large else small,
    )
}

@Composable
internal fun SegmentHeaderTile(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isLast: Boolean = false,
    leadingChip: (@Composable () -> Unit)? = null,
    pill: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentShape(isFirst = true, isLast = isLast),
    ) {
        Row(
            modifier = Modifier.padding(
                start = if (leadingChip != null) 12.dp else 18.dp,
                end = 16.dp,
                top = if (leadingChip != null) 12.dp else 16.dp,
                bottom = if (leadingChip != null) 12.dp else 14.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingChip != null) {
                leadingChip()
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            if (pill != null) {
                Spacer(Modifier.width(8.dp))
                pill()
            }
        }
    }
}

@Composable
internal fun SegmentPill(
    label: String,
    container: Color,
    content: Color,
    icon: ImageVector? = null,
) {
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun SegmentHtmlTile(
    html: String,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentShape(isFirst = false, isLast = isLast),
    ) {
        HtmlBody(
            html = html,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )
    }
}

// The Piano di Studi knob, repurposed as a milestone marker: a circle that morphs into
// the sunny shape once the milestone is behind us.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SegmentKnob(checked: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Sunny) }
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "knobMorph",
    )
    val container by animateColorAsState(
        targetValue = if (checked) scheme.primary else scheme.surfaceContainerHighest,
        animationSpec = motion.defaultEffectsSpec(),
        label = "knobContainer",
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(MorphPolygonShape(morph, progress))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = scheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// Scales the normalized morph path up to the knob's actual size.
private class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val matrix = Matrix()
        matrix.scale(size.width, size.height)
        val path = morph.toPath(progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}
