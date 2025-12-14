package it.attendance100.mybicocca.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*

@Composable
@Preview
private fun OfflineIndicatorPreview() {
  MaterialTheme(
    colorScheme = MyBicoccaDarkColorScheme
  ) {
    StatusIndicator(isOffline = true, isSessionExpired = false)
  }
}

@Composable
@Preview
private fun ExpiredIndicatorPreview() {
  MaterialTheme(
    colorScheme = MyBicoccaDarkColorScheme
  ) {
    StatusIndicator(isOffline = false, isSessionExpired = true)
  }
}

@Composable
@Preview
private fun OfflineExpiredIndicatorPreview() {
  MaterialTheme(
    colorScheme = MyBicoccaDarkColorScheme
  ) {
    StatusIndicator(isOffline = true, isSessionExpired = true)
  }
}

@Composable
fun StatusIndicator(
  isOffline: Boolean,
  isSessionExpired: Boolean,
  modifier: Modifier = Modifier,
) {
  val isVisible = isOffline || isSessionExpired
  val backgroundColor = if (!isOffline) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.secondaryContainer
  val contentColor = if (!isOffline) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer


  AnimatedVisibility(
    visible = isVisible,
    enter = EnterTransition.None,
    exit = shrinkVertically(),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .height(20.dp)
          .background(backgroundColor)
          .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isOffline) {
        Icon(
          imageVector = Icons.Default.CloudOff,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.status_offline),
          color = contentColor,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      } else
        if (isSessionExpired) {
          Icon(
            imageVector = Icons.Default.VpnKeyOff,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.status_session_expired),
            color = contentColor,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
    }
  }
}
