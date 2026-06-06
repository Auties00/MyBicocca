package it.attendance100.mybicocca.ui.screen.settings.subscreen.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.agsl.DeformingFlagBox
import it.attendance100.mybicocca.ui.component.agsl.WavingSelectionWrapper
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.flags.FlagFrame
import it.attendance100.mybicocca.ui.component.flags.ItalyFlag
import it.attendance100.mybicocca.ui.component.flags.UkUsaFlag
import it.attendance100.mybicocca.ui.component.flags.WorldFlag
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

private const val LOCALE_SYSTEM = "system"

// Each option pairs the locale with its custom-drawn flag (World / Italy / UK·USA split), shown
// on the tile's leading edge — there is no chip glyph or two-letter mark anymore. Keeping the
// flag here means a new language is a single list entry, with no parallel `when` to update.
private data class LanguageOption(
    val code: String,
    val label: String,
    val flag: @Composable () -> Unit,
)

private val LANGUAGE_OPTIONS = listOf(
    LanguageOption(LOCALE_SYSTEM, "Sistema", flag = { WorldFlag(Modifier.fillMaxSize()) }),
    LanguageOption("it", "Italiano", flag = { ItalyFlag(Modifier.fillMaxSize()) }),
    LanguageOption("en", "English", flag = { UkUsaFlag(Modifier.fillMaxSize()) }),
)

// Label of the language the app is currently running with, for the settings tile subtitle.
fun currentAppLanguageLabel(context: Context): String {
    val current = currentAppLanguage(context)
    return LANGUAGE_OPTIONS.first { it.code == current }.label
}

// Language picker as a modal in the app's expressive language: a connected segmented card of
// neutral tiles — selection lives in the trailing MorphKnob (circle morphing to sunny on the
// motion-scheme springs), not in a container wash, like the Piano di Studi picks. Each tile
// leads with its custom flag, which ripples on the AGSL waving shader the moment it becomes the
// active selection (Android 13+). The locale lands as a configuration change (the activity
// declares locale|layoutDirection), so the app re-localizes in place and the sheet stays open
// on the new selection instead of being torn down. Header text follows the edifici sheet style.
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
                text = "Lingua",
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Lingua dell'app",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LANGUAGE_OPTIONS.forEachIndexed { index, option ->
                    val selected = selectedLocale == option.code
                    SegmentedTile(
                        isFirst = index == 0,
                        isLast = index == LANGUAGE_OPTIONS.lastIndex,
                        title = option.label,
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
                                modifier = Modifier.width(46.dp),
                            )
                        },
                        trailing = {
                            Spacer(Modifier.width(10.dp))
                            MorphKnob(checked = selected, uncheckedIcon = null)
                        },
                    )
                }
            }
        }
    }
}

// The leading flag: a static frame on Android < 13, an AGSL waving ripple that fires once on
// selection from Tiramisu onward. The [width] modifier sets the footprint; the 3:2 FlagFrame
// derives its own height.
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

private fun currentAppLanguage(context: Context): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    } else {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) LOCALE_SYSTEM else locales.get(0)?.language ?: LOCALE_SYSTEM
    }

private fun setAppLanguage(context: Context, code: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (code == LOCALE_SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(
                code
            )
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (code == LOCALE_SYSTEM) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(
                code
            ),
        )
    }
}
