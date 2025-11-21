package it.attendance100.mybicocca.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import java.util.*

@Composable
fun getCurrentLocale(): Locale {
  return LocalConfiguration.current.getLocales().get(0)
}