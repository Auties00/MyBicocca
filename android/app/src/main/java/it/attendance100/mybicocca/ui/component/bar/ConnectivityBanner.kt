package it.attendance100.mybicocca.ui.component.bar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

private val BandContentHeight = 40.dp
private const val RESTORED_LINGER_MS = 1_200L

/**
 * The band fills are fixed status colors rather than scheme roles: error/primary flip to pastel
 * tones in dark schemes, while this band must read as the same saturated red/green signal in
 * both themes. White content on both is deliberate, matching the app-wide convention for
 * content on saturated brand fills.
 */
private val OfflineRed = Color(0xFFB3261E)
private val RestoredGreen = Color(0xFF2E7D32)

/**
 * Full-bleed connectivity band hosted at the very top of AppRoot, wrapping the whole app as
 * [content]. While offline a red band unrolls from under the status bar and pushes the app
 * (each screen's app bar included) down by its label height; when connectivity returns it turns
 * green, lingers for a beat, then retracts back under the status bar — staying green while it
 * retracts rather than flashing back to red.
 *
 * The push works by consuming exactly the share of the status-bar inset the band has covered so
 * far for everything below: inset-aware app bars shrink their own status spacer in lockstep,
 * which turns the band's growth into a clean push instead of a double offset. While the band
 * covers the status bar it also owns the backdrop — system status icons are forced light over
 * the saturated fill (swapped at mid-cover, when the band actually reaches the icons) and the
 * theme rule is handed back once it retracts.
 *
 * Motion: a spatial spring on the way in (the slight overshoot makes the push read as
 * physical), brisk and clean on the way out.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectivityBannerHost(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf(false) }
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            restored = false
            expanded = true
        } else if (expanded && !restored) {
            restored = true
            delay(RESTORED_LINGER_MS)
            expanded = false
        }
    }

    val density = LocalDensity.current
    val statusBarPx = WindowInsets.statusBars.getTop(density)
    val expandedHeightPx = statusBarPx + with(density) { BandContentHeight.roundToPx() }

    val motion = MaterialTheme.motionScheme
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (expanded) motion.defaultSpatialSpec() else motion.fastSpatialSpec(),
        label = "connectivityBandProgress",
    )
    val bandHeightPx = (progress * expandedHeightPx).roundToInt().coerceAtLeast(0)

    val view = LocalView.current
    val dark = LocalDarkTheme.current
    val coversStatusBar = statusBarPx > 0 && bandHeightPx >= statusBarPx / 2
    LaunchedEffect(coversStatusBar, dark) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
            if (coversStatusBar) false else !dark
    }

    Column(modifier.fillMaxSize()) {
        if (bandHeightPx > 0) {
            ConnectivityBand(restored = restored, heightPx = bandHeightPx)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .consumeWindowInsets(WindowInsets(top = min(bandHeightPx, statusBarPx)))
        ) {
            content()
        }
    }
}

/**
 * The band surface itself. Content is bottom-anchored so the label rides the leading edge — it
 * slides out from under the status bar as the band unrolls and tucks back under it on retract.
 * The offline glyph pulses softly (still scanning for a network) and the restored swap slides
 * the label through vertically.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConnectivityBand(restored: Boolean, heightPx: Int) {
    val motion = MaterialTheme.motionScheme
    val bandColor by animateColorAsState(
        targetValue = if (restored) RestoredGreen else OfflineRed,
        animationSpec = motion.defaultEffectsSpec(),
        label = "connectivityBandColor",
    )
    val heightDp = with(LocalDensity.current) { heightPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clipToBounds()
            .background(bandColor),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val pulse = rememberInfiniteTransition(label = "connectivityPulse")
        val pulseAlpha by pulse.animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "connectivityPulseAlpha",
        )
        AnimatedContent(
            targetState = restored,
            transitionSpec = {
                (slideInVertically(motion.defaultSpatialSpec()) { it / 2 } +
                        fadeIn(motion.defaultEffectsSpec()))
                    .togetherWith(
                        slideOutVertically(motion.defaultSpatialSpec()) { -it / 2 } +
                                fadeOut(motion.defaultEffectsSpec())
                    )
            },
            label = "connectivityBandContent",
        ) { online ->
            Row(
                modifier = Modifier.height(BandContentHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (online) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { alpha = if (online) 1f else pulseAlpha },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(if (online) R.string.connectivity_online else R.string.connectivity_offline),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
