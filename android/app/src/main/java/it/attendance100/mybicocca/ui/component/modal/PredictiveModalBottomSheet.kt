package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveModalBottomSheet(
    onDismiss: () -> Unit,
    modalColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    duration: Int = 400,
    sizeDuration: Int = duration,
    content: @Composable (closeTransition: Transition<Boolean>, progress: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val closeSeekableState = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekableState, label = "closeTransition")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = shape,
        containerColor = modalColor,
        scrimColor = scrimColor,
    ) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { backEvent ->
                    closeSeekableState.seekTo(backEvent.progress, targetState = false)
                }
                closeSeekableState.animateTo(false)
                onDismiss()
            } catch (_: CancellationException) {
                closeSeekableState.animateTo(true)
            }
        }

        closeTransition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(durationMillis = duration)),
                    initialContentExit = fadeOut(tween(durationMillis = duration)),
                    sizeTransform = SizeTransform(clip = true) { _, _ ->
                        tween(durationMillis = sizeDuration)
                    },
                )
            },
            contentKey = { it }
        ) { isVisible ->
            if (isVisible) {
                val progress by this@AnimatedContent.transition.animateFloat(
                    transitionSpec = { tween(durationMillis = duration) },
                    label = "progress"
                ) { state ->
                    if (state == EnterExitState.Visible) 0f else 1f
                }

                content(closeTransition, progress)
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                )
            }
        }
    }
}
