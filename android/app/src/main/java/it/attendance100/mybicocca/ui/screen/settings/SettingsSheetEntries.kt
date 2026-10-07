package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.ui.navigation.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.navigation.LocalDestination
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationChooserPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.FileAssociationsPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.fileAssociations.fileAssociationKind
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.LanguagePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearancePage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsHaptic.SettingsHapticPage
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecurityPage

/**
 * The settings feature's sheet pages. Every page draws its own title, so none carries a pinned
 * header. [fileOpenViewModel] is the shell's file-open preference store: the file-associations
 * list and its chooser page read and write the same, already-loaded choices, so the chooser opens
 * on the current default instead of flashing "ask every time" while a fresh ViewModel loads.
 */
fun EntryProviderScope<NavKey>.settingsSheetEntries(
    fileOpenViewModel: FileOpenPreferenceViewModel,
) {
    entry<SheetRoute.SettingsAppearance> { SettingsAppearancePage() }
    entry<SheetRoute.SettingsLanguage> { LanguagePage() }
    entry<SheetRoute.SettingsHaptic> { SettingsHapticPage() }
    entry<SheetRoute.SettingsSecurity> { SettingsSecurityPage() }
    entry<SheetRoute.FileAssociations> { FileAssociationsPage(viewModel = fileOpenViewModel) }
    entry<SheetRoute.FileAssociationChooser> { key ->
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
    entry<SheetRoute.AppInfo> { AppInfoPage() }
}
