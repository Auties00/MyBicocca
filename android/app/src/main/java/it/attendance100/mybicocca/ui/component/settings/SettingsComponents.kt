package it.attendance100.mybicocca.ui.component.settings

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.util.HapticManager
import it.attendance100.mybicocca.util.rememberHapticManager

@Composable
private fun settingsCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.background,
    disabledContainerColor = MaterialTheme.colorScheme.background,
)

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
    subtitleColor: Color,
) {
    Column {
        Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (subtitle != null) {
            Text(text = subtitle, color = subtitleColor, fontSize = 13.sp)
        }
    }
}

@Composable
fun SimpleCategorySettingItem(
    title: String,
    subtitle: String?,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        onClick = { haptic.tap(); onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clip(RoundedCornerShape(borderRadius)),
        colors = settingsCardColors(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (iconComposable != null) iconComposable()
                else if (icon != null) SettingItemIcon(icon, primaryColor)
                SettingItemTexts(title, subtitle, textColor, grayColor)
            }
            if (trailingContent != null) trailingContent()
        }
    }
}

@Composable
fun DialogOpenerSettingItem(
    title: String,
    subtitle: String?,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = grayColor)
        },
        iconComposable = iconComposable,
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
        onClick = { haptic.tap(); onCheckedChange(!checked) },
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clip(RoundedCornerShape(borderRadius)),
        colors = settingsCardColors(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                SettingItemIcon(icon, primaryColor)
                SettingItemTexts(title, subtitle, textColor, grayColor)
            }
            Switch(
                checked = checked,
                onCheckedChange = { haptic.tap(); onCheckedChange(it) },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                ),
                modifier = Modifier.padding(start = 15.dp),
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
            .alpha(if (enabled) 1f else 0.38f),
    ) {
        Card(
            onClick = { haptic.tap(); onSettingsClick() },
            enabled = enabled,
            shape = RoundedCornerShape(topStart = borderRadius, bottomStart = borderRadius),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = borderRadius, bottomStart = borderRadius)),
            colors = settingsCardColors(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            color = grayColor.copy(alpha = 0.2f),
        )
        Card(
            onClick = { haptic.tap(); onCheckedChange(!checked) },
            shape = RoundedCornerShape(topEnd = borderRadius, bottomEnd = borderRadius),
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = borderRadius, bottomEnd = borderRadius)),
            colors = settingsCardColors(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Switch(
                    checked = checked,
                    onCheckedChange = { haptic.tap(); onCheckedChange(it) },
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primaryColor,
                        checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                    ),
                )
            }
        }
    }
}
