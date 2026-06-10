package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.component.FingerprintGraphic
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/** ON landing: the app-lock gate — a primary lock icon above the lit fingerprint graphic, centered. */
@Composable
fun LandingPageOn(
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(lockIconSize(deviceType)),
            )
            Spacer(Modifier.height(10.dp))
            FingerprintGraphic(enabled = true, deviceType = deviceType)
        }
    }
}

/** OFF landing: the app's calendar under an app-bar mock */
@Composable
fun LandingPageOff(
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row {
            if (deviceType == DeviceType.Tablet) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                        .width(20.dp),
                ) {
                    TabletNavRail()
                }
            }
            Column(
                modifier = modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(scheme.background),
            ) {
                AppBarMock()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    when (deviceType) {
                        DeviceType.Phone -> CalendarPreviewPhone()
                        DeviceType.Foldable, DeviceType.Tablet -> CalendarPreviewTablet()
                    }
                }
                if (deviceType == DeviceType.Phone) PhoneBottomNavBar()
                if (deviceType == DeviceType.Foldable) FoldableBottomNavBar()
            }
        }
    }

}

/**
 * Compact MyBicocca top app-bar mock: a pill container holding a search-button dot, the
 * two-segment "MyBicocca" wordmark, and a profile-picture circle.
 */
@Composable
fun AppBarMock(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth()
                .background(scheme.surfaceContainerHigh, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(6.dp)
                        .background(scheme.onBackground.copy(alpha = 0.3f), CircleShape),
                )
                Row(modifier = Modifier.height(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .background(scheme.onBackground, CircleShape),
                    )
                    Spacer(Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(27.dp)
                            .background(scheme.primary, RoundedCornerShape(5.dp)),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .background(scheme.onBackground.copy(alpha = 0.3f), CircleShape),
                )
            }
        }
    }
}

private fun lockIconSize(deviceType: DeviceType): Dp = when (deviceType) {
    DeviceType.Phone -> 34.dp
    DeviceType.Foldable -> 40.dp
    DeviceType.Tablet -> 34.dp
}

@Preview(showBackground = true, widthDp = 120, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOnPhonePreview() {
    BicoccaTheme(dark = true) { LandingPageOn(DeviceType.Phone) }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOnFoldablePreview() {
    BicoccaTheme(dark = true) { LandingPageOn(DeviceType.Foldable) }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 175, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOnTabletPreview() {
    BicoccaTheme(dark = true) { LandingPageOn(DeviceType.Tablet) }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOffPhonePreview() {
    BicoccaTheme(dark = true) { LandingPageOff(DeviceType.Phone) }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOffFoldablePreview() {
    BicoccaTheme(dark = true) { LandingPageOff(DeviceType.Foldable) }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 175, backgroundColor = PreviewBgDark)
@Composable
private fun LandingPageOffTabletPreview() {
    BicoccaTheme(dark = true) { LandingPageOff(DeviceType.Tablet) }
}
