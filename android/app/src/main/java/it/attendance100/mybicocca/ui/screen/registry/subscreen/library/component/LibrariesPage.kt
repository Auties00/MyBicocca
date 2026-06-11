package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags

/**
 * "Prenota" sub-page: the bookable libraries as a scrollable column of [LibraryCard]s, with
 * loading, error-with-retry and empty states. The modal header already names the page, so it
 * renders no section title of its own.
 */
@Composable
internal fun LibrariesPage(
    libraries: Loadable<List<Library>>,
    librariesStatus: SyncStatus,
    onOpenLibrary: (Library) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val context = LocalContext.current
        when (libraries) {
            Loadable.NotYetLoaded -> when (librariesStatus) {
                is SyncStatus.Failed -> SheetMessage(
                    icon = Icons.Outlined.MeetingRoom,
                    title = context.getString(R.string.common_error_title),
                    body = context.getString(R.string.library_libraries_loading_failed),
                    action = { RetryButton(onClick = onRetry) },
                    modifier = Modifier.testTag(LibraryTestTags.LIBRARIES_ERROR),
                )
                else -> SheetLoadingIndicator(
                    label = context.getString(R.string.library_libraries_loading),
                    modifier = Modifier.testTag(LibraryTestTags.LIBRARIES_LOADING),
                )
            }

            is Loadable.Loaded -> {
                if (libraries.value.isEmpty()) {
                    SheetMessage(
                        icon = Icons.Outlined.MeetingRoom,
                        title = context.getString(R.string.library_no_libraries),
                        body = context.getString(R.string.library_no_libraries_body),
                        modifier = Modifier.testTag(LibraryTestTags.LIBRARIES_EMPTY),
                    )
                } else {
                    libraries.value.forEach { library ->
                        LibraryCard(
                            library = library,
                            onClick = { onOpenLibrary(library) },
                            modifier = Modifier.testTag(LibraryTestTags.library(library.id)),
                        )
                    }
                }
            }
        }
    }
}
