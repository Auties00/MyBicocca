package it.attendance100.mybicocca.ui.screen.settings.subscreen.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.applyAppLanguage
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.isAppLanguageSupported
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.os.systemAppLanguage
import it.attendance100.mybicocca.core.os.systemPrimaryLocale
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.DeformingFlagBox
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.FlagFrame
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.ItalyFlag
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.UkUsaFlag
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.WavingSelectionWrapper
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component.WorldFlag

private const val LOCALE_SYSTEM = "system"

/**
 * Pairs a locale with its label and the custom-drawn flag (World / Italy / UK·USA split) shown
 * on the tile's leading edge. Keeping the flag here means a new language is a single list
 * entry, with no parallel `when` to update.
 */
private data class LanguageOption(
    val code: String,
    val labelRes: Int,
    val flag: @Composable () -> Unit,
)

private val LANGUAGE_OPTIONS = listOf(
    LanguageOption(
        LOCALE_SYSTEM,
        R.string.settings_language_system,
        flag = { WorldFlag(Modifier.fillMaxSize()) }),
    LanguageOption(
        "it",
        R.string.settings_language_italian,
        flag = { ItalyFlag(Modifier.fillMaxSize()) }),
    LanguageOption(
        "en",
        R.string.settings_language_english,
        flag = { UkUsaFlag(Modifier.fillMaxSize()) }),
)

/** Label of the language the app is currently running with, for the settings tile subtitle. */
fun currentAppLanguageLabel(context: Context): String {
    val current = currentAppLanguage(context)
    val labelRes = when (current) {
        "it" -> R.string.settings_language_italian
        "en" -> R.string.settings_language_english
        else -> R.string.settings_language_system
    }
    return context.getString(labelRes)
}

/**
 * The language picker, shown as a modal bottom sheet in the app's expressive language: a
 * connected segmented card of neutral tiles — selection lives in the trailing [MorphKnob]
 * (circle morphing to sunny on the motion-scheme springs), not in a container wash, like the
 * Piano di Studi picks. Each tile leads with its custom flag, which ripples on the AGSL waving
 * shader the moment it becomes the active selection (Android 13+). The locale lands as a
 * configuration change (the activity declares locale|layoutDirection), so the app re-localizes
 * in place and the sheet stays open on the new selection rather than being torn down. Header
 * text follows the edifici sheet style.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val haptic = rememberHapticManager()
    var selectedLocale by remember { mutableStateOf(currentAppLanguage(context)) }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_language_sheet_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.settings_language_sheet_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            val osLocale = systemPrimaryLocale(context)
            val osLanguage = osLocale?.language
            // An unresolvable system locale is treated as supported, so the "help translate" hint
            // only appears when we positively know the OS language is one the app doesn't ship.
            val isOsLanguageSupported =
                osLanguage == null || isAppLanguageSupported(context, osLanguage)
            val appLocale = currentLocale()
            val osLangName =
                osLocale?.getDisplayLanguage(appLocale)?.replaceFirstChar { it.uppercase() } ?: ""

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LANGUAGE_OPTIONS.forEachIndexed { index, option ->
                    val selected = selectedLocale == option.code

                    val titleStr = stringResource(option.labelRes)
                    val titleAnnotated = if (option.code == LOCALE_SYSTEM) {
                        buildAnnotatedString {
                            append(titleStr)
                            append(" - ")
                            append(osLangName)
                        }
                    } else null

                    val isEnabled =
                        if (option.code == LOCALE_SYSTEM) isOsLanguageSupported else true

                    SegmentedTile(
                        isFirst = index == 0,
                        isLast = index == LANGUAGE_OPTIONS.lastIndex,
                        title = titleStr,
                        titleAnnotated = titleAnnotated,
                        enabled = isEnabled,
                        onClick = {
                            if (selectedLocale != option.code) {
                                selectedLocale = option.code
                                haptic.tap()
                                setAppLanguage(context, option.code)
                            }
                        },
                        leading = {
                            LanguageFlag(
                                option = option,
                                selected = selected,
                                modifier = Modifier
                                    .width(46.dp)
                                    .alpha(if (isEnabled) 1f else 0.38f),
                            )
                        },
                        trailing = {
                            Spacer(Modifier.width(10.dp))
                            MorphKnob(checked = selected, uncheckedIcon = null)
                        },
                    )
                }
            }

            if (!isOsLanguageSupported) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_language_help_translate, osLangName),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * The leading flag: a static frame on Android < 13, an AGSL waving ripple that fires once on
 * selection from Tiramisu onward. The caller's width modifier sets the footprint; the 3:2
 * [FlagFrame] derives its own height.
 */
@Composable
private fun LanguageFlag(
    option: LanguageOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            FlagFrame { option.flag() }
        } else {
            WavingSelectionWrapper(isSelected = selected) { isWaving ->
                DeformingFlagBox(isWaving = isWaving) {
                    FlagFrame { option.flag() }
                }
            }
        }
    }
}

private fun currentAppLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    if (prefs.getBoolean("is_system", true)) {
        return LOCALE_SYSTEM
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        return if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    } else {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    }
}

private fun setAppLanguage(context: Context, code: String) {
    val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    if (code == LOCALE_SYSTEM) {
        prefs.edit { putBoolean("is_system", true) }
        applyAppLanguage(context, systemAppLanguage(context))
    } else {
        prefs.edit { putBoolean("is_system", false) }
        applyAppLanguage(context, code)
    }
}
