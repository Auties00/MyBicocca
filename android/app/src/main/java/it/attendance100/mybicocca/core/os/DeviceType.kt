package it.attendance100.mybicocca.core.os

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf


enum class DeviceType { Phone, Tablet, Foldable }

fun getDeviceType(widthSizeClass: WindowWidthSizeClass): DeviceType {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> DeviceType.Phone
        WindowWidthSizeClass.Medium -> DeviceType.Foldable
        WindowWidthSizeClass.Expanded -> DeviceType.Tablet
        else -> DeviceType.Phone
    }
}

val LocalDeviceType = staticCompositionLocalOf<DeviceType> {
    error("DeviceType has not been provided. Make sure to wrap your app in a CompositionLocalProvider.")
}