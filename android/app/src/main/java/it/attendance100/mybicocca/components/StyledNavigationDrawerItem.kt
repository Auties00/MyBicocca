package it.attendance100.mybicocca.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*


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