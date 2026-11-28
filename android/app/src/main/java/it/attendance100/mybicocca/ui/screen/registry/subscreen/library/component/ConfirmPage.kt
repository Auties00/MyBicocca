package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.ui.component.input.ConsentCheckboxCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.durationLabel
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FullDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEEE d MMMM", currentLocale())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Wizard step 4: recap of library, zone, seat, slot and booking e-mail, optional/required note,
 * and mandatory-consent switch. When signed out, displays an info card explaining one-time e-mail
 * verification and enables the "Verifica email e prenota" button without requiring consent yet.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ConfirmPage(
    libraryName: String,
    zoneName: String,
    seat: LibrarySeat,
    date: LocalDate,
    startTime: LocalTime,
    durationMinutes: Int,
    email: String,
    loggedIn: Boolean,
    note: String,
    onNoteChange: (String) -> Unit,
    agreement: LibraryAgreement?,
    consentAccepted: Boolean,
    onConsentChange: (Boolean) -> Unit,
    submitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val end = startTime.plusMinutes(durationMinutes.toLong())

    val noteOk = !seat.noteRequired || note.isNotBlank()
    val consentOk = agreement?.mandatory != true || consentAccepted
    val canSubmit = if (loggedIn) {
        email.isNotBlank() && noteOk && consentOk && !submitting
    } else {
        email.isNotBlank() && !submitting
    }
    val haptic = rememberHapticManager()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                IconRow(
                    Icons.Outlined.LocalLibrary,
                    stringResource(R.string.library_library),
                    "$libraryName · $zoneName",
                )
                IconRow(
                    Icons.Outlined.Chair,
                    stringResource(R.string.library_seat),
                    seat.shortName + if (seat.hasPowerOutlet) " · ${stringResource(R.string.library_power_outlet)}" else "",
                )
                val fullDateFormat = FullDateFormat
                val locale = currentLocale()
                IconRow(
                    Icons.Outlined.CalendarMonth,
                    stringResource(R.string.library_when),
                    buildString {
                        append(date.format(fullDateFormat).replaceFirstChar { it.titlecase(locale) })
                        append(" · ${startTime.format(TimeFormat)}–${end.format(TimeFormat)}")
                        append(" · ${durationLabel(durationMinutes)}")
                    },
                )
                IconRow(
                    Icons.Outlined.AlternateEmail,
                    stringResource(R.string.library_book_as),
                    email.ifBlank { "—" },
                )
            }

            if (seat.noteRequired || seat.noteLabel != null) {
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(seat.noteLabel ?: "Note") },
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (agreement != null) {
                ConsentRow(
                    agreement = agreement,
                    accepted = consentAccepted,
                    enabled = !submitting,
                    onChange = onConsentChange,
                )
            }
            Spacer(Modifier.size(4.dp))
        }

        if (!loggedIn) {
            VerifyEmailInfoCard(
                email = email,
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp)
                    .testTag(LibraryTestTags.CONFIRM_INFO_CARD),
            )
        }

        Button(
            onClick = { haptic.tap(); onSubmit() },
            enabled = canSubmit && LocalIsOnline.current,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                .testTag(LibraryTestTags.CONFIRM_SUBMIT),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            if (submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
                )
            } else {
                Text(
                    text = if (loggedIn) {
                        stringResource(R.string.library_book)
                    } else {
                        stringResource(R.string.library_verify_and_book)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun VerifyEmailInfoCard(
    email: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val message = if (email.isNotBlank()) {
        stringResource(R.string.library_confirm_verify_info, email)
    } else {
        stringResource(R.string.library_confirm_verify_info_no_email)
    }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun IconRow(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(14.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface)
        }
    }
}

@Composable
private fun ConsentRow(
    agreement: LibraryAgreement,
    accepted: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val linkStyles = TextLinkStyles(SpanStyle(color = scheme.primary, fontWeight = FontWeight.SemiBold))

    val statement = buildAnnotatedString {
        append(stringResource(R.string.library_accept) + " ")
        val url = agreement.url
        val label = agreement.name ?: stringResource(R.string.library_terms)
        if (url != null) {
            withLink(LinkAnnotation.Clickable("agreement", linkStyles) {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri())
            }) { append(label) }
        } else {
            append(label)
        }
    }

    ConsentCheckboxCard(
        checked = accepted,
        onCheckedChange = onChange,
        text = statement,
        enabled = enabled,
    )
}

@Preview(name = "Confirm Signed Out · Light", showBackground = true)
@Composable
private fun ConfirmSignedOutLightPreview() {
    LibraryPreview(dark = false) {
        ConfirmPage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seat = LibraryPreviewDefaults.Seats.first(),
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            email = LibraryPreviewDefaults.Email,
            loggedIn = false,
            note = "",
            onNoteChange = {},
            agreement = LibraryPreviewDefaults.Agreement,
            consentAccepted = false,
            onConsentChange = {},
            submitting = false,
            onSubmit = {},
        )
    }
}

@Preview(
    name = "Confirm Signed Out · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ConfirmSignedOutDarkPreview() {
    LibraryPreview(dark = true) {
        ConfirmPage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seat = LibraryPreviewDefaults.Seats.first(),
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            email = LibraryPreviewDefaults.Email,
            loggedIn = false,
            note = "",
            onNoteChange = {},
            agreement = LibraryPreviewDefaults.Agreement,
            consentAccepted = false,
            onConsentChange = {},
            submitting = false,
            onSubmit = {},
        )
    }
}

@Preview(name = "Confirm Signed In · Light", showBackground = true)
@Composable
private fun ConfirmSignedInLightPreview() {
    LibraryPreview(dark = false) {
        ConfirmPage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seat = LibraryPreviewDefaults.Seats.first(),
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            email = LibraryPreviewDefaults.Email,
            loggedIn = true,
            note = "",
            onNoteChange = {},
            agreement = LibraryPreviewDefaults.Agreement,
            consentAccepted = true,
            onConsentChange = {},
            submitting = false,
            onSubmit = {},
        )
    }
}

@Preview(
    name = "Confirm Signed In · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ConfirmSignedInDarkPreview() {
    LibraryPreview(dark = true) {
        ConfirmPage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seat = LibraryPreviewDefaults.Seats.first(),
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            email = LibraryPreviewDefaults.Email,
            loggedIn = true,
            note = "",
            onNoteChange = {},
            agreement = LibraryPreviewDefaults.Agreement,
            consentAccepted = true,
            onConsentChange = {},
            submitting = false,
            onSubmit = {},
        )
    }
}
