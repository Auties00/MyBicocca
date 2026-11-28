package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOccupancyLevel
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.model.library.matchedMetadata
import it.attendance100.mybicocca.domain.model.library.shortSeatName
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal const val LIBRARY_WEBSITE_URL = "https://www.biblio.unimib.it/it/chi-siamo/sedi-e-orari"
internal const val LIBRARY_WEBSITE_HOST = "biblio.unimib.it"

private val ShortDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale())

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private val LeadingStatusPrefixRegex = Regex(
    "^(?:aperta|chiusa|open|closed)\\s*(?:·\\s*)?",
    RegexOption.IGNORE_CASE,
)

/** Theme-aligned green / amber / red accent for a qualitative crowding tier. */
@Composable
internal fun occupancyAccentColor(level: LibraryOccupancyLevel): Color = when (level) {
    LibraryOccupancyLevel.Low -> registryAccent(RegistryAccent.Success)
    LibraryOccupancyLevel.Moderate -> registryAccent(RegistryAccent.Warning)
    LibraryOccupancyLevel.High -> MaterialTheme.colorScheme.error
}

/** Capitalized qualitative crowding label for charts and status callouts. */
@Composable
internal fun occupancyLevelLabel(level: LibraryOccupancyLevel): String = when (level) {
    LibraryOccupancyLevel.Low -> stringResource(R.string.library_crowded_low)
    LibraryOccupancyLevel.Moderate -> stringResource(R.string.library_crowded_mid)
    LibraryOccupancyLevel.High -> stringResource(R.string.library_crowded_high)
}

/** Single accessibility description for the three-armchair crowding group. */
@Composable
internal fun crowdingArmchairsDescription(level: LibraryOccupancyLevel?): String {
    val locale = currentLocale()
    val state = if (level != null) {
        occupancyLevelLabel(level).lowercase(locale)
    } else {
        stringResource(R.string.library_crowded_unavailable)
    }
    return stringResource(R.string.library_crowding_cd, state)
}

/** Formats a duration as "30 min", "1h" or "1h 30m". */
@Composable
fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> stringResource(R.string.library_duration_minutes_only, rest)
        rest == 0 -> stringResource(R.string.library_duration_hours_only, hours)
        else -> stringResource(R.string.library_duration_hours_minutes, hours, rest)
    }
}

/** Localized building + floor line ("Ed. U6 Agorà · II piano") for a library. */
@Composable
internal fun libraryBuildingLine(library: Library): String? {
    val isItalian = currentLocale().language == "it"
    return LibraryCatalog.buildingLine(library, isItalian = isItalian)
}

/** Catalogue floor label with its first letter capitalised ("II piano", "Piano terra", "2nd floor", "Ground floor"). */
internal fun formatLibraryFloor(libraryId: String, locale: Locale): String? {
    val raw = LibraryCatalog.byId(libraryId)
        ?.floorLabel(isItalian = locale.language == "it")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null
    return raw.replaceFirstChar { it.titlecase(locale) }
}

/** Localized building + floor line for a synced reservation, when known. */
@Composable
internal fun reservationBuildingLine(reservation: LibraryReservation): String? {
    val isItalian = currentLocale().language == "it"
    return LibraryCatalog.buildingLine(
        secondaryName = reservation.librarySecondaryName,
        floorLabel = reservation.matchedMetadata()?.floorLabel(isItalian),
    )
}

/** "Oggi" / "Domani" / capitalized short date ("Dom 21 Giu") relative to [AppClock.clock]. */
@Composable
internal fun bookingDayLabel(
    date: LocalDate,
    today: LocalDate = LocalDate.now(AppClock.clock),
): String = when (date) {
    today -> stringResource(R.string.relative_day_today)
    today.plusDays(1) -> stringResource(R.string.relative_day_tomorrow)
    else -> {
        val locale = currentLocale()
        date.format(ShortDateFormat).replaceFirstChar { it.titlecase(locale) }
    }
}

/**
 * Formats a seat name without trailing annotations, prefixing "Posto" / "Seat" only when the name
 * does not already carry a seat or carrel prefix ("R 42" -> "Posto R 42", "Posto 101" -> "Posto 101").
 */
internal fun formatSeatDisplayName(shortSeatName: String, seatPrefixTemplate: String): String {
    val trimmed = shortSeatName.trim()
    if (trimmed.isEmpty()) return trimmed
    val alreadyPrefixed = trimmed.startsWith("Posto ", ignoreCase = true) ||
        trimmed.startsWith("Seat ", ignoreCase = true) ||
        trimmed.startsWith("Carrel", ignoreCase = true)
    return if (alreadyPrefixed) trimmed else String.format(seatPrefixTemplate, trimmed)
}

