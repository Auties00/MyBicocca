package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContinuePlayable

/**
 * Hero card resuming the course's current video: a 16:10 thumbnail (with elapsed/total time
 * pill), title and section subtitle, and an action row pairing a full-width play button
 * ("Riprendi"/"Guarda") with a cookie-shaped go-to-lesson button. The whole card is tappable
 * as a resume shortcut; the clickable Surface overload keeps the tap ripple clipped to the
 * card shape.
 */
@Composable
fun ContinueWatchingCard(
    item: ContinuePlayable,
    onResume: () -> Unit,
    onGoToLesson: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onResume,
        shape = RoundedCornerShape(32.dp),
        color = scheme.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ThumbnailBlock(
                elapsedLabel = item.elapsedLabel,
                totalLabel = item.totalLabel,
                thumbnail = item.thumbnail,
            )
            Column(modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 14.dp, bottom = 4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                )
                if (!item.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                ActionRow(
                    label = if (item.progress > 0f) "Riprendi" else "Guarda",
                    onResume = onResume,
                    onGoToLesson = onGoToLesson,
                )
            }
        }
    }
}

/**
 * The card's thumbnail area, handling the three thumbnail phases: resolving the URL
 * (NotYetLoaded) shows the animated loading state, a resolved URL shows the image once
 * decoded, and "no thumbnail" (Loaded(null)) or a failed load shows the static decorative
 * backdrop.
 *
 * The image painter exists only once a URL is known, but it is then always composed so Coil
 * can resolve the request's target size from the draw scope — gating the Image behind a
 * loaded flag would deadlock (it never draws, so it never loads). It stays transparent until
 * decoded (crossfade), then shows through as the loading state above it fades out; the
 * loading visual is one call site across both phases so its animation never restarts
 * mid-load.
 */
@Composable
private fun ThumbnailBlock(
    elapsedLabel: String?,
    totalLabel: String?,
    thumbnail: Loadable<String?>,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.surfaceContainerHighest),
    ) {
        val url = (thumbnail as? Loadable.Loaded)?.value
        val resolving = thumbnail is Loadable.NotYetLoaded

        val painter = if (url != null) {
            val context = LocalContext.current
            rememberAsyncImagePainter(
                model = remember(url) {
                    ImageRequest.Builder(context)
                        .data(url)
                        .crossfade(true)
                        .build()
                },
            )
        } else {
            null
        }
        val state = painter?.state
        val loaded = state is AsyncImagePainter.State.Success
        val failed = state is AsyncImagePainter.State.Error

        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        val noImage = (thumbnail is Loadable.Loaded && url == null) || failed
        val showLoading = !noImage && (resolving || (url != null && !loaded))
        val loadingAlpha by animateFloatAsState(
            targetValue = if (showLoading) 1f else 0f,
            animationSpec = tween(durationMillis = 450),
            label = "loadingFade",
        )

        if (noImage) {
            DecorativeBackdrop()
        }
        if (loadingAlpha > 0f) {
            ThumbnailLoadingState(modifier = Modifier.graphicsLayer { alpha = loadingAlpha })
        }

        if (elapsedLabel != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "$elapsedLabel / ${totalLabel ?: "—:—"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

/** Static blob decor for the no-thumbnail / failed-load cases — the card's resting look. */
@Composable
private fun DecorativeBackdrop() {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .size(160.dp)
                .graphicsLayer { alpha = 0.10f }
                .background(scheme.primary, OrganicShapes.Burst),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-25).dp, y = 25.dp)
                .size(120.dp)
                .graphicsLayer { alpha = 0.10f }
                .background(scheme.tertiary, OrganicShapes.Puffy),
        )
    }
}

/**
 * Loading state for the thumbnail: the resting blobs come alive — a slow counter-rotating,
 * counter-breathing pair (the breathe value spans 0.92→1.08 and the second blob reads
 * 2 − breathe, so the pair pulses in opposition) — while a soft highlight band sweeps the box
 * diagonally. All driven by one infinite transition so the motion stays in sync and keeps
 * running for as long as the placeholder is shown.
 *
 * The sweep is an endless run of diagonal rays: the gradient tiles along its axis vector
 * (w, h) with two soft rays per tile, and translating the origin by exactly one whole tile
 * vector per cycle lands on a pixel-identical image, so the Restart loop wraps invisibly —
 * rays drift off the bottom-right and the next ones enter from the top-left with no snap, and
 * the sweep reads as one continuous loop with no pause to "reset" on.
 */
@Composable
private fun ThumbnailLoadingState(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "thumbLoading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing)),
        label = "rotation",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe",
    )
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing)),
        label = "shimmer",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .size(170.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = breathe
                    scaleY = breathe
                    alpha = 0.16f
                }
                .background(scheme.primary, OrganicShapes.Burst),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-25).dp, y = 25.dp)
                .size(130.dp)
                .graphicsLayer {
                    rotationZ = -rotation * 0.8f
                    scaleX = 2f - breathe
                    scaleY = 2f - breathe
                    alpha = 0.16f
                }
                .background(scheme.tertiary, OrganicShapes.Puffy),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val tx = shimmer * w
                    val ty = shimmer * h
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.13f to Color.Transparent,
                                0.20f to Color.White.copy(alpha = 0.18f),
                                0.27f to Color.Transparent,
                                0.53f to Color.Transparent,
                                0.60f to Color.White.copy(alpha = 0.18f),
                                0.67f to Color.Transparent,
                                1.00f to Color.Transparent,
                            ),
                            start = Offset(tx - w, ty - h),
                            end = Offset(tx, ty),
                            tileMode = TileMode.Repeated,
                        ),
                    )
                },
        )
    }
}

@Composable
private fun ActionRow(label: String, onResume: () -> Unit, onGoToLesson: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onResume,
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .height(72.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = 0.1.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(OrganicShapes.SmoothCookie6)
                .background(scheme.surfaceContainerHigh)
                .clickable(onClick = onGoToLesson),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SubdirectoryArrowRight,
                contentDescription = stringResource(R.string.continue_watching_go_to_lesson),
                tint = scheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
