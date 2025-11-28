package it.attendance100.mybicocca.utils

import android.content.res.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*


@Composable
fun isTablet(): Boolean {
  val configuration = LocalConfiguration.current
  return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    configuration.screenWidthDp > 840
  } else {
    configuration.screenWidthDp > 600
  }
}