@Composable
internal fun LibraryReservation.formattedSeatLabel(): String =
    formatSeatDisplayName(shortSeatName(), stringResource(R.string.library_seat_number, "%s"))

/** Localized zone label for Centrale's colored seating zones; null for carrels and generic desks. */
@Composable
internal fun zoneDisplayName(color: LibraryZoneColor): String? = when (color) {
    LibraryZoneColor.Rossa -> stringResource(R.string.library_zone_red)
    LibraryZoneColor.Gialla -> stringResource(R.string.library_zone_yellow)
    LibraryZoneColor.Verde -> stringResource(R.string.library_zone_green)
    LibraryZoneColor.Lilla -> stringResource(R.string.library_zone_lilac)
    LibraryZoneColor.Blu -> stringResource(R.string.library_zone_blue)
    LibraryZoneColor.Arancio -> stringResource(R.string.library_zone_orange)
    LibraryZoneColor.Carrels, LibraryZoneColor.Other -> null
}

/**
 * Derives the detail portion after "Aperta · " / "Chiusa · " on the info card, avoiding duplicated
 * status prefixes when Affluences already includes "Aperta" / "Chiusa" in [LibraryLiveStatus.statusText].
 */
internal fun resolveHoursDetailText(
    isOpen: Boolean,
    statusText: String?,
    weekHours: LibraryWeekHours?,
    nowTime: LocalTime,
    closesAtFormatter: (String) -> String,
    opensAtFormatter: (String) -> String,
): String? {
    val days = weekHours?.days.orEmpty()
    val today = days.firstOrNull { it.isToday }
    if (isOpen) {
        val todayRanges = today?.ranges.orEmpty()
        val closingTime = todayRanges.firstOrNull { !nowTime.isBefore(it.open) && nowTime.isBefore(it.close) }?.close
            ?: todayRanges.lastOrNull()?.close
        if (closingTime != null) {
            return closesAtFormatter(closingTime.format(TimeFormat))
        }
    } else if (today != null) {
        // The service words its status in Italian only, so an opening today or tomorrow comes from
        // the timetable. A later one keeps the service's text, which can name the day.
        val soon = today.ranges.firstOrNull { it.open.isAfter(nowTime) }
            ?: days.firstOrNull { it.day == today.day.plusDays(1) }?.ranges?.firstOrNull()
        if (soon != null) {
            return opensAtFormatter(soon.open.format(TimeFormat))
        }
    }

    val stripped = statusText
        ?.trim()
        ?.replace(LeadingStatusPrefixRegex, "")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    if (stripped != null) {
        return stripped.replaceFirstChar { it.uppercaseChar() }
    }

    if (!isOpen) {
        val later = days.drop(1).firstNotNullOfOrNull { it.ranges.firstOrNull() }
        if (later != null) {
            return opensAtFormatter(later.open.format(TimeFormat))
        }
    }
    return null
}

/** Opens [url] in a Chrome Custom Tab, falling back to ACTION_VIEW; never throws if no app handles it. */
internal fun openCustomTabSafely(context: Context, url: String) {
    val uri = url.toUri()
    val customTabResult = runCatching {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
    }
    if (customTabResult.isFailure) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}

/** Launches the phone dialer for [phone]; silently ignored on devices with no dialer. */
internal fun openDialerSafely(context: Context, phone: String) {
    val digits = phone.filter { it.isDigit() || it == '+' }
    if (digits.isEmpty()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, "tel:$digits".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/** Launches a mail client for [email]; silently ignored if no mail app is installed. */
internal fun openEmailSafely(context: Context, email: String) {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_SENDTO, "mailto:${Uri.encode(trimmed)}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/** Launches a geo: intent searching for [address]; silently ignored if no maps app is installed. */
internal fun openGeoAddressSafely(context: Context, address: String) {
    val trimmed = address.trim()
    if (trimmed.isEmpty()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "geo:0,0?q=${Uri.encode(trimmed)}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/**
 * Returns the default maps app's label when exactly one non-resolver default handles `geo:`
 * intents, or null when no maps app or a disambiguation chooser would handle it.
 */
internal fun resolveDefaultMapsAppLabel(context: Context): String? = runCatching {
    val pm = context.packageManager
    val probe = Intent(Intent.ACTION_VIEW, "geo:0,0?q=Milano".toUri())
    val resolved = pm.resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY) ?: return@runCatching null
    val activityInfo = resolved.activityInfo ?: return@runCatching null
    val pkg = activityInfo.packageName
    if (
        pkg == "android" ||
        pkg == "com.android.intentresolver" ||
        activityInfo.name.contains("ResolverActivity", ignoreCase = true) ||
        activityInfo.name.contains("ChooserActivity", ignoreCase = true)
    ) {
        return@runCatching null
    }
    resolved.loadLabel(pm).toString().trim().takeIf { it.isNotEmpty() }
}.getOrNull()
