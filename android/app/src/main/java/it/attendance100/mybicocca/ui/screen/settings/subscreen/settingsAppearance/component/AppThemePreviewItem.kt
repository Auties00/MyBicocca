package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.component

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.screen.settings.component.AppBarMock
import it.attendance100.mybicocca.ui.screen.settings.component.CalendarPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.component.CalendarPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.component.ElearningPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.component.ElearningPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.component.FoldableBottomNavBar
import it.attendance100.mybicocca.ui.screen.settings.component.MapsPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.component.MapsPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.component.PhoneBottomNavBar
import it.attendance100.mybicocca.ui.screen.settings.component.RegistryPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.component.RegistryPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.component.TabletNavRail
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * A miniature app mockup that previews a palette's colors in simple shapes across all screens.
 * A device-shaped frame (phone 9:16, foldable square, tablet 16:10 with a nav rail on the left)
 * whose border lights up in the palette's primary when [selected]. Inside, the app-bar mock sits
 * above an auto-sliding carousel cycling the four tab mocks (calendar, e-learning, map,
 * registry) while a second carousel steps the bottom nav bar's — or, on tablet, the rail's —
 * selected pill in step with the content.
 */
@Composable
fun AppThemePreviewItem(
    selected: Boolean,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
) {
    val height = when (deviceType) {
        DeviceType.Phone -> 200.dp
        DeviceType.Foldable -> 250.dp
        DeviceType.Tablet -> 250.dp
    }
    val aspectRatio = when (deviceType) {
        DeviceType.Phone -> 9f / 16f
        DeviceType.Tablet -> 16f / 10f
        DeviceType.Foldable -> 1f
    }

    Row(
        modifier = modifier
            .height(height)
            .aspectRatio(aspectRatio)
            .border(
                width = 4.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else DividerDefaults.color,
                shape = RoundedCornerShape(17.dp),
            )
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (deviceType == DeviceType.Tablet) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp)
                    .width(20.dp),
            ) {
                AutoSlidingCarousel(
                    modifier = Modifier.fillMaxSize(),
                    items = listOf(
                        { TabletNavRail(index = 0) },
                        { TabletNavRail(index = 1) },
                        { TabletNavRail(index = 2) },
                        { TabletNavRail(index = 3) },
                    ),
                    enterAnim = fadeIn(),
                    exitAnim = fadeOut()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
        ) {
            AppBarMock()

            val previews = when (deviceType) {
                DeviceType.Phone -> listOf<@Composable () -> Unit>(
                    { CalendarPreviewPhone() },
                    { ElearningPreviewPhone() },
                    { MapsPreviewPhone() },
                    { RegistryPreviewPhone() },
                )

                DeviceType.Foldable, DeviceType.Tablet -> listOf<@Composable () -> Unit>(
                    { CalendarPreviewTablet() },
                    { ElearningPreviewTablet() },
                    { MapsPreviewTablet() },
                    { RegistryPreviewTablet() },
                )
            }

            AutoSlidingCarousel(
                items = previews,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            val bottomBars = when (deviceType) {
                DeviceType.Phone -> listOf<@Composable () -> Unit>(
                    { PhoneBottomNavBar(index = 0) },
                    { PhoneBottomNavBar(index = 1) },
                    { PhoneBottomNavBar(index = 2) },
                    { PhoneBottomNavBar(index = 3) },
                )

                DeviceType.Foldable -> listOf<@Composable () -> Unit>(
                    { FoldableBottomNavBar(index = 0) },
                    { FoldableBottomNavBar(index = 1) },
                    { FoldableBottomNavBar(index = 2) },
                    { FoldableBottomNavBar(index = 3) },
                )

                DeviceType.Tablet -> emptyList()
            }

            if (bottomBars.isNotEmpty()) {
                AutoSlidingCarousel(
                    items = bottomBars,
                    enterAnim = fadeIn(),
                    exitAnim = fadeOut()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppThemePreviewItemPhonePreview() {
    BicoccaTheme(dark = true) {
        AppThemePreviewItem(
            selected = true,
            deviceType = DeviceType.Phone,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppThemePreviewItemFoldablePreview() {
    BicoccaTheme(dark = true) {
        AppThemePreviewItem(
            selected = false,
            deviceType = DeviceType.Foldable,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppThemePreviewItemTabletPreview() {
    BicoccaTheme(dark = true) {
        AppThemePreviewItem(
            selected = false,
            deviceType = DeviceType.Tablet,
        )
    }
}