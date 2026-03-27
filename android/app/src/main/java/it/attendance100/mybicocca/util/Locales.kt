package it.attendance100.mybicocca.util

import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import java.util.*

@Composable
fun getCurrentLocale(): Locale {
  return LocalConfiguration.current.locales.get(0)
}
