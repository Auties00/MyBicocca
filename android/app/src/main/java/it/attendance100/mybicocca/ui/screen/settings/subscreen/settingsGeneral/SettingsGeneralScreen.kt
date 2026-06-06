package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsGeneral

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.agsl.DeformingFlagBox
import it.attendance100.mybicocca.ui.component.agsl.WavingSelectionWrapper
import it.attendance100.mybicocca.ui.component.flags.FlagFrame
import it.attendance100.mybicocca.ui.component.flags.ItalyFlag
import it.attendance100.mybicocca.ui.component.flags.UkUsaFlag
import it.attendance100.mybicocca.ui.component.flags.WorldFlag
import it.attendance100.mybicocca.ui.screen.settings.component.preference.SettingsSectionTitle

private const val LOCALE_SYSTEM = "system"

private data class LanguageOption(val code: String, val label: String)

private val LANGUAGES = listOf(
    LanguageOption(LOCALE_SYSTEM, "Sistema"),
    LanguageOption("it", "Italiano"),
    LanguageOption("en", "English"),
)

@Composable
fun SettingsGeneralScreen() {
    val context = LocalContext.current
    var selectedLocale by remember { mutableStateOf(currentAppLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        SettingsSectionTitle("Lingua")
        LANGUAGES.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { language ->
                    LanguageCell(
                        label = language.label,
                        language = language,
                        selected = selectedLocale == language.code,
                        onClick = {
                            selectedLocale = language.code
                            setAppLanguage(context, language.code)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Lone trailing card
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageCell(
    label: String,
    language: LanguageOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()

    Surface(
        selected = selected,
        onClick = {
            haptic.tap()
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlagPreview(selected = selected, language)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@Suppress("ComposableNaming")
private val LanguageOption.Flag: Unit
    @Composable
    get() = FlagFrame {
        when (code) {
            "it" -> ItalyFlag()
            "en" -> UkUsaFlag()
            else -> WorldFlag()
        }
    }

@Composable
private fun FlagPreview(
    selected: Boolean,
    language: LanguageOption,
    modifier: Modifier = Modifier,
) {
    Box(modifier.padding(8.dp)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            language.Flag
        } else {
            WavingSelectionWrapper(isSelected = selected) { isWaving ->
                DeformingFlagBox(isWaving = isWaving) {
                    language.Flag
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
