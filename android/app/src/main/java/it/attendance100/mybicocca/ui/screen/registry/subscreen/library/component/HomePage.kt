package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.compactLibraryName
import it.attendance100.mybicocca.domain.model.library.zoneColor
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.bookingDayLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formattedSeatLabel
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.format.DateTimeFormatter

private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val NonLetterRegex = Regex("[^\\p{L}]+")
private val HeroCardShape = RoundedCornerShape(28.dp)

/**
 * Extracts initials from an e-mail's local part (`m.rossi99@…` → `"MR"`, single word → its first
 * letter) for the signed-in account avatar badge.
 */
internal fun emailInitials(email: String): String {
    val localPart = email.substringBefore('@').trim()
    if (localPart.isEmpty()) return ""
    val parts = localPart.split(NonLetterRegex).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.size == 1 -> parts.first().first().uppercase()
        else -> localPart.firstOrNull { it.isLetterOrDigit() }?.uppercase().orEmpty()
    }
}

/**
 * Trailing action in the Biblioteca Home header: a compact "Accedi" pill when signed out, or a
 * 42 dp initials circle opening Account when signed in. End padding offsets [SheetPagerHeader]'s
 * 12 dp icon-button overhang so non-icon actions line up with the 24 dp inset.
 */
@Composable
internal fun HomeHeaderAction(
    linkedEmail: String?,
    onLogin: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    if (linkedEmail == null) {
        FilledTonalButton(
            onClick = { haptic.tap(); onLogin() },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            modifier = Modifier
                .padding(end = 12.dp)
                .heightIn(min = 40.dp)
                .testTag(LibraryTestTags.HOME_SIGN_IN),
        ) {
            Text(
                text = stringResource(R.string.library_login),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        val accountDescription = stringResource(R.string.library_account)
        // 48 dp touch target around a 42 dp visual circle; 9 dp end padding aligns the circle with the 24 dp inset.
        Box(
            modifier = Modifier
                .padding(end = 9.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) {
                    haptic.tap()
                    onOpenAccount()
                }
                .semantics { contentDescription = accountDescription }
                .testTag(LibraryTestTags.HOME_ACCOUNT),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emailInitials(linkedEmail),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/**
 * Hub-style landing page for the Biblioteca sheet: either a single wide Libraries CTA card (signed
 * out or no upcoming booking) or a split pair (Libraries + next booking), over connected rows for
 * "Le mie prenotazioni" and "Servizi". Wraps its content with no library photos or percentages.
 */
@Composable
internal fun HomePage(
    linkedEmail: String?,
    nextReservation: LibraryReservation?,
    libraries: Loadable<List<Library>>,
    reservations: Loadable<List<LibraryReservation>>,
    syncStatus: SyncStatus,
    onOpenLibraries: () -> Unit,
    onOpenReservation: (LibraryReservation) -> Unit,
    onOpenBookings: () -> Unit,
    onOpenServices: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loggedIn = linkedEmail != null
    val bookingsCount = reservations.valueOrNull()?.size ?: 0
    val libraryNames = libraries.valueOrNull().orEmpty().take(2).map { it.name }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .testTag(LibraryTestTags.HOME_PAGE),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (loggedIn && nextReservation != null) {
            HomeSplitBoxes(
                libraryNames = libraryNames,
                nextReservation = nextReservation,
                onOpenLibraries = onOpenLibraries,
                onOpenReservation = { onOpenReservation(nextReservation) },
            )
        } else {
            LibrariesWideCard(
                onClick = onOpenLibraries,
                modifier = Modifier.testTag(LibraryTestTags.HOME_LIBRARIES_CARD),
            )
        }

        val bookingsSupporting = when {
            !loggedIn -> stringResource(R.string.library_bookings_row_signed_out)
            reservations !is Loadable.Loaded || syncStatus is SyncStatus.Refreshing ->
                stringResource(R.string.library_syncing)
            bookingsCount == 0 -> stringResource(R.string.library_bookings_none_scheduled)
            else -> pluralStringResource(
                R.plurals.library_bookings_scheduled_count,
                bookingsCount,
                bookingsCount,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HomeHubRow(
                icon = Icons.AutoMirrored.Outlined.ListAlt,
                title = stringResource(R.string.library_my_bookings),
                subtitle = bookingsSupporting,
                isFirst = true,
                isLast = false,
                onClick = onOpenBookings,
                modifier = Modifier.testTag(LibraryTestTags.HOME_BOOKINGS_ROW),
            )
            HomeHubRow(
                icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                title = stringResource(R.string.library_services),
                subtitle = stringResource(R.string.library_services_subtitle),
                isFirst = false,
                isLast = true,
                onClick = onOpenServices,
                modifier = Modifier.testTag(LibraryTestTags.HOME_SERVICES_ROW),
            )
        }
    }
}

@Composable
private fun LibrariesWideCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val pillBg = if (dark) scheme.onPrimaryContainer else scheme.surfaceContainerLowest
    val pillFg = if (dark) scheme.primaryContainer else scheme.onPrimaryContainer

    // Top inset reserves space inside the scroll container so the desk artwork breaks cleanly above the card.
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { haptic.tap(); onClick() },
            shape = HeroCardShape,
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .heightIn(min = 170.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 132.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.library_libraries_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.library_home_libraries_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.82f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = CircleShape,
                    color = pillBg,
                    contentColor = pillFg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.library_home_see_libraries),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Narrower than the scaled drawing, so the desk runs off the card's end edge.
        Image(
            painter = painterResource(R.drawable.library_art_desk),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            alignment = Alignment.CenterStart,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = 170.dp, height = 156.dp),
        )
    }
}

@Composable
private fun HomeSplitBoxes(
    libraryNames: List<String>,
    nextReservation: LibraryReservation,
    onOpenLibraries: () -> Unit,
    onOpenReservation: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val librariesBody = if (libraryNames.isNotEmpty()) {
        libraryNames.joinToString("\n")
    } else {
        stringResource(R.string.library_home_libraries_body)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SplitHeroBox(
            artwork = {
                Image(
                    painter = painterResource(R.drawable.library_art_desk),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 160.dp, height = 120.dp),
                )
            },
            onClick = { haptic.tap(); onOpenLibraries() },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag(LibraryTestTags.HOME_LIBRARIES_CARD),
        ) {
            Text(
                text = stringResource(R.string.library_libraries_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = librariesBody,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        SplitHeroBox(
            artwork = {
                Image(
                    painter = painterResource(nextReservation.zoneColor().artDrawable()),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .librarySharedElement(key = libraryBookingArtSharedKey(nextReservation.reservationId))
                        .size(width = 160.dp, height = 120.dp),
                )
            },
            onClick = { haptic.tap(); onOpenReservation() },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag(LibraryTestTags.HOME_NEXT_BOOKING),
        ) {
            Text(
                text = stringResource(R.string.library_home_your_booking),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = bookingDayLabel(nextReservation.start.toLocalDate()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${nextReservation.start.format(TimeFormat)}–${nextReservation.end.format(TimeFormat)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${nextReservation.formattedSeatLabel()} · ${nextReservation.compactLibraryName()}",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onPrimaryContainer.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SplitHeroBox(
    artwork: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.heightIn(min = 204.dp)) {
        Surface(
            onClick = onClick,
            shape = HeroCardShape,
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 62.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                content()
            }
        }
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            artwork()
        }
    }
}

@Composable
private fun HomeHubRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()

    Surface(
        onClick = { haptic.tap(); onClick() },
        shape = segmentedShape(isFirst = isFirst, isLast = isLast),
        color = scheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(name = "Home Signed Out · Light", showBackground = true)
@Composable
private fun HomeSignedOutLightPreview() {
    LibraryPreview(dark = false) {
        HomePage(
            linkedEmail = null,
            nextReservation = null,
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.NotYetLoaded,
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(
    name = "Home Signed Out · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun HomeSignedOutDarkPreview() {
    LibraryPreview(dark = true) {
        HomePage(
            linkedEmail = null,
            nextReservation = null,
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.NotYetLoaded,
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(name = "Home Signed In No Booking · Light", showBackground = true)
@Composable
private fun HomeNoBookingLightPreview() {
    LibraryPreview(dark = false) {
        HomePage(
            linkedEmail = LibraryPreviewDefaults.Email,
            nextReservation = null,
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.Loaded(emptyList()),
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(
    name = "Home Signed In No Booking · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun HomeNoBookingDarkPreview() {
    LibraryPreview(dark = true) {
        HomePage(
            linkedEmail = LibraryPreviewDefaults.Email,
            nextReservation = null,
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.Loaded(emptyList()),
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(name = "Home With Booking · Light", showBackground = true)
@Composable
private fun HomeWithBookingLightPreview() {
    LibraryPreview(dark = false) {
        HomePage(
            linkedEmail = LibraryPreviewDefaults.Email,
            nextReservation = LibraryPreviewDefaults.Reservations.first(),
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.Loaded(LibraryPreviewDefaults.Reservations),
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(
    name = "Home With Booking · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun HomeWithBookingDarkPreview() {
    LibraryPreview(dark = true) {
        HomePage(
            linkedEmail = LibraryPreviewDefaults.Email,
            nextReservation = LibraryPreviewDefaults.Reservations.first(),
            libraries = Loadable.Loaded(LibraryPreviewDefaults.Libraries),
            reservations = Loadable.Loaded(LibraryPreviewDefaults.Reservations),
            syncStatus = SyncStatus.Idle,
            onOpenLibraries = {},
            onOpenReservation = {},
            onOpenBookings = {},
            onOpenServices = {},
        )
    }
}

@Preview(name = "Home Header Actions · Light", showBackground = true)
@Composable
private fun HomeHeaderActionsLightPreview() {
    LibraryPreview(dark = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeHeaderAction(linkedEmail = null, onLogin = {}, onOpenAccount = {})
            HomeHeaderAction(linkedEmail = LibraryPreviewDefaults.Email, onLogin = {}, onOpenAccount = {})
        }
    }
}

@Preview(
    name = "Home Header Actions · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun HomeHeaderActionsDarkPreview() {
    LibraryPreview(dark = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeHeaderAction(linkedEmail = null, onLogin = {}, onOpenAccount = {})
            HomeHeaderAction(linkedEmail = LibraryPreviewDefaults.Email, onLogin = {}, onOpenAccount = {})
        }
    }
}
