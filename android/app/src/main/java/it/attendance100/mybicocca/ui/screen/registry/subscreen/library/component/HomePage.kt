package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags

/**
 * Landing page of the Biblioteca sheet: the server-synced bookings over a pinned footer button.
 * Until the email is verified it shows a verify empty state with a "Verifica" footer; once
 * verified, the scrollable bookings list (or its empty state, or the sheet loading indicator
 * while the first sync runs) with a "Prenota" footer. Mirrors the Appuntamenti reservations page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomePage(
    reservations: Loadable<List<LibraryReservation>>,
    loggedIn: Boolean,
    onOpenReservation: (LibraryReservation) -> Unit,
    onLogin: () -> Unit,
    onPrenota: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookings = (reservations as? Loadable.Loaded)?.value.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .testTag(LibraryTestTags.HOME_PAGE),
    ) {
        when {
            !loggedIn -> EmptyBox(modifier = Modifier.testTag(LibraryTestTags.HOME_VERIFY_PROMPT)) {
                EmptyState(
                    icon = Icons.Outlined.MarkEmailRead,
                    title = "Verifica la tua email",
                    body = "Verifica la tua email istituzionale per vedere e prenotare i posti in biblioteca.",
                )
            }

            reservations !is Loadable.Loaded -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .testTag(LibraryTestTags.HOME_SYNCING),
            ) {
                SheetLoadingIndicator(label = "Sincronizzazione…", modifier = Modifier.align(Alignment.Center))
            }

            bookings.isEmpty() -> EmptyBox(modifier = Modifier.testTag(LibraryTestTags.HOME_EMPTY)) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                    title = "Nessuna prenotazione",
                    body = "Non hai ancora prenotato un posto in biblioteca. Tocca Prenota per farlo.",
                )
            }

            else -> Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .testTag(LibraryTestTags.HOME_LIST),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                bookings.forEach { reservation ->
                    LibraryReservationCard(reservation = reservation, onClick = { onOpenReservation(reservation) })
                }
            }
        }

        FooterButton(
            label = if (loggedIn) "Prenota" else "Verifica",
            icon = if (loggedIn) Icons.Outlined.EventSeat else Icons.Outlined.MarkEmailRead,
            onClick = if (loggedIn) onPrenota else onLogin,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
                .testTag(LibraryTestTags.HOME_FOOTER),
        )
    }
}

@Composable
private fun EmptyBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(bottom = 4.dp),
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FooterButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
