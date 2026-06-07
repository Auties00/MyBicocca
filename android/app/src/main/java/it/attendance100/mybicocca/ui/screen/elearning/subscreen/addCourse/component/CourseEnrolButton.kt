package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus

// Circular enrol affordance shared by the catalog and search rows. The four states cross-fade
// in place: idle "+", in-progress spinner, enrolled = the app's MorphKnob check badge (same as
// the language picker), failed "!" (tap to retry). An already enrolled course stays tappable for
// haptic feedback but never re-fires a request (the ViewModel short-circuits it), so the only
// state that ignores taps is the in-flight spinner.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseEnrolButton(
    status: EnrolmentStatus,
    accent: Color,
    onEnrol: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val enabled = status != EnrolmentStatus.InProgress
    Box(
        modifier = modifier
            .size(38.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { haptic.tap(); onEnrol() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = status,
            transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
            label = "enrolStatus",
        ) { state ->
            when (state) {
                // Same "joined" badge as the language settings modal.
                EnrolmentStatus.Enrolled -> MorphKnob(checked = true, uncheckedIcon = null)

                else -> StatusCircle(state = state, accent = accent)
            }
        }
    }
}

@Composable
private fun StatusCircle(state: EnrolmentStatus, accent: Color) {
    val scheme = MaterialTheme.colorScheme
    val container = if (state == EnrolmentStatus.Failed) scheme.errorContainer else accent
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            EnrolmentStatus.Idle -> Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Iscriviti",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )

            EnrolmentStatus.InProgress -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )

            EnrolmentStatus.Failed -> Icon(
                imageVector = Icons.Outlined.PriorityHigh,
                contentDescription = "Riprova",
                tint = scheme.onErrorContainer,
                modifier = Modifier.size(18.dp),
            )

            EnrolmentStatus.Enrolled -> Unit
        }
    }
}
