package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType

/** Height of the mock device frame for each form factor. */
fun frameHeight(deviceType: DeviceType): Dp = when (deviceType) {
    DeviceType.Phone -> 200.dp
    DeviceType.Foldable -> 250.dp
    DeviceType.Tablet -> 250.dp
}

fun frameAspectRatio(deviceType: DeviceType): Float = when (deviceType) {
    DeviceType.Phone -> 9f / 16f
    DeviceType.Tablet -> 16f / 10f
    DeviceType.Foldable -> 1f
}

val FrameBorderShape = RoundedCornerShape(17.dp)
val FrameClipShape = RoundedCornerShape(15.dp)

fun gridColumns(deviceType: DeviceType): Int = when (deviceType) {
    DeviceType.Phone -> 4
    DeviceType.Foldable -> 5
    DeviceType.Tablet -> 7
}

const val AppIconCount = 19

/** Cell that holds the MyBicocca launcher icon */
const val LogoCellRow = 1
const val LogoCellCol = 2

val HomeScreenPadding = 12.dp
val HomeCellGap = 6.dp
val AppIconShape = RoundedCornerShape(30)

const val HomeHoldMillis = 700
const val OpenMillis = 520
const val LandingHoldMillis = 1500
const val CloseMillis = 460
