package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.unlockpreview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.SysNavBarPreview
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import kotlinx.coroutines.delay

/** Biometric access preview */
@Composable
fun UnlockPreview(
    enabled: Boolean,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
    loop: Boolean = true,
) {
    var frameCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var logoCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val anchor = remember(frameCoords, logoCoords) { resolveAnchor(frameCoords, logoCoords) }

    val progress = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }

    LaunchedEffect(enabled, loop) {
        val open = tween<Float>(durationMillis = OpenMillis, easing = FastOutSlowInEasing)
        val close = tween<Float>(durationMillis = CloseMillis, easing = FastOutSlowInEasing)
        if (loop) {
            while (true) {
                closing = false
                progress.snapTo(0f)
                delay(HomeHoldMillis.toLong())
                progress.animateTo(1f, open)
                delay(LandingHoldMillis.toLong())
                closing = true
                progress.animateTo(0f, close)
            }
        } else {
            closing = false
            progress.snapTo(0f)
            delay(HomeHoldMillis.toLong())
            progress.animateTo(1f, open)
        }
    }

    UnlockPreviewContent(
        enabled = enabled,
        deviceType = deviceType,
        progress = progress.value,
        closing = closing,
        anchor = anchor,
        modifier = modifier,
        onFramePositioned = { frameCoords = it },
        onLogoPositioned = { logoCoords = it },
    )
}

/** Home screen plus the landing scaled by [progress] about [anchor] */
@Composable
private fun UnlockPreviewContent(
    enabled: Boolean,
    deviceType: DeviceType,
    progress: Float,
    anchor: TransformOrigin,
    modifier: Modifier = Modifier,
    closing: Boolean = false,
    onFramePositioned: (LayoutCoordinates) -> Unit = {},
    onLogoPositioned: (LayoutCoordinates) -> Unit = {},
) {
    // Lifts the nav bar only while closing (1f -> 0f), peaking near progress 0.25.
    val navBarOffset = if (closing) navBarLiftOffset(progress) else 0.dp

    Box(
        modifier = modifier
            .height(frameHeight(deviceType))
            .aspectRatio(frameAspectRatio(deviceType))
            .border(4.dp, MaterialTheme.colorScheme.outlineVariant, FrameBorderShape)
            .clip(FrameClipShape)
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned(onFramePositioned),
    ) {
        OsHomeScreen(
            deviceType = deviceType,
            modifier = Modifier.fillMaxSize(),
            onLogoPositioned = onLogoPositioned,
        )

        if (progress > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = anchor
                        scaleX = progress
                        scaleY = progress
                    },
            ) {
                if (enabled) {
                    LandingPageOn(deviceType, Modifier.fillMaxSize())
                } else {
                    LandingPageOff(deviceType, Modifier.fillMaxSize())
                }
            }
        }

        // System navigation bar
        SysNavBarPreview(
            Modifier
                .align(Alignment.BottomCenter)
                .offset(y = navBarOffset),
        )
    }
}

/** Converts the measured launcher icon position into a [TransformOrigin] within the device frame */
private fun resolveAnchor(frame: LayoutCoordinates?, logo: LayoutCoordinates?): TransformOrigin {
    if (frame == null || logo == null || !frame.isAttached || !logo.isAttached) {
        return TransformOrigin.Center
    }
    val w = frame.size.width.toFloat()
    val h = frame.size.height.toFloat()
    if (w <= 0f || h <= 0f) return TransformOrigin.Center
    val center = frame.localPositionOf(
        sourceCoordinates = logo,
        relativeToSource = Offset(logo.size.width / 2f, logo.size.height / 2f),
    )
    return TransformOrigin((center.x / w).coerceIn(0f, 1f), (center.y / h).coerceIn(0f, 1f))
}

private val NavBarLiftMax = 5.dp
private const val NavBarLiftPeak = 0.25f

/** Upward nav-bar lift during the closing phase: 0 at progress 1 and 0, peaking at [NavBarLiftPeak] */
private fun navBarLiftOffset(progress: Float): Dp {
    val t = (if (progress <= NavBarLiftPeak) {
        progress / NavBarLiftPeak
    } else {
        (1f - progress) / (1f - NavBarLiftPeak)
    }).coerceIn(0f, 1f)
    val eased = t * t * (3f - 2f * t) // smoothstep for a soft bump
    return -(NavBarLiftMax * eased)
}

// Approximate launcher-icon anchor for static previews
private val PreviewAnchor = TransformOrigin(0.58f, 0.22f)

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewHomePreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = true,
            deviceType = DeviceType.Phone,
            progress = 0f,
            anchor = PreviewAnchor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewOpeningPreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = true,
            deviceType = DeviceType.Phone,
            progress = 0.45f,
            anchor = PreviewAnchor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewLandedOnPreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = true,
            deviceType = DeviceType.Phone,
            progress = 1f,
            anchor = PreviewAnchor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewLandedOffPreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = false,
            deviceType = DeviceType.Phone,
            progress = 1f,
            anchor = PreviewAnchor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewLandedOnTabletPreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = true,
            deviceType = DeviceType.Tablet,
            progress = 1f,
            anchor = PreviewAnchor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = PreviewBgDark)
@Composable
private fun UnlockPreviewLandedOffFoldablePreview() {
    BicoccaTheme(dark = true) {
        UnlockPreviewContent(
            enabled = false,
            deviceType = DeviceType.Foldable,
            progress = 1f,
            anchor = PreviewAnchor,
        )
    }
}
