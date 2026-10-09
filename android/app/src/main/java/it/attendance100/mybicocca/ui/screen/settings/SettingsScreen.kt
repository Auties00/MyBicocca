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
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.navigation.LocalAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.settings.component.SettingsEntrySection
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntry
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntryGroup
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.currentAppLanguageLabel

/**
 * Landing page of the settings feature: a scrollable directory grouped into connected segmented
 * cards, mirroring the Registry tab's service directory style. Aspetto, Lingua, Sicurezza,
 * Apertura file, Vibrazione and About each push their [SheetRoute] on the shell navigator, which
 * shows them as modal sheets over the directory (outside the shell, e.g. in a standalone UI test,
 * those taps do nothing). Licenze hands off to the play-services [OssLicensesMenuActivity], which
 * lists the bundled open-source libraries; the Privacy Policy entry is a placeholder that only
 * acknowledges the tap with haptic feedback. The Lingua tile's subtitle shows the language the app
 * is running with, re-read whenever its sheet opens or closes (a language change also recreates
 * the activity).
 */
@Composable
fun SettingsScreen() {
    val strSettingsOssLicensesTitle = stringResource(R.string.settings_oss_licenses_title)

    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = rememberHapticManager()

    val navigator = LocalAppNavigator.current

    val languageSheetOpen by remember(navigator) {
        derivedStateOf { navigator?.entries?.any { it.route == SheetRoute.SettingsLanguage } == true }
    }
    val languageLabel = remember(languageSheetOpen) { currentAppLanguageLabel(context) }

    val sections = listOf(
        Triple(
            SettingsEntryGroup(
                name = stringResource(R.string.settings_preferences_title),
                caption = stringResource(R.string.settings_preferences_subtitle),
                entries = listOf(
                    SettingsEntry(
                        "appearance",
                        stringResource(R.string.settings_appearance_title),
                        stringResource(R.string.settings_appearance_subtitle),
                        Icons.Outlined.Palette,
                        onClick = { navigator?.navigate(SheetRoute.SettingsAppearance) }),
                    SettingsEntry(
                        "language",
                        stringResource(R.string.settings_language_title),
                        languageLabel,
                        Icons.Outlined.Translate,
                        onClick = { navigator?.navigate(SheetRoute.SettingsLanguage) }),
                    SettingsEntry(
                        "app_lock",
                        stringResource(R.string.settings_security_title),
                        stringResource(R.string.settings_security_subtitle),
                        Icons.Outlined.Lock,
                        onClick = { navigator?.navigate(SheetRoute.SettingsSecurity) }),
                    SettingsEntry(
                        "file_open",
                        stringResource(R.string.settings_file_opening_title),
                        stringResource(R.string.settings_file_opening_subtitle),
                        Icons.Outlined.FileOpen,
                        onClick = { navigator?.navigate(SheetRoute.FileAssociations) }),
                    SettingsEntry(
                        "haptic",
                        stringResource(R.string.settings_haptic_title),
                        stringResource(R.string.settings_haptic_subtitle),
                        Icons.Outlined.Vibration,
                        onClick = { navigator?.navigate(SheetRoute.SettingsHaptic) }),
                ),
            ),
            scheme.primaryContainer, scheme.onPrimaryContainer,
        ),
        Triple(
            SettingsEntryGroup(
                name = stringResource(R.string.settings_information_title),
                caption = stringResource(R.string.settings_information_subtitle),
                entries = buildList {
                    add(
                        SettingsEntry(
                            "about",
                            stringResource(R.string.settings_about_title),
                            stringResource(R.string.settings_about_subtitle),
                            Icons.Outlined.Info,
                            onClick = { navigator?.navigate(SheetRoute.AppInfo) })
                    )
                    // Debug-only, so its copy is hardcoded rather than translated.
                    if (BuildConfig.DEBUG) {
                        add(
                            SettingsEntry(
                                "notification-debug",
                                "Notifications debug",
                                "Fire one of every notification spec",
                                Icons.Outlined.Info,
                                onClick = { navigator?.navigate(SheetRoute.NotificationDebug) })
                        )
                    }
                    add(
                        SettingsEntry(
                            "privacy",
                            stringResource(R.string.settings_privacy_title),
                            stringResource(R.string.settings_privacy_subtitle),
                            Icons.Outlined.PrivacyTip,
                            onClick = { haptic.tap() })
                    )
                    add(
                        SettingsEntry(
                            "license",
                            stringResource(R.string.settings_license_title),
                            stringResource(R.string.settings_license_subtitle),
                            Icons.Outlined.Description,
                            onClick = {
                                OssLicensesMenuActivity.setActivityTitle(strSettingsOssLicensesTitle)
                                context.startActivity(
                                    Intent(
                                        context,
                                        OssLicensesMenuActivity::class.java
                                    )
                                )
                            }
                        )
                    )
                },
            ),
            scheme.secondaryContainer, scheme.onSecondaryContainer,
        ),
    )

    Column(
        modifier = Modifier
            .testTag(SettingsTestTags.ROOT)
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
}
