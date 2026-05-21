package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val FLIP_THRESHOLD_FRACTION = 0.5f
private const val FLICK_THRESHOLD_FRACTION = 0.05f
private const val FLICK_MAX_DURATION_MS = 100L
private const val PARALLAX_PX = 24f

@Composable
fun BadgeCard(
    account: Account?,
    career: Career?,
    photoFile: File?,
    modifier: Modifier = Modifier,
) {
    var rotationY by remember { mutableFloatStateOf(0f) }
    var animatedRotation by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme
    val flipSpec = remember(motion) { motion.slowSpatialSpec<Float>() }

    LaunchedEffect(rotationY) {
        val animatable = Animatable(animatedRotation)
        animatable.animateTo(targetValue = rotationY, animationSpec = flipSpec) {
            animatedRotation = value
        }
    }

    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(Size.Zero) }

    val isBackVisible by remember {
        derivedStateOf {
            val a = ((animatedRotation % 360f) + 360f) % 360f
            a in 90f..270f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // ID card aspect ratio
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val toggle = if (offset.x < size.width / 2f) -180f else 180f
                        rotationY += toggle
                    },
                )
            }
            .pointerInput(Unit) {
                var dragStartTimeMs = 0L
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragStartTimeMs = System.currentTimeMillis()
                        totalDrag = 0f
                    },
                    onDragEnd = {
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val durationMs = System.currentTimeMillis() - dragStartTimeMs
                        val fraction = abs(totalDrag) / width
                        val flicked = durationMs <= FLICK_MAX_DURATION_MS && fraction >= FLICK_THRESHOLD_FRACTION
                        if (flicked || fraction >= FLIP_THRESHOLD_FRACTION) {
                            rotationY += if (totalDrag >= 0f) 180f else -180f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                        touchOffset = change.position
                    },
                )
            }
            .pointerInput(Unit) {
                // Track touch position (without consuming) for parallax. detectTapGestures
                // and detectHorizontalDragGestures don't expose continuous tracking.
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.firstOrNull()?.let { c -> touchOffset = c.position }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Card body — single graphicsLayer host to flip both faces.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    cardSize = Size(this.size.width, this.size.height)
                    this.rotationY = animatedRotation
                    this.cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(20.dp))
                .background(badgeGradient())
                .drawWithCache {
                    val brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.0f), Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.0f)),
                    )
                    onDrawWithContent {
                        drawContent()
                        // Chromatic-aberration: three color-channel offsets, magnitude driven
                        // by |sin(rotationY)| so the effect peaks mid-flip and damps at rest.
                        val angleRad = Math.toRadians(animatedRotation.toDouble()).toFloat()
                        val offsetMagnitude = abs(sin(angleRad)) * 12f * density
                        if (offsetMagnitude > 0.5f) {
                            drawRect(
                                color = Color(0xFFFF003A).copy(alpha = 0.18f),
                                topLeft = Offset(-offsetMagnitude, 0f),
                                size = this.size,
                                blendMode = androidx.compose.ui.graphics.BlendMode.Plus,
                            )
                            drawRect(
                                color = Color(0xFF00FFB7).copy(alpha = 0.14f),
                                topLeft = Offset(offsetMagnitude * cos(angleRad), 0f),
                                size = this.size,
                                blendMode = androidx.compose.ui.graphics.BlendMode.Plus,
                            )
                            drawRect(
                                color = Color(0xFF1F4DFF).copy(alpha = 0.16f),
                                topLeft = Offset(offsetMagnitude, 0f),
                                size = this.size,
                                blendMode = androidx.compose.ui.graphics.BlendMode.Plus,
                            )
                        }
                        // Subtle highlight stripe — rebuilt only on size change via drawWithCache.
                        drawRect(brush = brush, size = this.size)
                    }
                },
        ) {
            // Front face (visible when 0..90, 270..360)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (isBackVisible) 0f else 1f },
            ) {
                BadgeFront(account = account, career = career, touch = touchOffset, cardSize = cardSize)
            }
            // Back face — counter-flipped so its content reads right-side-up
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isBackVisible) 1f else 0f
                        this.rotationY = 180f
                    },
            ) {
                BadgeBack(account = account, photoFile = photoFile, touch = touchOffset, cardSize = cardSize)
            }
        }
    }

    // Suppress unused-variable warning for scope; reserved for future spring-cancellation work.
    @Suppress("unused") val noop = scope
}

@Composable
private fun BadgeFront(account: Account?, career: Career?, touch: Offset, cardSize: Size) {
    val parallax = computeParallax(touch, cardSize)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "MyBicocca",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = parallax.x * 0.4f
                    translationY = parallax.y * 0.4f
                },
            )
            Text(
                text = "Università degli Studi",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFD4AF37))
                .graphicsLayer {
                    translationX = parallax.x * 0.7f
                    translationY = parallax.y * 0.7f
                },
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = (account?.displayName ?: "—").uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = career?.matricola ?: "",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun BadgeBack(account: Account?, photoFile: File?, touch: Offset, cardSize: Size) {
    val parallax = computeParallax(touch, cardSize)
    Column(modifier = Modifier.fillMaxSize()) {
        // Magnetic stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.Black.copy(alpha = 0.85f)),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = parallax.x * 0.5f
                        translationY = parallax.y * 0.5f
                    },
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Email",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
                Text(
                    text = "${account?.username ?: ""}@campus.unimib.it",
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Codice Fiscale",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
                Text(
                    text = account?.academic?.fiscalCode ?: "—",
                    color = Color.White,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun computeParallax(touch: Offset, cardSize: Size): Offset {
    if (cardSize.width <= 0f || cardSize.height <= 0f) return Offset.Zero
    val nx = (touch.x / cardSize.width).coerceIn(0f, 1f) - 0.5f
    val ny = (touch.y / cardSize.height).coerceIn(0f, 1f) - 0.5f
    return Offset(-nx * PARALLAX_PX, -ny * PARALLAX_PX)
}

@Composable
private fun badgeGradient(): Brush {
    val cs = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(cs.primary, cs.primaryContainer, cs.tertiary),
    )
}

private val Float.dpAsPx: Float
    @Composable
    get() = with(LocalDensity.current) { this@dpAsPx.dp.toPx() }
