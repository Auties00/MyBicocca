package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun toPercentage(value: Float): String = "${(value * 100).roundToInt()}%"

// Renders the oversized faded percentage behind the content; Offscreen compositing keeps the
// low-alpha blend from compounding against the dither texture underneath.
private fun Modifier.screenBlend(alpha: Float): Modifier = graphicsLayer {
    this.alpha = alpha
    compositingStrategy = CompositingStrategy.Offscreen
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ProgressStatCard(
    modifier: Modifier = Modifier,
    title: String,
    current: Int,
    total: Int,
    primaryColor: Color? = null,
    textColor: Color,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    progressbar: Boolean = false,
    backgroundProgressBar: Boolean = false,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    var primaryColor by remember { mutableStateOf(primaryColor) }
    if (primaryColor == null) {
        primaryColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
    }

    val targetProgress = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val progress by animateFloatAsState(
        targetValue = if (isLoading) 0f else targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "Progress Animation",
    )
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(-22f) }
    val rotationDef = -22f
    val rotDelta = -4f

    val outerRadius = 16

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = {
            onClick?.invoke()
            scope.launch {
                launch {
                    scale.animateTo(1.1f, tween(150))
                    scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
                }
                launch {
                    rotation.animateTo(rotationDef - rotDelta, tween(100))
                    rotation.animateTo(rotationDef + rotDelta, tween(100))
                    rotation.animateTo(rotationDef, spring(dampingRatio = 0.4f, stiffness = 500f))
                }
            }
        },
        shape = RoundedCornerShape(outerRadius.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val padding = 6
            val innerRadius = outerRadius - padding
            if (backgroundProgressBar) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(padding.dp)
                        .clip(RoundedCornerShape(innerRadius.dp)),
                ) {
                    if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(primaryColor!!),
                        ) {}
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(
                    text = toPercentage(progress),
                    modifier = Modifier
                        .wrapContentWidth(unbounded = true)
                        .offset(x = 13.dp, y = (15 - 5).dp)
                        .scale(scale.value)
                        .rotate(rotation.value)
                        .screenBlend(0.2f),
                    color = Color.Gray,
                    fontSize = 80.sp,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    fontFamily = FontFamily(
                        Font(
                            R.font.bebas_neue_regular,
                            variationSettings = FontVariation.Settings(FontVariation.weight(900)),
                        ),
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .padding(outerRadius.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    color = secondaryColor,
                    fontSize = 12.sp,
                    maxLines = 2,
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer()
                            .background(textColor.copy(alpha = 0.2f)),
                    )
                } else {
                    Text(
                        text = "$current/$total",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (progressbar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(secondaryColor.copy(alpha = 0.2f)),
                    ) {
                        if (!isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor!!),
                            )
                        }
                    }
                }
            }
        }
    }
}
