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
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage

// "Prenota" sub-page: the bookable libraries. The modal header already names it, so no section title.
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
        when (libraries) {
            Loadable.NotYetLoaded -> when (librariesStatus) {
                is SyncStatus.Failed -> SheetMessage(
                    icon = Icons.Outlined.MeetingRoom,
                    title = "Caricamento non riuscito",
                    body = "Non è stato possibile caricare le biblioteche.",
                    action = { RetryButton(onClick = onRetry) },
                )
                else -> SheetLoadingIndicator(label = "Caricamento biblioteche…")
            }

            is Loadable.Loaded -> {
                if (libraries.value.isEmpty()) {
                    SheetMessage(
                        icon = Icons.Outlined.MeetingRoom,
                        title = "Nessuna biblioteca",
                        body = "Al momento non ci sono biblioteche prenotabili.",
                    )
                } else {
                    libraries.value.forEach { library ->
                        LibraryCard(library = library, onClick = { onOpenLibrary(library) })
                    }
                }
            }
        }
    }
}
