package it.attendance100.mybicocca.utils

import android.content.*
import androidx.appcompat.app.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.core.content.*

class PreferencesManager(private val context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LOCALE = "locale"

    const val THEME_SYSTEM_DEFAULT = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val LOCALE_SYSTEM_DEFAULT = "system"
    const val LOCALE_ITALIAN = "it" // Default locale
    const val LOCALE_ENGLISH = "en"
  }

  var themeMode: String
    get() = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM_DEFAULT) ?: THEME_SYSTEM_DEFAULT
    set(value) {
      prefs.edit { putString(KEY_THEME_MODE, value) }
    }

  var isDarkMode: Boolean
    get() = when (themeMode) {
      THEME_DARK -> true
      THEME_LIGHT -> false
      else -> false // System default, return false as fallback
    }
    set(value) {
      themeMode = if (value) THEME_DARK else THEME_LIGHT
    }

  var locale: String
    get() = prefs.getString(KEY_LOCALE, LOCALE_ITALIAN) ?: LOCALE_ITALIAN
    set(value) {
      prefs.edit { putString(KEY_LOCALE, value) }
    }


  /**
   * Applies the stored theme preference to the entire app
   */
  fun applyTheme() {
    AppCompatDelegate.setDefaultNightMode(
      when (themeMode) {
        THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      }
    )
  }
}

@Composable
fun rememberPreferencesManager(): PreferencesManager {
  val context = LocalContext.current
  return remember { PreferencesManager(context) }
}

