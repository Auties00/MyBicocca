package it.attendance100.mybicocca.core.os

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.R
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

private const val ANDROID_RES_NAMESPACE = "http://schemas.android.com/apk/res/android"

/**
 * The set of locales the app ships translations for, together with the one to fall back to. Read
 * from `res/xml/locales_config.xml` — the same `android:localeConfig` the system uses for the
 * per-app language picker — so it stays a single source of truth: adding a `<locale>` there is
 * all it takes to teach the whole app about a new language.
 */
private data class SupportedLocales(val tags: List<String>, val default: String?)

/** The device's primary system locale. */
fun systemPrimaryLocale(context: Context): Locale? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).systemLocales.get(0)
    } else {
        Resources.getSystem().configuration.locales.get(0)
    }

/**
 * The [Locale] the UI is currently rendered in, tracking per-app language changes. Prefer this
 * over reading `LocalConfiguration.current.locales` directly at each call site — it keeps the
 * "which locale drives the UI" decision (and its fallback) in one place.
 */
@Composable
@ReadOnlyComposable
fun currentLocale(): Locale =
    LocalConfiguration.current.locales.get(0) ?: LocalLocale.current.platformLocale

/** The BCP-47 language tags the app is translated into, in declaration order. */
fun supportedAppLanguages(context: Context): List<String> = readSupportedLocales(context).tags

/** Whether [language] (ISO code or BCP-47 tag) is one of the languages the app ships a translation for. */
fun isAppLanguageSupported(context: Context, language: String?): Boolean {
    if (language.isNullOrBlank()) return false
    val target = Locale.forLanguageTag(language).language
    return supportedAppLanguages(context).any { Locale.forLanguageTag(it).language == target }
}

/**
 * The supported app language that best matches the device's system locale, or the app's default
 * when the system language isn't one the app ships. Matching is by language, so a system locale
 * of `en-US` still resolves to a supported `en`.
 */
fun systemAppLanguage(context: Context): String {
    val supported = readSupportedLocales(context)
    val systemLanguage = systemPrimaryLocale(context)?.language
    return supported.tags.firstOrNull { Locale.forLanguageTag(it).language == systemLanguage }
        ?: supported.default
        ?: supported.tags.firstOrNull()
        ?: Locale.getDefault().language
}

/**
 * Records and applies the user's language choice: a language tag pins the app to it, null goes
 * back to following the system. The flag is what tells startup whether to re-apply the system
 * language, so the two are always written together.
 */
fun selectAppLanguage(context: Context, languageTag: String?) {
    context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_FOLLOWS_SYSTEM, languageTag == null) }
    applyAppLanguage(context, languageTag ?: systemAppLanguage(context))
}

/** Whether the app follows the system language rather than one the user picked. */
fun followsSystemLanguage(context: Context): Boolean =
    context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE).getBoolean(KEY_FOLLOWS_SYSTEM, true)

private const val LANGUAGE_PREFS = "language_prefs"
private const val KEY_FOLLOWS_SYSTEM = "is_system"

/** Applies [languageTags] as the app's locale. */
fun applyAppLanguage(context: Context, languageTags: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(languageTags)
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTags))
    }
}

private fun readSupportedLocales(context: Context): SupportedLocales {
    val tags = mutableListOf<String>()
    var default: String? = null
    val parser = context.resources.getXml(R.xml.locales_config)
    parser.use { parser ->
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    // android:defaultLocale is optional (Android 14+)
                    "locale-config" -> default =
                        parser.getAttributeValue(ANDROID_RES_NAMESPACE, "defaultLocale")
                            ?.takeIf { it.isNotBlank() }

                    "locale" -> parser.getAttributeValue(ANDROID_RES_NAMESPACE, "name")
                        ?.takeIf { it.isNotBlank() }
                        ?.let(tags::add)
                }
            }
            event = parser.next()
        }
    }
    return SupportedLocales(tags, default)
}
