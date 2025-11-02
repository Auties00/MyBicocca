package it.attendance100.mybicocca.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_MATERIAL_YOU = "material_you"
        private const val KEY_LOCALE = "locale"

        const val LOCALE_SYSTEM_DEFAULT = "system"
        const val LOCALE_ITALIAN = "it" // Default locale
        const val LOCALE_ENGLISH = "en"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Dark Mode defaults to true
        set(value) { prefs.edit() { putBoolean(KEY_DARK_MODE, value) } }

  var isMaterialYou: Boolean
        get() = prefs.getBoolean(KEY_MATERIAL_YOU, false)
        set(value) { prefs.edit() { putBoolean(KEY_MATERIAL_YOU, value) } }

    var locale: String
        get() = prefs.getString(KEY_LOCALE, LOCALE_ITALIAN) ?: LOCALE_ITALIAN
        set(value) { prefs.edit() { putString(KEY_LOCALE, value) } }
}

@Composable
fun rememberPreferencesManager(): PreferencesManager {
    val context = LocalContext.current
    return remember { PreferencesManager(context) }
}

