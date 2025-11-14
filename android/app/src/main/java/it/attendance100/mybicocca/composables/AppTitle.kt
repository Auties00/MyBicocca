package it.attendance100.mybicocca.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.utils.*


@Composable
fun AppTitle() {
  val preferencesManager = rememberPreferencesManager()
  val isDarkMode = preferencesManager.isDarkMode

  Column(
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(if (isDarkMode) R.drawable.dark_text else R.drawable.text),
      contentDescription = stringResource(R.string.app_logo),
      modifier = Modifier.size(dimensionResource(id = R.dimen.launcher_icon_size))
    )
  }
}