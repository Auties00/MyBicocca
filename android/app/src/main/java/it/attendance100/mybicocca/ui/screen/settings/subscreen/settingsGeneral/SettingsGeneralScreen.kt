package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsGeneral

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.ui.screen.settings.component.preference.OpenDialogTile
import it.attendance100.mybicocca.ui.screen.settings.component.preference.SettingsSectionTitle

private const val LOCALE_SYSTEM = "system"
private val LANGUAGE_ENTRIES = mapOf(
    LOCALE_SYSTEM to "Sistema",
    "it" to "Italiano",
    "en" to "English",
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
        OpenDialogTile(
            title = "Lingua dell'app",
            value = selectedLocale,
            entries = LANGUAGE_ENTRIES,
            onValueChange = { code ->
                selectedLocale = code
                setAppLanguage(context, code)
            },
        )
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
