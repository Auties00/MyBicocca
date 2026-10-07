package it.attendance100.mybicocca.ui.screen.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.navigation.LocalDestination
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeader
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeaderInPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationChooserPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.fileAssociationChooserHeader
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationsPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.fileAssociationKind
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.LanguagePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearancePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsHaptic.SettingsHapticPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecurityPage

/**
 * The settings feature's sheet pages, each under the sheet's pinned title/subtitle header; About
 * ([SheetRoute.AppInfo]) pages internally and draws its own. [fileOpenViewModel] is the shell's file-open preference store: the file-associations
 * list and its chooser page read and write the same, already-loaded choices, so the chooser opens
 * on the current default instead of flashing "ask every time" while a fresh ViewModel loads.
 */
fun EntryProviderScope<NavKey>.settingsSheetEntries(
    fileOpenViewModel: FileOpenPreferenceViewModel,
) {
    entry<SheetRoute.SettingsAppearance>(
        metadata = staticSheetHeader<SheetRoute.SettingsAppearance>(
            R.string.settings_appearance_sheet_title,
            R.string.settings_appearance_sheet_subtitle,
        ),
    ) { SettingsAppearancePage() }
    entry<SheetRoute.SettingsLanguage>(
        metadata = staticSheetHeader<SheetRoute.SettingsLanguage>(
            R.string.settings_language_sheet_title,
            R.string.settings_language_sheet_subtitle,
        ),
    ) { LanguagePage() }
    entry<SheetRoute.SettingsHaptic>(
        metadata = staticSheetHeader<SheetRoute.SettingsHaptic>(
            R.string.settings_haptic_sheet_title,
            R.string.settings_haptic_sheet_subtitle,
        ),
    ) { SettingsHapticPage() }
    entry<SheetRoute.SettingsSecurity>(
        metadata = staticSheetHeader<SheetRoute.SettingsSecurity>(
            R.string.settings_security_sheet_title,
            R.string.settings_security_sheet_subtitle,
        ),
    ) { SettingsSecurityPage() }
    entry<SheetRoute.FileAssociations>(
        metadata = staticSheetHeader<SheetRoute.FileAssociations>(
            R.string.settings_file_opening_title,
            R.string.settings_file_opening_subtitle,
        ),
    ) { FileAssociationsPage(viewModel = fileOpenViewModel) }
    entry<SheetRoute.FileAssociationChooser>(
        metadata = sheetHeader<SheetRoute.FileAssociationChooser> { key ->
            val choices by fileOpenViewModel.choices.collectAsStateWithLifecycle()
            fileAssociationChooserHeader(
                kind = remember(key.preferenceKey) { fileAssociationKind(key.preferenceKey) },
                current = choices[key.preferenceKey],
            )
        },
    ) { key ->
        val navigator = requireAppNavigator()
        val self = checkNotNull(LocalDestination.current)
        val choices by fileOpenViewModel.choices.collectAsStateWithLifecycle()
        val kind = remember(key.preferenceKey) { fileAssociationKind(key.preferenceKey) }
        if (kind == null) {
            LaunchedEffect(self.id) { navigator.pop(self.id) }
        } else {
            FileAssociationChooserPage(
                kind = kind,
                current = choices[key.preferenceKey],
                onSelect = { choice ->
                    if (choice == null) {
                        fileOpenViewModel.forget(key.preferenceKey)
                    } else {
                        fileOpenViewModel.remember(key.preferenceKey, choice)
                    }
                    navigator.pop(self.id)
                },
            )
        }
    }
    entry<SheetRoute.AppInfo>(metadata = sheetHeaderInPage()) { AppInfoPage() }
}

/** A pinned header with a fixed title and subtitle. */
private inline fun <reified K : Route> staticSheetHeader(
    @StringRes title: Int,
    @StringRes subtitle: Int,
): Map<String, Any> = sheetHeader<K> {
    SheetHeaderSpec(title = stringResource(title), subtitle = stringResource(subtitle))
}
