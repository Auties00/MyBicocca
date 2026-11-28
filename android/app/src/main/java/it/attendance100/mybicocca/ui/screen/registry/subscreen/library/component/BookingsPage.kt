package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.isCheckInOpenAt
import it.attendance100.mybicocca.ui.component.input.SegmentedSwitch
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryBookingsTab
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDateTime

/**
 * Signed-in student's bookings screen with a pinned "In programma" / "Storico" segmented switch:
 * any active or check-in-open booking leads as a canteen-style hero box with quick presence
 * check-in, followed by tonal cards for other bookings. History lists ended bookings newest first.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BookingsPage(
    reservations: Loadable<List<LibraryReservation>>,
    pastReservations: Loadable<List<LibraryReservation>>,
    selectedTab: LibraryBookingsTab,
    onSelectTab: (LibraryBookingsTab) -> Unit,
    onOpenReservation: (LibraryReservation) -> Unit,
    onVerifyPresence: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier,
    now: LocalDateTime = LocalDateTime.now(AppClock.clock),
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(LibraryTestTags.BOOKINGS_PAGE),
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth(),
        ) {
            when (selectedTab) {
                LibraryBookingsTab.Upcoming -> when (reservations) {
                    Loadable.NotYetLoaded -> LibraryLoading(
                        label = stringResource(R.string.library_syncing),
                        modifier = Modifier.testTag(LibraryTestTags.BOOKINGS_SYNCING),
                    )

                    is Loadable.Loaded -> {
                        if (reservations.value.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 32.dp, vertical = 20.dp)
                                    .testTag(LibraryTestTags.BOOKINGS_EMPTY),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.library_art_book_stack),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(width = 160.dp, height = 120.dp),
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.library_no_bookings),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.library_no_bookings_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = { haptic.tap(); onBook() },
                                    shapes = ButtonDefaults.shapes(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = ButtonDefaults.MediumContainerHeight)
                                        .testTag(LibraryTestTags.BOOKINGS_EMPTY_BOOK),
                                    contentPadding = ButtonDefaults.MediumContentPadding,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                                        contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
                                    ),
                                ) {
                                    Icon(
                                        Icons.Outlined.EventSeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.library_book),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        } else {
                            val (liveBookings, otherBookings) = reservations.value.partition {
                                it.isCheckInOpenAt(now)
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp)
                                    .testTag(LibraryTestTags.BOOKINGS_LIST),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                liveBookings.forEach { reservation ->
                                    LiveReservationHeroCard(
                                        reservation = reservation,
                                        onClick = { onOpenReservation(reservation) },
                                        onVerifyPresence = onVerifyPresence,
                                        now = now,
                                        modifier = Modifier.testTag(
                                            LibraryTestTags.booking(reservation.reservationId),
                                        ),
                                    )
                                }
                                otherBookings.forEach { reservation ->
                                    LibraryReservationCard(
                                        reservation = reservation,
                                        onClick = { onOpenReservation(reservation) },
                                        now = now,
                                        modifier = Modifier.testTag(
                                            LibraryTestTags.booking(reservation.reservationId),
                                        ),
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }

                LibraryBookingsTab.History -> when (pastReservations) {
                    Loadable.NotYetLoaded -> LibraryLoading(
                        label = stringResource(R.string.library_syncing),
                        modifier = Modifier.testTag(LibraryTestTags.BOOKINGS_SYNCING),
                    )

                    is Loadable.Loaded -> {
                        if (pastReservations.value.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 32.dp, vertical = 28.dp)
                                    .testTag(LibraryTestTags.BOOKINGS_HISTORY_EMPTY),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.library_art_book_stack),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(width = 148.dp, height = 110.dp),
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.library_no_past_bookings),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp)
                                    .testTag(LibraryTestTags.BOOKINGS_LIST),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                pastReservations.value.forEach { reservation ->
                                    LibraryReservationCard(
                                        reservation = reservation,
                                        onClick = { onOpenReservation(reservation) },
                                        now = now,
                                        modifier = Modifier.testTag(
                                            LibraryTestTags.booking(reservation.reservationId),
                                        ),
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        SegmentedSwitch(
            options = listOf(LibraryBookingsTab.Upcoming, LibraryBookingsTab.History),
            selected = selectedTab,
            onSelected = onSelectTab,
            label = { tab ->
                when (tab) {
                    LibraryBookingsTab.Upcoming -> stringResource(R.string.library_bookings_tab_upcoming)
                    LibraryBookingsTab.History -> stringResource(R.string.library_bookings_tab_history)
                }
            },
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
                .testTag(LibraryTestTags.BOOKINGS_TAB_SWITCH),
        )
    }
}

@Preview(name = "Bookings Upcoming With Live · Light", showBackground = true)
@Composable
private fun BookingsUpcomingLiveLightPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = false) {
        BookingsPage(
            reservations = Loadable.Loaded(
                listOf(LibraryPreviewDefaults.LiveReservation) + LibraryPreviewDefaults.Reservations.drop(1),
            ),
            pastReservations = Loadable.Loaded(LibraryPreviewDefaults.PastReservations),
            selectedTab = LibraryBookingsTab.Upcoming,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
            now = now,
        )
    }
}

@Preview(
    name = "Bookings Upcoming With Live · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun BookingsUpcomingLiveDarkPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = true) {
        BookingsPage(
            reservations = Loadable.Loaded(
                listOf(LibraryPreviewDefaults.LiveReservation) + LibraryPreviewDefaults.Reservations.drop(1),
            ),
            pastReservations = Loadable.Loaded(LibraryPreviewDefaults.PastReservations),
            selectedTab = LibraryBookingsTab.Upcoming,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
            now = now,
        )
    }
}

@Preview(name = "Bookings Empty · Light", showBackground = true)
@Composable
private fun BookingsEmptyLightPreview() {
    LibraryPreview(dark = false) {
        BookingsPage(
            reservations = Loadable.Loaded(emptyList()),
            pastReservations = Loadable.Loaded(LibraryPreviewDefaults.PastReservations),
            selectedTab = LibraryBookingsTab.Upcoming,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
        )
    }
}

@Preview(
    name = "Bookings Empty · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun BookingsEmptyDarkPreview() {
    LibraryPreview(dark = true) {
        BookingsPage(
            reservations = Loadable.Loaded(emptyList()),
            pastReservations = Loadable.Loaded(emptyList()),
            selectedTab = LibraryBookingsTab.Upcoming,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
        )
    }
}

@Preview(name = "Bookings History · Light", showBackground = true)
@Composable
private fun BookingsHistoryLightPreview() {
    val now = LocalDateTime.of(LibraryPreviewDefaults.Today, LibraryPreviewDefaults.NowTime)
    LibraryPreview(dark = false) {
        BookingsPage(
            reservations = Loadable.Loaded(emptyList()),
            pastReservations = Loadable.Loaded(LibraryPreviewDefaults.PastReservations),
            selectedTab = LibraryBookingsTab.History,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
            now = now,
        )
    }
}

@Preview(
    name = "Bookings History Empty · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun BookingsHistoryEmptyDarkPreview() {
    LibraryPreview(dark = true) {
        BookingsPage(
            reservations = Loadable.Loaded(emptyList()),
            pastReservations = Loadable.Loaded(emptyList()),
            selectedTab = LibraryBookingsTab.History,
            onSelectTab = {},
            onOpenReservation = {},
            onVerifyPresence = {},
            onBook = {},
        )
    }
}
