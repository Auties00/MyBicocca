package it.attendance100.mybicocca.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.util.HapticManager
import it.attendance100.mybicocca.util.LocalHapticManager
import it.attendance100.mybicocca.util.ProvideHapticManager
import it.attendance100.mybicocca.util.rememberHapticManager

@Composable
private fun SettingItemIcon(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun SettingItemTexts(
    title: String,
    subtitle: String?,
    titleColor: Color,
    subtitleColor: Color
) {
    Column {
        Text(
            text = title,
            color = titleColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun cardColors(): CardColors {
    return CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
    )
}

@Composable
fun SimpleCategorySettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingContent: (@Composable () -> Unit)? = null,
    iconComposable: (@Composable () -> Unit)? = null,
    verticalPadding: Dp = 16.dp,
    enabled: Boolean = true,
    haptic: HapticManager = rememberHapticManager(),
    borderRadius: Dp = 0.dp,
) {
    Card(
        onClick = {
            haptic.tap()
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clip(RoundedCornerShape(borderRadius)),
        // color = MaterialTheme.colorScheme.background
        colors = cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (iconComposable != null) {
                    iconComposable()
                } else if (icon != null) {
                    SettingItemIcon(icon, primaryColor)
                }
                SettingItemTexts(title, subtitle, textColor, grayColor)
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun DialogOpenerSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconComposable: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    borderRadius: Dp = 0.dp,
) {
    SimpleCategorySettingItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        primaryColor = primaryColor,
        textColor = textColor,
        grayColor = grayColor,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = grayColor
            )
        },
        iconComposable = iconComposable,
        verticalPadding = 16.dp,
        enabled = enabled,
        borderRadius = borderRadius,
    )
}

@Composable
fun SimpleSwitchSettingItem(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    haptic: HapticManager = rememberHapticManager(),
    borderRadius: Dp = 0.dp,
) {
    Card(
        onClick = {
            haptic.tap()
            onCheckedChange(!checked)
        },
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clip(RoundedCornerShape(borderRadius)),
        // color = MaterialTheme.colorScheme.background,
        colors = cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                SettingItemIcon(icon, primaryColor)
                SettingItemTexts(title, subtitle, textColor, grayColor)
            }

            Switch(
                checked = checked,
                onCheckedChange = {
                    haptic.tap()
                    onCheckedChange(it)
                },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(start = 15.dp)
            )
        }
    }
}

@Composable
fun AdvancedSwitchSettingItem(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    haptic: HapticManager = rememberHapticManager(),
    borderRadius: Dp = 0.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.background)
            .alpha(if (enabled) 1f else 0.38f)
    ) {
        Card(
            onClick = {
                haptic.tap()
                onSettingsClick()
            },
            enabled = enabled,
            shape = RoundedCornerShape(topStart = borderRadius, bottomStart = borderRadius),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = borderRadius, bottomStart = borderRadius)),
            // color = MaterialTheme.colorScheme.background
            colors = cardColors()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingItemIcon(icon, primaryColor)
                SettingItemTexts(title, subtitle, textColor, grayColor)
            }
        }

        VerticalDivider(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp),
            color = grayColor.copy(alpha = 0.2f)
        )

        Card(
            onClick = {
                haptic.tap()
                onCheckedChange(!checked)
            },
            shape = RoundedCornerShape(topEnd = borderRadius, bottomEnd = borderRadius),
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = borderRadius, bottomEnd = borderRadius)),
            // color = MaterialTheme.colorScheme.background
            colors = cardColors()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        haptic.tap()
                        onCheckedChange(it)
                    },
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primaryColor,
                        checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
@Preview(
    backgroundColor = 0xFF0D0D0D,
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
private fun SimpleCategorySettingItemDarkPreview() {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
        ProvideHapticManager {
            SimpleCategorySettingItem(
                title = "Account Information",
                subtitle = "Manage your personal details",
                icon = Icons.Default.Person,
                onClick = {},
                haptic = LocalHapticManager.current,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
private fun SimpleCategorySettingItemLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        ProvideHapticManager {
            SimpleCategorySettingItem(
                title = "Account Information",
                subtitle = "Manage your personal details",
                icon = Icons.Default.Person,
                onClick = {},
                haptic = LocalHapticManager.current,
            )
        }
    }
}

@Composable
@Preview(
    backgroundColor = 0xFF0D0D0D,
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
private fun DialogOpenerSettingItemDarkPreview() {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
        ProvideHapticManager {
            DialogOpenerSettingItem(
                title = "Theme",
                subtitle = "Select app appearance",
                icon = Icons.Default.ColorLens,
                onClick = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
private fun DialogOpenerSettingItemLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        ProvideHapticManager {
            DialogOpenerSettingItem(
                title = "Theme",
                subtitle = "Select app appearance",
                icon = Icons.Default.ColorLens,
                onClick = {}
            )
        }
    }
}

@Composable
@Suppress("AssignedValueIsNeverRead")
@Preview(
    backgroundColor = 0xFF0D0D0D,
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
private fun SimpleSwitchSettingItemDarkPreview() {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
        ProvideHapticManager {
            var checked by remember { mutableStateOf(true) }
            SimpleSwitchSettingItem(
                title = "Notifications",
                subtitle = "Enable push notifications",
                icon = Icons.Default.Notifications,
                checked = checked,
                onCheckedChange = { checked = it },
                haptic = LocalHapticManager.current,
            )
        }
    }
}

@Composable
@Suppress("AssignedValueIsNeverRead")
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
private fun SimpleSwitchSettingItemLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        ProvideHapticManager {
            var checked by remember { mutableStateOf(false) }
            SimpleSwitchSettingItem(
                title = "Notifications",
                subtitle = "Enable push notifications",
                icon = Icons.Default.Notifications,
                checked = checked,
                onCheckedChange = { checked = it },
                haptic = rememberHapticManager(),
            )
        }
    }
}

@Composable
@Suppress("AssignedValueIsNeverRead")
@Preview(
    backgroundColor = 0xFF0D0D0D,
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
private fun AdvancedSwitchSettingItemDarkPreview() {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
        ProvideHapticManager {
            var checked by remember { mutableStateOf(true) }
            AdvancedSwitchSettingItem(
                title = "Wi-Fi",
                subtitle = "Connected to Home Network",
                icon = Icons.Default.Wifi,
                checked = checked,
                onCheckedChange = { checked = it },
                onSettingsClick = {},
                haptic = LocalHapticManager.current,
            )
        }
    }
}

@Composable
@Suppress("AssignedValueIsNeverRead")
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
private fun AdvancedSwitchSettingItemLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        ProvideHapticManager {
            var checked by remember { mutableStateOf(false) }
            AdvancedSwitchSettingItem(
                title = "Wi-Fi",
                subtitle = "Connected to Home Network",
                icon = Icons.Default.Wifi,
                checked = checked,
                onCheckedChange = { checked = it },
                onSettingsClick = {},
                haptic = LocalHapticManager.current,
            )
        }
    }
}
