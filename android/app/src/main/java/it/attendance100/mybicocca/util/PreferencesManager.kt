package it.attendance100.mybicocca.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Suppress("unused")
class PreferencesManager @Inject constructor(
  @ApplicationContext context: Context,
) {
  private val prefs: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

  companion object {
    // Auth
    private const val KEY_AUTH_UID = "auth_uid"
    private const val KEY_AUTH_CLIENT = "auth_client"
    private const val KEY_AUTH_TOKEN = "auth_access_token"
    private const val KEY_AUTH_FISCAL_CODE = "auth_fiscal_code"

    private const val KEY_USER_STUDENT_ID = "user_student_id"
    private const val KEY_USER_MATRIC_ID = "user_matric_id"
    private const val KEY_USER_PERSON_ID = "user_person_id"
    private const val KEY_USER_TYPE_TITLE_CODE = "user_type_title_code"
    private const val KEY_SESSION_START_TIME = "session_start_time"
    private const val KEY_AUTH_EXPIRY = "auth_expiry"

    // Private Settings
    private const val KEY_IS_DEVELOPER_MODE = "developer_mode"
    private const val KEY_MAP_EDITOR_ENABLED = "map_editor_enabled"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FINGERPRINT_LOGIN = "fingerprint_login"
    private const val KEY_KEEP_LOGGED_IN = "keep_logged_in"
    private const val KEY_SESSION_DURATION = "session_duration"
    private const val KEY_LOCALE = "locale"
    private const val KEY_BADGE_PARALLAX = "badge_parallax"
    private const val KEY_BADGE_WHITE = "badge_white"
    private const val KEY_PROGRESS_BAR_TOGGLE = "progress_bar_toggle"
    private const val KEY_SWIPE_PROFILE_ENABLED = "swipe_profile_enabled"
    private const val KEY_SWIPE_SEARCH_ENABLED = "swipe_search_enabled"


    // Public Settings
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

  var mapEditorEnabled: Boolean
    get() = prefs.getBoolean(KEY_MAP_EDITOR_ENABLED, false)
    set(value) {
      prefs.edit { putBoolean(KEY_MAP_EDITOR_ENABLED, value) }
    }


  var themeMode: String
    get() = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM_DEFAULT) ?: THEME_SYSTEM_DEFAULT
    set(value) {
      prefs.edit { putString(KEY_THEME_MODE, value) }
    }

  var isDarkMode: Boolean?
    get() = when (themeMode) {
      THEME_DARK -> true
      THEME_LIGHT -> false
      else -> null // System default, will use isSystemInDarkTheme() in the composable function
    }
    set(value) {
      if (value == null) return
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
	  get() = prefs.getBoolean(KEY_BADGE_WHITE, true)
    set(value) {
      prefs.edit { putBoolean(KEY_BADGE_WHITE, value) }
    }

  var progressBarToggle: Boolean
    get() = prefs.getBoolean(KEY_PROGRESS_BAR_TOGGLE, false)
    set(value) {
      prefs.edit { putBoolean(KEY_PROGRESS_BAR_TOGGLE, value) }
    }

  var swipeProfileEnabled: Boolean
    get() = prefs.getBoolean(KEY_SWIPE_PROFILE_ENABLED, true)
    set(value) {
      prefs.edit { putBoolean(KEY_SWIPE_PROFILE_ENABLED, value) }
    }

  var swipeSearchEnabled: Boolean
    get() = prefs.getBoolean(KEY_SWIPE_SEARCH_ENABLED, true)
    set(value) {
      prefs.edit { putBoolean(KEY_SWIPE_SEARCH_ENABLED, value) }
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

  var sessionStartTime: Long
    get() = prefs.getLong(KEY_SESSION_START_TIME, 0L)
    set(value) = prefs.edit { putLong(KEY_SESSION_START_TIME, value) }

  // Authentication Headers
  var authUid: String?
    get() = prefs.getString(KEY_AUTH_UID, null)
    set(value) = prefs.edit { putString(KEY_AUTH_UID, value) }

  var authClient: String?
    get() = prefs.getString(KEY_AUTH_CLIENT, null)
    set(value) = prefs.edit { putString(KEY_AUTH_CLIENT, value) }

  var authAccessToken: String?
    get() = prefs.getString(KEY_AUTH_TOKEN, null)
    set(value) = prefs.edit { putString(KEY_AUTH_TOKEN, value) }

  var authFiscalCode: String?
    get() = prefs.getString(KEY_AUTH_FISCAL_CODE, null)
    set(value) = prefs.edit { putString(KEY_AUTH_FISCAL_CODE, value) }

  var authExpiry: Long
    get() = prefs.getLong(KEY_AUTH_EXPIRY, 0L)
    set(value) = prefs.edit { putLong(KEY_AUTH_EXPIRY, value) }

  // User Info for quick access
  var userStudentId: Int
    get() = prefs.getInt(KEY_USER_STUDENT_ID, -1)
    set(value) = prefs.edit { putInt(KEY_USER_STUDENT_ID, value) }

  var userMatricId: Int
    get() = prefs.getInt(KEY_USER_MATRIC_ID, -1)
    set(value) = prefs.edit { putInt(KEY_USER_MATRIC_ID, value) }

  var userPersonId: Int
    get() = prefs.getInt(KEY_USER_PERSON_ID, -1)
    set(value) = prefs.edit { putInt(KEY_USER_PERSON_ID, value) }

  var userTypeTitleCode: String?
    get() = prefs.getString(KEY_USER_TYPE_TITLE_CODE, null)
    set(value) = prefs.edit { putString(KEY_USER_TYPE_TITLE_CODE, value) }

  fun clearAuth() {
    prefs.edit {
      remove(KEY_AUTH_UID)
      remove(KEY_AUTH_CLIENT)
      remove(KEY_AUTH_TOKEN)
      remove(KEY_AUTH_FISCAL_CODE)
      remove(KEY_USER_STUDENT_ID)
      remove(KEY_USER_MATRIC_ID)
      remove(KEY_USER_PERSON_ID)
      remove(KEY_USER_TYPE_TITLE_CODE)
      remove(KEY_SESSION_START_TIME)
      remove(KEY_AUTH_EXPIRY)
    }
  }

  fun isLoggedIn(): Boolean {
    return !authUid.isNullOrBlank() &&
        !authClient.isNullOrBlank() &&
        !authAccessToken.isNullOrBlank()
  }

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
