package it.attendance100.mybicocca.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.ui.theme.MyBicoccaLightColorScheme
import it.attendance100.mybicocca.ui.theme.OnPrimaryColor
import it.attendance100.mybicocca.util.ProvideHapticManager
import it.attendance100.mybicocca.util.rememberHapticManager


@Composable
fun StyledNavigationDrawerItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val grayColor = GrayColor()
    val haptic = rememberHapticManager()

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) OnPrimaryColor else grayColor
            )
        },
        label = {
            Text(
                text = label,
                color = if (selected) OnPrimaryColor else textColor,
                fontSize = 16.sp
            )
        },
        selected = selected,
        onClick = {
            haptic.tap()
            onClick()
        },
        modifier = modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            selectedContainerColor = OnPrimaryColor.copy(alpha = 0.4f),
            unselectedIconColor = grayColor,
            unselectedTextColor = textColor,
            selectedIconColor = OnPrimaryColor,
            selectedBadgeColor = OnPrimaryColor,
            unselectedBadgeColor = textColor,
            selectedTextColor = OnPrimaryColor,
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1a0d0f)
@Composable
private fun StyledNavigationDrawerItemDarkPreview() {
    ProvideHapticManager {
        MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                StyledNavigationDrawerItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    selected = false,
                    onClick = { }
                )
            }
        }
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun StyledNavigationDrawerItemLightPreview() {
    ProvideHapticManager {
        MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                StyledNavigationDrawerItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    selected = false,
                    onClick = { }
                )
            }
        }
    }
}