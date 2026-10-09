package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.navigation.PopWhenMissing
import it.attendance100.mybicocca.ui.navigation.rememberPopSelf
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeader
import it.attendance100.mybicocca.ui.navigation.sheetHeaderInPage
import it.attendance100.mybicocca.ui.navigation.staticSheetHeader
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationChooserPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.fileAssociationChooserHeader
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationsPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.fileAssociationKind
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.LanguagePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.notificationDebug.NotificationDebugPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearancePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsHaptic.SettingsHapticPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecurityPage

/**
 * The settings sheets, each under a pinned title/subtitle header; About ([SheetRoute.AppInfo])
 * pages internally and draws its own. [fileOpenViewModel] is the shell's file-open preference
 * store: the file-associations list and its chooser read and write the same, already-loaded
 * choices, so the chooser opens on the current default instead of flashing "ask every time".
 */
fun EntryProviderScope<NavKey>.settingsSheetEntries(
    fileOpenViewModel: FileOpenPreferenceViewModel,
) {
    entry<SheetRoute.SettingsAppearance>(
        metadata = staticSheetHeader(
            R.string.settings_appearance_sheet_title,
            R.string.settings_appearance_sheet_subtitle,
        ),
    ) { SettingsAppearancePage() }
    entry<SheetRoute.SettingsLanguage>(
        metadata = staticSheetHeader(
            R.string.settings_language_sheet_title,
            R.string.settings_language_sheet_subtitle,
        ),
    ) { LanguagePage() }
    entry<SheetRoute.SettingsHaptic>(
        metadata = staticSheetHeader(
            R.string.settings_haptic_sheet_title,
            R.string.settings_haptic_sheet_subtitle,
        ),
    ) { SettingsHapticPage() }
    entry<SheetRoute.SettingsSecurity>(
        metadata = staticSheetHeader(
            R.string.settings_security_sheet_title,
            R.string.settings_security_sheet_subtitle,
        ),
    ) { SettingsSecurityPage() }
    entry<SheetRoute.FileAssociations>(
        metadata = staticSheetHeader(
            R.string.settings_file_opening_title,
            R.string.settings_file_opening_subtitle,
        ),
    ) { FileAssociationsPage(viewModel = fileOpenViewModel) }
    entry<SheetRoute.FileAssociationChooser>(
        metadata = sheetHeader<SheetRoute.FileAssociationChooser> { route ->
            val choices by fileOpenViewModel.choices.collectAsStateWithLifecycle()
            fileAssociationChooserHeader(
                kind = remember(route.preferenceKey) { fileAssociationKind(route.preferenceKey) },
                current = choices[route.preferenceKey],
            )
        },
    ) { route ->
        val close = rememberPopSelf()
        val choices by fileOpenViewModel.choices.collectAsStateWithLifecycle()
        val kind = remember(route.preferenceKey) { fileAssociationKind(route.preferenceKey) }
        // A preference key from an older version that no longer names a file kind.
        PopWhenMissing(loaded = true, missing = kind == null)
        if (kind != null) {
            FileAssociationChooserPage(
                kind = kind,
                current = choices[route.preferenceKey],
                onSelect = { choice ->
                    if (choice == null) {
                        fileOpenViewModel.forget(route.preferenceKey)
                    } else {
                        fileOpenViewModel.remember(route.preferenceKey, choice)
                    }
                    close()
                },
            )
        }
    }
    entry<SheetRoute.AppInfo>(metadata = sheetHeaderInPage()) { AppInfoPage() }
    // Debug-only, so its copy is hardcoded rather than translated.
    entry<SheetRoute.NotificationDebug>(
        metadata = sheetHeader<SheetRoute.NotificationDebug> {
            SheetHeaderSpec(
                title = "Notifications debug",
                subtitle = "Fire one of every notification spec",
            )
        },
    ) { NotificationDebugPage() }
}
