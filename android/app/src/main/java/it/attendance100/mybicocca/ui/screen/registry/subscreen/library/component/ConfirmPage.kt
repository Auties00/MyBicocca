package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.durationLabel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

// Wizard step 4: recap of the seat + the booking email, note and consent, then "Prenota".
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
    val dark = isSystemInDarkTheme()
    val end = startTime.plusMinutes(durationMinutes.toLong())

    val noteOk = !seat.noteRequired || note.isNotBlank()
    val consentOk = agreement?.mandatory != true || consentAccepted
    val canSubmit = email.isNotBlank() && noteOk && consentOk && !submitting

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
                IconRow(Icons.Outlined.LocalLibrary, "BIBLIOTECA", "$libraryName · $zoneName")
                IconRow(
                    Icons.Outlined.Chair,
                    "POSTO",
                    seat.shortName + if (seat.hasPowerOutlet) " · presa elettrica" else "",
                )
                IconRow(
                    Icons.Outlined.CalendarMonth,
                    "QUANDO",
                    buildString {
                        append(date.format(FullDateFormat).replaceFirstChar { it.titlecase(Locale.ITALIAN) })
                        append(" · ${startTime.format(TimeFormat)}–${end.format(TimeFormat)}")
                        append(" · ${durationLabel(durationMinutes)}")
                    },
                )
                // Pinned to the institutional email so the booking syncs to the user's account.
                IconRow(Icons.Outlined.AlternateEmail, "PRENOTI COME", email)
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

        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
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
                Text("Prenota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
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
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
        append("Accetto le ")
        val url = agreement.url
        val label = agreement.name ?: "condizioni d'uso"
        if (url != null) {
            withLink(LinkAnnotation.Clickable("agreement", linkStyles) {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri())
            }) { append(label) }
        } else {
            append(label)
        }
    }

    Surface(
        onClick = { if (enabled) onChange(!accepted) },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = statement,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Switch(checked = accepted, onCheckedChange = { onChange(it) }, enabled = enabled)
        }
    }
}
