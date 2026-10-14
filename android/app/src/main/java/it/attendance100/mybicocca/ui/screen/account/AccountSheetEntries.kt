package it.attendance100.mybicocca.ui.screen.account

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.ui.navigation.requireAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeaderInPage
import it.attendance100.mybicocca.ui.navigation.sheetStyle
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherPage
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherSheetStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.ui.navigation.RootViewModel

/** The account switcher, opened from the avatar in the top bar; it shares the shell's [accountViewModel]. */
fun EntryProviderScope<NavKey>.accountSheetEntries(accountViewModel: AccountViewModel) {
    entry<SheetRoute.AccountSwitcher>(
        metadata = sheetHeaderInPage() + sheetStyle(AccountSwitcherSheetStyle),
    ) {
        val navigator = requireAppNavigator()
        val rootViewModel: RootViewModel = hiltViewModel(checkNotNull(LocalActivity.current as? ComponentActivity))
        AccountSwitcherPage(
            onOpenProfile = { navigator.navigate(AppRoute.Profile) },
            onOpenSettings = { navigator.navigate(AppRoute.Settings) },
            onCareerPickRequired = { account -> rootViewModel.onSignedIn(account.id, requiresPick = true) },
            viewModel = accountViewModel,
        )
    }
}
