package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingCourseGroup
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.ExamCardAccent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseCard(
    group: BookingCourseGroup,
    accent: ExamCardAccent,
    today: LocalDate,
    onOpen: (ExamCall) -> Unit,
    modifier: Modifier = Modifier,
    // Deep-link: when set, the card auto-expands once (the caller clears the request after).
    expandRequest: Boolean = false,
    onExpandConsumed: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    var expanded by rememberSaveable(group.courseKey) { mutableStateOf(false) }

    LaunchedEffect(expandRequest) {
        if (expandRequest) {
            expanded = true
            onExpandConsumed()
        }
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "chevronRotation",
    )
    // Expressive interaction feedback: the card morphs from its skewed resting shape
    // to an even one, brightens a surface step, and the accent glyph tips the other
    // way — all on springs.
    val topStart by animateDpAsState(
        targetValue = if (expanded) 24.dp else 32.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "topStart",
    )
    val topEnd by animateDpAsState(
        targetValue = if (expanded) 24.dp else 18.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "topEnd",
    )
    val bottomStart by animateDpAsState(
        targetValue = if (expanded) 24.dp else 18.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "bottomStart",
    )
    val bottomEnd by animateDpAsState(
        targetValue = if (expanded) 24.dp else 32.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "bottomEnd",
    )
    val containerColor by animateColorAsState(
        targetValue = if (expanded) scheme.surfaceContainerHigh else scheme.surfaceContainerLow,
        animationSpec = motion.defaultEffectsSpec(),
        label = "containerColor",
    )
    val accentRotation by animateFloatAsState(
        targetValue = if (expanded) 8f else -10f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "accentRotation",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart,
        ),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        ) {
            HeaderRow(
                group = group,
                accent = accent,
                accentRotation = accentRotation,
                chevronRotation = chevronRotation,
                onToggle = { expanded = !expanded },
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(motion.defaultSpatialSpec()) +
                        fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.defaultSpatialSpec()) +
                        fadeOut(motion.defaultEffectsSpec()),
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    group.calls.forEach { call ->
                        DateItem(
                            call = call,
                            today = today,
                            onClick = { onOpen(call) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeaderRow(
    group: BookingCourseGroup,
    accent: ExamCardAccent,
    accentRotation: Float,
    chevronRotation: Float,
    onToggle: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onToggle,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = accentRotation }
                .clip(accent.shape)
                .background(accent.accent),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.courseTitle,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val parts = buildList {
                add(callCountLabel(group.calls.size))
                group.courseCode?.let { add(it) }
                group.courseOfStudy?.let { add(it) }
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = chevronRotation },
        )
    }
}

private fun callCountLabel(count: Int): String =
    if (count == 1) "1 appello" else "$count appelli"
