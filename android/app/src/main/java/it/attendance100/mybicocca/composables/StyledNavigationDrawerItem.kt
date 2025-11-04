package it.attendance100.mybicocca.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.ui.theme.*


@Composable
fun StyledNavigationDrawerItem(
  icon: ImageVector,
  label: String,
  selected: Boolean = false,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  NavigationDrawerItem(
    icon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) PrimaryColor else grayColor
      )
    },
    label = {
      Text(
        text = label,
        color = if (selected) PrimaryColor else textColor,
        fontSize = 16.sp
      )
    },
    selected = selected,
    onClick = onClick,
    modifier = modifier
        .padding(horizontal = 8.dp)
        .padding(bottom = 4.dp),
    shape = RoundedCornerShape(12.dp),
    colors = NavigationDrawerItemDefaults.colors(
      unselectedContainerColor = grayColor.copy(alpha = 0.05f),
      selectedContainerColor = PrimaryColor.copy(alpha = 0.4f),
      unselectedIconColor = grayColor,
      unselectedTextColor = textColor,
      selectedIconColor = PrimaryColor,
      selectedTextColor = PrimaryColor
    )
  )
}

