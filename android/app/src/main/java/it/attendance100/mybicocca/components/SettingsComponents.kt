package it.attendance100.mybicocca.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.utils.*

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
private fun SettingItemTexts(title: String, subtitle: String?, titleColor: Color, subtitleColor: Color) {
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
fun SimpleCategorySettingItem(
  title: String,
  subtitle: String?,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  primaryColor: Color = MaterialTheme.colorScheme.primary,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
  grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  trailingContent: (@Composable () -> Unit)? = null,
  iconComposable: (@Composable () -> Unit)? = null,
  verticalPadding: Dp = 16.dp,
  enabled: Boolean = true,
) {
  val haptic = rememberHapticManager()
  Surface(
    modifier = modifier
        .fillMaxWidth()
        .alpha(if (enabled) 1f else 0.38f)
        .clickable(enabled = enabled) {
          haptic.tap()
          onClick()
        },
    color = MaterialTheme.colorScheme.background
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
        } else {
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
  title: String,
  subtitle: String?,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  primaryColor: Color = MaterialTheme.colorScheme.primary,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
  grayColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  iconComposable: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
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
    enabled = enabled
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
) {
  val haptic = rememberHapticManager()
  Surface(
    modifier = modifier
        .fillMaxWidth()
        .alpha(if (enabled) 1f else 0.38f)
        .clickable(enabled = enabled) {
          haptic.tap()
          onCheckedChange(!checked)
        },
    color = MaterialTheme.colorScheme.background
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
) {
  val haptic = rememberHapticManager()
  Row(
    modifier = modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .alpha(if (enabled) 1f else 0.38f)
  ) {
    Surface(
      modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable(enabled = enabled) {
            haptic.tap()
            onSettingsClick()
          },
      color = MaterialTheme.colorScheme.background
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
          .width(1.dp)
          .fillMaxHeight(),
      color = grayColor.copy(alpha = 0.2f)
    )

    Surface(
      modifier = Modifier
          .fillMaxHeight()
          .clickable(enabled = enabled) {
            haptic.tap()
            onCheckedChange(!checked)
          },
      color = MaterialTheme.colorScheme.background
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
