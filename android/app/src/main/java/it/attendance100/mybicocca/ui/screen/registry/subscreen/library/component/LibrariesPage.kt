package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Dedicated Libraries directory screen: one photo card per bookable library (ordered preferred
 * campus first by the ViewModel), with page-turn loading and retry error states.
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
            .padding(bottom = 24.dp)
            .testTag(LibraryTestTags.LIBRARIES_PAGE),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (libraries) {
            Loadable.NotYetLoaded -> when (librariesStatus) {
                is SyncStatus.Failed -> SheetMessage(
                    icon = Icons.Outlined.MeetingRoom,
                    title = stringResource(R.string.common_error_title),
                    body = stringResource(R.string.library_libraries_loading_failed),
                    action = { RetryButton(onClick = onRetry) },
                    modifier = Modifier.testTag(LibraryTestTags.LIBRARIES_ERROR),
                )

                else -> LibraryLoading(
                    label = stringResource(R.string.library_libraries_loading),
                    modifier = Modifier.testTag(LibraryTestTags.LIBRARIES_LOADING),
                )
            }

            is Loadable.Loaded -> {
                if (libraries.value.isEmpty()) {
                    SheetMessage(
                        icon = Icons.Outlined.MeetingRoom,
                        title = stringResource(R.string.library_no_libraries),
                        body = stringResource(R.string.library_no_libraries_body),
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

@Preview(name = "Libraries Loaded · Light", showBackground = true)
@Composable
private fun LibrariesLoadedLightPreview() {
    LibraryPreview(dark = false) {
        LibrariesPage(
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            librariesStatus = SyncStatus.Idle,
            onOpenLibrary = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Libraries Loaded · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibrariesLoadedDarkPreview() {
    LibraryPreview(dark = true) {
        LibrariesPage(
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            librariesStatus = SyncStatus.Idle,
            onOpenLibrary = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Libraries Loading · Light", showBackground = true)
@Composable
private fun LibrariesLoadingLightPreview() {
    LibraryPreview(dark = false) {
        LibrariesPage(
            libraries = Loadable.NotYetLoaded,
            librariesStatus = SyncStatus.Refreshing,
            onOpenLibrary = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Libraries Error · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibrariesErrorDarkPreview() {
    LibraryPreview(dark = true) {
        LibrariesPage(
            libraries = Loadable.NotYetLoaded,
            librariesStatus = SyncStatus.Failed(RuntimeException("offline")),
            onOpenLibrary = {},
            onRetry = {},
        )
    }
}
