package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R

/**
 * The reveal behind a profile card while it is swiped left. The trash-style icon grows in
 * as the card is dragged, then springs to full size with a slight overshoot once the commit
 * threshold is crossed — at which point the zone turns vivid and a "Rimuovi" label slides
 * in. Driven by primitive booleans because [SwipeToRemoveBox] tracks its own gesture state.
 */
@Composable
fun RemoveSwipeBackground(
    armed: Boolean,
    revealed: Boolean,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme

    val container by animateColorAsState(
        targetValue = if (armed) scheme.error else scheme.errorContainer,
        animationSpec = motion.fastEffectsSpec(),
        label = "swipe-container",
    )
    val content = if (armed) scheme.onError else scheme.onErrorContainer
    val iconScale by animateFloatAsState(
        targetValue = when {
            armed -> 1f
            revealed -> 0.7f
            else -> 0.4f
        },
        animationSpec = spring(
            dampingRatio = 0.42f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "swipe-icon",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(if (revealed) container else Color.Transparent),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier = Modifier.padding(end = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedVisibility(
                visible = armed,
                enter = fadeIn(motion.fastEffectsSpec()) + expandHorizontally(motion.fastSpatialSpec()),
                exit = fadeOut(motion.fastEffectsSpec()) + shrinkHorizontally(motion.fastSpatialSpec()),
            ) {
                Text(
                    text = stringResource(R.string.account_switcher_remove),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = content,
                )
            }
            Icon(
                imageVector = Icons.Outlined.PersonRemove,
                contentDescription = stringResource(R.string.account_switcher_remove_account),
                tint = content,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
        }
    }
}
