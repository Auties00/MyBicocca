package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader

/**
 * Profile presented as an overlay scene of the account switcher modal: a pinned
 * title/subtitle [SheetPagerHeader] whose back arrow returns to the accounts list, above
 * the shared [ProfileContent] body filling the fixed frame the host sizes.
 * Pull-to-refresh is absent because the drag belongs to the sheet; the in-profile
 * calculators still open their own sheets on top.
 */
@Composable
fun ProfileSheetContent(
    modifier: Modifier = Modifier,
    onCollapse: () -> Unit,
    onOpenAppelli: (courseKey: String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        SheetPagerHeader(
            depth = 0,
            title = stringResource(R.string.screen_title_profile),
            subtitle = stringResource(R.string.profile_subtitle),
            onBack = onCollapse,
        )

        ProfileContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onOpenAppelli = onOpenAppelli,
            viewModel = viewModel,
        )
    }
}
