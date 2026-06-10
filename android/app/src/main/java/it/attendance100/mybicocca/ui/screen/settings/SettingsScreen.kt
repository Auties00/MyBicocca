package it.attendance100.mybicocca.ui.screen.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.screen.settings.component.SettingsEntrySection
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntry
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntryGroup
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationsSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.LanguageSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.currentAppLanguageLabel
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearanceSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecuritySheet

/**
 * Landing page of the settings feature: a scrollable directory grouped into connected segmented
 * cards, mirroring the Registry tab's service directory style. Everything is handled in place —
 * Aspetto, Lingua, Sicurezza, Apertura file and About each open a modal bottom sheet over the
 * directory — so the screen has no sub-routes. Licenze hands off to the play-services
 * [OssLicensesMenuActivity], which lists the bundled open-source libraries; the Privacy Policy
 * entry is a placeholder that only acknowledges the tap with haptic feedback. The Lingua tile's
 * subtitle shows the language the app is running with, re-read whenever its sheet closes (a
 * language change also recreates the activity).
 */
@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen() {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = rememberHapticManager()

    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showSecuritySheet by remember { mutableStateOf(false) }
    var showFileAssocSheet by remember { mutableStateOf(false) }
    var showAppInfoSheet by remember { mutableStateOf(false) }

    val languageLabel = remember(showLanguageSheet) { currentAppLanguageLabel(context) }

    val sections = listOf(
        Triple(
            SettingsEntryGroup(
                name = "Preferenze",
                caption = "Personalizza la tua esperienza",
                entries = listOf(
                    SettingsEntry("appearance", "Aspetto", "Tema e colori dell'app", Icons.Outlined.Palette, onClick = { showAppearanceSheet = true }),
                    SettingsEntry("language", "Lingua", languageLabel, Icons.Outlined.Translate, onClick = { showLanguageSheet = true }),
                    SettingsEntry(
                        "app_lock",
                        "Sicurezza",
                        "Blocco app e privacy",
                        Icons.Outlined.Lock,
                        onClick = { showSecuritySheet = true }),
                    SettingsEntry(
                        "file_open",
                        "Apertura file",
                        "App predefinite per tipo di file",
                        Icons.Outlined.FileOpen,
                        onClick = { showFileAssocSheet = true }),
                ),
            ),
            scheme.primaryContainer, scheme.onPrimaryContainer,
        ),
        Triple(
            SettingsEntryGroup(
                name = "Informazioni",
                caption = "About MyBicocca",
                entries = listOf(
                    SettingsEntry(
                        "about",
                        "About",
                        "Versione, copyright e crediti",
                        Icons.Outlined.Info,
                        onClick = { showAppInfoSheet = true }),
                    SettingsEntry(
                        "privacy",
                        "Privacy Policy",
                        "Informativa sulla privacy",
                        Icons.Outlined.PrivacyTip,
                        onClick = { haptic.tap() }),
                    SettingsEntry(
                        "license",
                        "Licenze",
                        "Licenze open source delle librerie",
                        Icons.Outlined.Description,
                        onClick = {
                        OssLicensesMenuActivity.setActivityTitle("Licenze open source")
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    }),
                ),
            ),
            scheme.secondaryContainer, scheme.onSecondaryContainer,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        sections.forEach { (group, container, onContainer) ->
            SettingsEntrySection(
                group = group,
                accentContainer = container,
                accentOnContainer = onContainer,
            )
        }
    }

    if (showAppearanceSheet) {
        SettingsAppearanceSheet(onDismiss = { showAppearanceSheet = false })
    }

    if (showLanguageSheet) {
        LanguageSheet(onDismiss = { showLanguageSheet = false })
    }

    if (showSecuritySheet) {
        SettingsSecuritySheet(onDismiss = { showSecuritySheet = false })
    }

    if (showFileAssocSheet) {
        FileAssociationsSheet(onDismiss = { showFileAssocSheet = false })
    }

    if (showAppInfoSheet) {
        AppInfoSheet(onDismiss = { showAppInfoSheet = false })
    }
}
