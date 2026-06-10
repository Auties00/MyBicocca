package it.attendance100.mybicocca.core.os

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf


/**
 * Coarse form-factor bucket the UI adapts its layouts to.
 *
 * Derived from the window width size class rather than hardware identity, so the same physical
 * device can land in a different bucket when rotated, resized in multi-window, or unfolded.
 */
enum class DeviceType { Phone, Tablet, Foldable }

/** Buckets a window width size class into a [DeviceType]; unrecognized classes fall back to [DeviceType.Phone]. */
fun getDeviceType(widthSizeClass: WindowWidthSizeClass): DeviceType {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> DeviceType.Phone
        WindowWidthSizeClass.Medium -> DeviceType.Foldable
        WindowWidthSizeClass.Expanded -> DeviceType.Tablet
        else -> DeviceType.Phone
    }
}

/** Installed at the activity root from the current window size class; reading it without a provider throws. */
val LocalDeviceType = staticCompositionLocalOf<DeviceType> {
    error("DeviceType has not been provided. Make sure to wrap your app in a CompositionLocalProvider.")
}