package it.attendance100.mybicocca.core.release

import it.attendance100.mybicocca.domain.model.update.AppRelease
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * How a nightly is told apart in prose: its publish date and time in the user's locale and zone
 * (e.g. "29 ago 2026, 14:00"), which reads better after "Nightly del" than its version does.
 * Falls back to the version when the source gave no publish time.
 */
fun AppRelease.nightlyDateLabel(): String =
    publishedAt?.let {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
            .format(it)
    } ?: versionName
