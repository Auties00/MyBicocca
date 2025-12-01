package it.attendance100.mybicocca.components

import android.content.res.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*


@Composable
fun AppTitle(
  modifier: Modifier = Modifier,
) {
  val preferencesManager = rememberPreferencesManager()
  val isDarkMode = preferencesManager.isDarkMode ?: isSystemInDarkTheme()

  Column(
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(if (isDarkMode) R.drawable.dark_text else R.drawable.text),
      contentDescription = stringResource(R.string.app_logo),
      modifier = modifier.size(dimensionResource(id = R.dimen.launcher_icon_size))
    )
  }
}

@Preview(backgroundColor = 0xFF0D0D0D, showBackground = true, showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun AppTitleDarkPreview() {
  MaterialTheme(
    colorScheme = MyBicoccaDarkColorScheme
  ) {
    Box(modifier = Modifier.padding(8.dp)) {
      AppTitle()
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
@Composable
private fun AppTitleLightPreview() {
  MaterialTheme(
    colorScheme = lightColorScheme()
  ) {
    Box(modifier = Modifier.padding(8.dp)) {
      AppTitle()
    }
  }
}