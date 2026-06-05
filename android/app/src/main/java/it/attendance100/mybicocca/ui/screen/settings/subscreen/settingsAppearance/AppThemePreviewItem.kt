package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.component.carousel.AutoSlidingCarousel
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.CalendarPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.CalendarPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.ElearningPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.ElearningPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.FoldableBottomNavBar
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.MapsPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.MapsPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.PhoneBottomNavBar
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.RegistryPreviewPhone
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.RegistryPreviewTablet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews.TabletNavRail
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * A miniature app mockup that previews a palette's colors in simple shapes across all screens
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
            // App bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(
                        start = if (deviceType == DeviceType.Tablet) 4.dp else 8.dp,
                        end = 8.dp,
                        top = 8.dp
                    )
                    .offset(y = (-1).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.small,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Search icon button
                        Box(
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                )
                        )
                        // MyBicocca text
                        Row(
                            modifier = Modifier.height(8.dp)
                        ) {
                            // "My" white text
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(12.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = CircleShape,
                                    )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            // "Bicocca" red text
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(27.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(5.dp),
                                    )
                            )
                        }

                        // Profile Picture
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                )
                        )
                    }
                }
            }

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

            // Content blocks
            AutoSlidingCarousel(
                items = previews,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            // Phone/Foldable Bottom Bars
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