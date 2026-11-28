package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.durationLabel
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
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
 * Final wizard step: armchair illustration over the booking recap and a pinned "Fatto" button.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryDonePage(
    libraryName: String,
    zoneName: String,
    seatName: String,
    date: LocalDate?,
    startTime: LocalTime?,
    durationMinutes: Int?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(R.drawable.library_art_armchair),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 160.dp, height = 120.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.library_booked),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "$libraryName · $zoneName · $seatName",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (date != null && startTime != null && durationMinutes != null) {
                val end = startTime.plusMinutes(durationMinutes.toLong())
                Spacer(Modifier.height(2.dp))
                Text(
                    text = date.format(FullDateFormat)
                        .replaceFirstChar { it.titlecase(currentLocale()) } +
                        " · ${startTime.format(TimeFormat)}–${end.format(TimeFormat)} · ${durationLabel(durationMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.library_find_in_bookings),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }

        DoneButton(onDone = onDone)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DoneButton(onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    Button(
        onClick = { haptic.tap(); onDone() },
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight)
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Text(
            stringResource(R.string.common_done),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(name = "LibraryDone · Light", showBackground = true)
@Composable
private fun LibraryDoneLightPreview() {
    LibraryPreview(dark = false) {
        LibraryDonePage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seatName = LibraryPreviewDefaults.Seats.first().shortName,
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            onDone = {},
        )
    }
}

@Preview(
    name = "LibraryDone · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryDoneDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryDonePage(
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            zoneName = LibraryPreviewDefaults.Zones.first().name,
            seatName = LibraryPreviewDefaults.Seats.first().shortName,
            date = LibraryPreviewDefaults.Today,
            startTime = LocalTime.of(14, 0),
            durationMinutes = 120,
            onDone = {},
        )
    }
}
