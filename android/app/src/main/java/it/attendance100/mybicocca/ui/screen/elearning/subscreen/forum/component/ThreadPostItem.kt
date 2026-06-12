package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.forum.PostAttachment
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ThreadNode
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

private val STEP = 18.dp
private val ELBOW_Y = 34.dp
private const val MAX_DEPTH = 5

/**
 * Wraps a [PostCard] with its place in the tree: the indentation gutter (with the
 * Reddit/Apollo-style connector rails, reimagined as soft rounded elbows), and a left-to-right
 * swipe that replies to the post — or, on the user's own post, opens the editor, since editing
 * wins over replying there. Deleted tombstones get no swipe, and neither does offline mode,
 * where composing is pointless; the action chip under the card fades and scales in as the swipe
 * progresses and triggers past a threshold on release.
 */
@Composable
fun ThreadPostItem(
    node: ThreadNode,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenAttachment: (PostAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val post = node.post
    val isOpeningPost = node.depth == 0 && post.parentId == null

    val isOnline = LocalIsOnline.current
    val swipeAction: SwipeAction? = when {
        !isOnline -> null
        post.isDeleted -> null
        post.canEdit -> SwipeAction(Icons.Outlined.Edit, stringResource(R.string.elearning_forum_edit), scheme.tertiary, scheme.onTertiary, onEdit)
        post.canReply -> SwipeAction(Icons.AutoMirrored.Outlined.Reply, stringResource(R.string.elearning_forum_reply), scheme.primary, scheme.onPrimary, onReply)
        else -> null
    }

    val density = LocalDensity.current
    val revealPx = with(density) { 92.dp.toPx() }
    val thresholdPx = with(density) { 58.dp.toPx() }
    val offsetX = remember(post.id.value) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentAction = rememberUpdatedState(swipeAction)

    Box(modifier = modifier.fillMaxWidth()) {
        if (swipeAction != null) {
            val progress = (offsetX.value / revealPx).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = STEP * node.depth.coerceAtMost(MAX_DEPTH)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = swipeAction.container,
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .size(44.dp)
                        .alpha(progress)
                        .scale(0.6f + 0.4f * progress),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = swipeAction.icon,
                            contentDescription = swipeAction.label,
                            tint = swipeAction.onContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        val swipeModifier = if (swipeAction != null) {
            Modifier.pointerInput(post.id.value) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value >= thresholdPx) {
                                currentAction.value?.onTrigger?.invoke()
                            }
                            offsetX.animateTo(0f)
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch { offsetX.snapTo((offsetX.value + dragAmount).coerceIn(0f, revealPx)) }
                }
            }
        } else {
            Modifier
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .then(swipeModifier),
            verticalAlignment = Alignment.Top,
        ) {
            if (node.depth > 0) {
                RailGutter(node = node, color = scheme.outline.copy(alpha = 0.5f))
            }
            PostCard(
                post = post,
                isOpeningPost = isOpeningPost,
                hasChildren = node.hasChildren,
                descendantCount = node.descendantCount,
                collapsed = collapsed,
                onToggleCollapse = onToggleCollapse,
                onReply = onReply,
                onEdit = onEdit,
                onDelete = onDelete,
                onOpenAttachment = onOpenAttachment,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val container: Color,
    val onContainer: Color,
    val onTrigger: () -> Unit,
)

/**
 * Draws the indentation rails: a soft pass-through line for every ancestor column that still has
 * siblings continuing below, and a connector from the immediate parent into this card — a
 * rounded elbow that stops at this row when the node is the last of its siblings, or a
 * straight-through rail with a tee branch when more follow. Visual depth is capped so deep
 * chains stay readable.
 */
@Composable
private fun RailGutter(node: ThreadNode, color: Color) {
    val depth = node.depth.coerceAtMost(MAX_DEPTH)
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(STEP * depth),
    ) {
        val stepPx = STEP.toPx()
        val elbowY = min(ELBOW_Y.toPx(), size.height)
        val stroke = 2.dp.toPx()
        val radius = stepPx * 0.5f
        val connectorX = (depth - 0.5f) * stepPx
        val cardX = depth * stepPx

        node.parentRails.forEachIndexed { column, draw ->
            if (draw && column < depth) {
                val x = (column + 0.5f) * stepPx
                drawLine(color, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), stroke)
            }
        }

        val path = Path()
        if (node.isLastChild) {
            path.moveTo(connectorX, 0f)
            path.lineTo(connectorX, elbowY - radius)
            path.quadraticTo(connectorX, elbowY, connectorX + radius, elbowY)
            path.lineTo(cardX, elbowY)
        } else {
            path.moveTo(connectorX, 0f)
            path.lineTo(connectorX, size.height)
            path.moveTo(connectorX, elbowY)
            path.lineTo(cardX, elbowY)
        }
        drawPath(path, color, style = Stroke(width = stroke))
    }
}
