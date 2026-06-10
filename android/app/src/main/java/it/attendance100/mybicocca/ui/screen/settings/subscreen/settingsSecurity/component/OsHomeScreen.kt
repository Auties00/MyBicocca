package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Flat theme-tinted launcher home-screen mock: a grid of rounded app-icon placeholders with the
 * MyBicocca logo sitting in one fixed cell, whose measured position is reported through
 * [onLogoPositioned] so the unlock animation can anchor its zoom on the icon. Trailing slots
 * past [AppIconCount] render as empty boxes so the last row keeps its cell widths.
 */
@Composable
fun OsHomeScreen(
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
    onLogoPositioned: (LayoutCoordinates) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val columns = gridColumns(deviceType)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.surfaceContainerLow)
            .padding(HomeScreenPadding),
        verticalArrangement = Arrangement.spacedBy(HomeCellGap),
    ) {
        val rows = (AppIconCount + columns - 1) / columns
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HomeCellGap),
            ) {
                repeat(columns) { col ->
                    val index = row * columns + col
                    val cellModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                    when {
                        index >= AppIconCount -> Box(cellModifier)
                        row == LogoCellRow && col == LogoCellCol ->
                            LogoAppCell(cellModifier.onGloballyPositioned(onLogoPositioned))

                        else -> GenericAppCell(cellModifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenericAppCell(modifier: Modifier = Modifier) {
    Box(
        modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, AppIconShape),
    )
}

@Composable
private fun LogoAppCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface, AppIconShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.4f),
        )
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun OsHomeScreenPhonePreview() {
    BicoccaTheme(dark = true) { OsHomeScreen(DeviceType.Phone) }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200, backgroundColor = PreviewBgDark)
@Composable
private fun OsHomeScreenFoldablePreview() {
    BicoccaTheme(dark = true) { OsHomeScreen(DeviceType.Foldable) }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 175, backgroundColor = PreviewBgDark)
@Composable
private fun OsHomeScreenTabletPreview() {
    BicoccaTheme(dark = true) { OsHomeScreen(DeviceType.Tablet) }
}
