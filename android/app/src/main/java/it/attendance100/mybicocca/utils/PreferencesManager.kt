package it.attendance100.mybicocca.utils

import android.content.*
import androidx.appcompat.app.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.core.content.*


@Suppress("unused")
class PreferencesManager(private val context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_DEVELOPER_MODE = "developer_mode"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LOCALE = "locale"
    private const val KEY_BADGE_PARALLAX = "badge_parallax"
    private const val KEY_BADGE_WHITE = "badge_white"
    private const val KEY_FINGERPRINT_LOGIN = "fingerprint_login"
    private const val KEY_KEEP_LOGGED_IN = "keep_logged_in"
    private const val KEY_SESSION_DURATION = "session_duration"

    const val THEME_SYSTEM_DEFAULT = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val LOCALE_SYSTEM_DEFAULT = "system"
    const val LOCALE_ITALIAN = "it" // Default locale
    const val LOCALE_ENGLISH = "en"

    const val DURATION_30_MIN = 30 * 60 * 1000L
    const val DURATION_1_HOUR = 60 * 60 * 1000L
    const val DURATION_2_HOURS = 2 * 60 * 60 * 1000L
    const val DURATION_6_HOURS = 6 * 60 * 60 * 1000L
    const val DURATION_12_HOURS = 12 * 60 * 60 * 1000L
    const val DURATION_24_HOURS = 24 * 60 * 60 * 1000L
    const val DURATION_7_DAYS = 7 * 24 * 60 * 60 * 1000L
    const val DURATION_FOREVER = -1L
  }

  var isDeveloperMode: Boolean
    get() = prefs.getBoolean(KEY_IS_DEVELOPER_MODE, false)
    set(value) {
      prefs.edit { putBoolean(KEY_IS_DEVELOPER_MODE, value) }
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

  var badgeParallax: Boolean
    get() = prefs.getBoolean(KEY_BADGE_PARALLAX, true)
    set(value) {
      prefs.edit { putBoolean(KEY_BADGE_PARALLAX, value) }
    }

  var badgeWhite: Boolean
    get() = prefs.getBoolean(KEY_BADGE_WHITE, false)
    set(value) {
      prefs.edit { putBoolean(KEY_BADGE_WHITE, value) }
    }

  var fingerprintLogin: Boolean
    get() = prefs.getBoolean(KEY_FINGERPRINT_LOGIN, false)
    set(value) {
      prefs.edit { putBoolean(KEY_FINGERPRINT_LOGIN, value) }
    }

  var keepLoggedIn: Boolean
    get() = prefs.getBoolean(KEY_KEEP_LOGGED_IN, false)
    set(value) {
      prefs.edit { putBoolean(KEY_KEEP_LOGGED_IN, value) }
    }

  var sessionDuration: Long
    get() = prefs.getLong(KEY_SESSION_DURATION, DURATION_30_MIN)
    set(value) {
      prefs.edit { putLong(KEY_SESSION_DURATION, value) }
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

