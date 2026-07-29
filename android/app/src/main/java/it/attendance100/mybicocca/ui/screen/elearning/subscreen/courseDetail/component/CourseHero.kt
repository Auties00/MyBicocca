package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContinuePlayable
import it.attendance100.mybicocca.ui.screen.elearning.theme.HeroShapeTriple
import kotlin.math.floor

/**
 * Expanded header of the course detail page: year/period label, the course title set as an
 * oversized two-tone headline (light italic head, brand-black tail), an instructor line, and
 * the continue-watching card, over three per-course organic accent shapes.
 *
 * The shapes spin continuously while [isLoading] (the hero doubles as the page's loading
 * indicator) and, driven by [scrollFraction] read inside graphicsLayer blocks, rotate, fade
 * and translate downward as the header collapses — a parallax that costs no recomposition.
 * The continue-watching card fades out on the same scroll curve. A minimum height keeps the
 * background shapes inside the hero bounds during loading, when the text column is too short
 * on its own to reach the shapes' resting offsets. The instructor/continue-watching block
 * animates in only once the course has sections.
 */
@Composable
fun CourseHero(
    details: CourseDetails?,
    continueWatching: ContinuePlayable?,
    shapes: HeroShapeTriple,
    isLoading: Boolean,
    onResume: () -> Unit,
    onGoToLesson: () -> Unit,
    modifier: Modifier = Modifier,
    scrollFraction: () -> Float = { 0f },
) {
    val scheme = MaterialTheme.colorScheme
    val enrolled = details?.enrolled

    val rotationLarge = rememberShapeRotation(isLoading, periodMs = 8_000, direction = 1f)
    val rotationMedium = rememberShapeRotation(isLoading, periodMs = 5_500, direction = -1f)
    val rotationSmall = rememberShapeRotation(isLoading, periodMs = 3_500, direction = 1f)
    val easing = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp)
            .heightIn(min = 280.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-10).dp)
                .size(240.dp)
                .graphicsLayer {
                    val sf = scrollFraction()
                    alpha = 0.22f * (0.7f + 0.3f * (1 - sf.coerceIn(0f, 1f)))
                    rotationZ = rotationLarge + sf * 40f
                    translationY += sf * 500f
                }
                .background(scheme.tertiary, shapes.large),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-40).dp, y = 60.dp)
                .size(160.dp)
                .graphicsLayer {
                    val sf = scrollFraction()
                    alpha = 0.18f * (0.7f + 0.3f * (1 - sf.coerceIn(0f, 1f)))
                    rotationZ = rotationMedium + sf * -55f
                    translationY += sf * 420f
                }
                .background(scheme.primary, shapes.medium),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-24).dp, y = 60.dp)
                .size(74.dp)
                .graphicsLayer {
                    val sf = scrollFraction()
                    alpha = 0.28f * (0.7f + 0.3f * (1 - sf.coerceIn(0f, 1f)))
                    rotationZ = rotationSmall + sf * 75f
                    translationY += sf * 570f
                }
                .background(scheme.secondary, shapes.small),
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            if (enrolled != null) {
                val yearLabel = enrolled.courseCode().periodLabel
                if (!yearLabel.isNullOrBlank()) {
                    Text(
                        text = yearLabel,
                        color = scheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = splitTitle(enrolled.fullName, scheme),
                    fontSize = 46.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Black,
                )
            }

            AnimatedVisibility(
                visible = details?.sections?.isNotEmpty() == true,
                enter = fadeIn(tween(400, delayMillis = 150)) +
                        expandVertically(tween(500, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(250)),
            ) {
                Column {
                    details?.let { instructorLine(it) }?.let { line ->
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    if (continueWatching != null) {
                        Spacer(Modifier.height(22.dp))

                        ContinueWatchingCard(
                            item = continueWatching,
                            onResume = onResume,
                            onGoToLesson = onGoToLesson,
                            modifier = Modifier.graphicsLayer {
                                alpha = easing.transform((1 - scrollFraction()).coerceIn(0f, 1f))
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Spins the hero shape continuously while loading, then carries it forward to the next
 * upright (multiple of 360°) rather than snapping back — avoids a visible reverse spin.
 */
@Composable
private fun rememberShapeRotation(
    isLoading: Boolean,
    periodMs: Int,
    direction: Float,
): Float {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f * direction,
                    animationSpec = tween(periodMs, easing = LinearEasing),
                )
            }
        } else {
            val current = rotation.value
            val target = if (direction >= 0f) {
                (floor(current / 360f) + 1f) * 360f
            } else {
                (floor(current / 360f)) * 360f
            }
            rotation.animateTo(target, tween(700, easing = FastOutSlowInEasing))
            rotation.snapTo(0f)
        }
    }
    return rotation.value
}

private fun splitTitle(title: String, scheme: ColorScheme): AnnotatedString {
    val trimmed = title.trim()
    val splitIndex = trimmed.lastIndexOf(' ')
    return if (splitIndex <= 0) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Black)) {
                append(trimmed)
            }
        }
    } else {
        val head = trimmed.substring(0, splitIndex)
        val tail = trimmed.substring(splitIndex + 1)
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    color = scheme.onSurface,
                ),
            ) {
                append(head)
            }
            append('\n')
            withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Black)) {
                append(tail)
            }
        }
    }
}

@Composable
private fun instructorLine(details: CourseDetails): String? {
    val primary = details.staff.firstOrNull { it.role == CourseStaffRole.Docente }
        ?: details.staff.firstOrNull()
    return primary?.fullName?.let { stringResource(R.string.b3_course_hero_prof_prefix, it) }
}
