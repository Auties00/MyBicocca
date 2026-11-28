package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.LIBRARY_WEBSITE_HOST
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.LIBRARY_WEBSITE_URL
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formatLibraryFloor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.openCustomTabSafely
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.openDialerSafely
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.openEmailSafely
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.resolveHoursDetailText
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val WeekdayFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEEE", currentLocale())

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private sealed interface InfoRowEntry {
    data object Hours : InfoRowEntry
    data class Address(val cleaned: String) : InfoRowEntry
    data class Floor(val label: String) : InfoRowEntry
    data class Website(val label: String, val url: String) : InfoRowEntry
    data class Phone(val formatted: String, val raw: String) : InfoRowEntry
    data class Email(val address: String) : InfoRowEntry
}

/**
 * Connected practical-info rows on the Library page: expandable opening hours (7 days starting
 * today), cleaned address opening the address sheet, non-clickable floor label, website Custom
 * Tab, phone dialer, and email. Rows without data are omitted and outer/inner radii follow the
 * connected-row rule.
 */
@Composable
internal fun LibraryInfoCard(
    library: Library?,
    liveStatus: LibraryLiveStatus?,
    weekHours: LibraryWeekHours?,
    onOpenAddress: () -> Unit,
    modifier: Modifier = Modifier,
    initialHoursExpanded: Boolean = false,
    nowTime: LocalTime = LocalTime.now(AppClock.clock),
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    var hoursExpanded by rememberSaveable { mutableStateOf(initialHoursExpanded) }
    val hoursInView = remember { BringIntoViewRequester() }
    var revealHours by remember { mutableStateOf(false) }

    val cleanedAddress = remember(library?.address) { LibraryCatalog.cleanAddress(library?.address) }
    val floorLabel = remember(library?.id, locale) {
        library?.id?.let { formatLibraryFloor(it, locale) }
    }
    val formattedPhone = remember(library?.phone) { LibraryCatalog.formatPhone(library?.phone) }
    val cleanedEmail = library?.email?.trim()?.takeIf { it.isNotEmpty() }
    val websiteEntry = remember(library?.id, library?.websiteUrl) {
        when {
            library == null -> null
            LibraryCatalog.byId(library.id) != null -> InfoRowEntry.Website(
                label = LIBRARY_WEBSITE_HOST,
                url = LIBRARY_WEBSITE_URL,
            )
            !library.websiteUrl.isNullOrBlank() -> {
                val url = library.websiteUrl.trim()
                val label = runCatching { url.toUri().host?.removePrefix("www.") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: url
                InfoRowEntry.Website(label = label, url = url)
            }
            else -> null
        }
    }

    val hasHours = liveStatus != null || !weekHours?.days.isNullOrEmpty()
    val canExpandHours = !weekHours?.days.isNullOrEmpty()

    val entries = buildList {
        if (hasHours) add(InfoRowEntry.Hours)
        if (cleanedAddress != null) add(InfoRowEntry.Address(cleanedAddress))
        if (floorLabel != null) add(InfoRowEntry.Floor(floorLabel))
        if (websiteEntry != null) add(websiteEntry)
        if (formattedPhone != null && !library?.phone.isNullOrBlank()) {
            add(InfoRowEntry.Phone(formattedPhone, library.phone))
        }
        if (cleanedEmail != null) add(InfoRowEntry.Email(cleanedEmail))
    }
    if (entries.isEmpty()) return

    val closesAtTemplate = stringResource(R.string.library_closes_at, "%s")
    val opensAtTemplate = stringResource(R.string.library_opens_at, "%s")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        entries.forEachIndexed { index, entry ->
            val isFirst = index == 0
            val isLast = index == entries.lastIndex

            when (entry) {
                InfoRowEntry.Hours -> {
                    val isOpen = liveStatus?.isOpen == true
                    val statusWord = if (isOpen) {
                        stringResource(R.string.library_open)
                    } else {
                        stringResource(R.string.library_closed)
                    }
                    val statusColor = if (isOpen) {
                        registryAccent(RegistryAccent.Success)
                    } else {
                        scheme.error
                    }
                    val detail = resolveHoursDetailText(
                        isOpen = isOpen,
                        statusText = liveStatus?.statusText,
                        weekHours = weekHours,
                        nowTime = nowTime,
                        closesAtFormatter = { String.format(closesAtTemplate, it) },
                        opensAtFormatter = { String.format(opensAtTemplate, it) },
                    )
                    val headline = buildAnnotatedString {
                        withStyle(SpanStyle(color = statusColor, fontWeight = FontWeight.SemiBold)) {
                            append(statusWord)
                        }
                        if (!detail.isNullOrBlank()) {
                            append(" · ")
                            append(detail)
                        }
                    }

                    val chevronRotation by animateFloatAsState(
                        targetValue = if (hoursExpanded) 180f else 0f,
                        label = "hours_chevron_rotation",
                    )

                    InfoRowSurface(
                        icon = Icons.Outlined.Schedule,
                        annotatedText = headline,
                        isFirst = isFirst,
                        isLast = isLast && !(canExpandHours && hoursExpanded),
                        onClick = if (canExpandHours) {
                            {
                                hoursExpanded = !hoursExpanded
                                revealHours = hoursExpanded
                            }
                        } else {
                            null
                        },
                        trailing = if (canExpandHours) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(
                                        if (hoursExpanded) R.string.library_hours_collapse else R.string.library_hours_expand,
                                    ),
                                    tint = scheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(chevronRotation),
                                )
                            }
                        } else {
                            null
                        },
                        modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_HOURS),
                    )

                    if (canExpandHours) {
                        val days = weekHours.days
                        AnimatedVisibility(
                            visible = hoursExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            // The list opens below the fold, so scroll to it once it has its full height.
                            val settled = transition.currentState == EnterExitState.Visible && !transition.isRunning
                            LaunchedEffect(settled) {
                                if (settled && revealHours) {
                                    hoursInView.bringIntoView()
                                    revealHours = false
                                }
                            }
                            Surface(
                                shape = segmentedShape(isFirst = false, isLast = isLast),
                                color = scheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(hoursInView),
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        start = 52.dp,
                                        end = 18.dp,
                                        top = 14.dp,
                                        bottom = 14.dp,
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    days.forEach { day ->
                                        WeekHoursRow(day = day)
                                    }
                                }
                            }
                        }
                    }
                }

                is InfoRowEntry.Address -> InfoRowSurface(
                    icon = Icons.Outlined.LocationOn,
                    text = entry.cleaned,
                    isFirst = isFirst,
                    isLast = isLast,
                    onClick = onOpenAddress,
                    trailing = {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_ADDRESS),
                )

                is InfoRowEntry.Floor -> InfoRowSurface(
                    icon = null,
                    text = entry.label,
                    isFirst = isFirst,
                    isLast = isLast,
                    modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_FLOOR),
                )

                is InfoRowEntry.Website -> InfoRowSurface(
                    icon = Icons.Outlined.Public,
                    text = entry.label,
                    isFirst = isFirst,
                    isLast = isLast,
                    onClick = { openCustomTabSafely(context, entry.url) },
                    modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_WEBSITE),
                )

                is InfoRowEntry.Phone -> InfoRowSurface(
                    icon = Icons.Outlined.Phone,
                    text = entry.formatted,
                    isFirst = isFirst,
                    isLast = isLast,
                    onClick = { openDialerSafely(context, entry.raw) },
                    modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_PHONE),
                )

                is InfoRowEntry.Email -> InfoRowSurface(
                    icon = Icons.Outlined.Email,
                    text = entry.address,
                    isFirst = isFirst,
                    isLast = isLast,
                    onClick = { openEmailSafely(context, entry.address) },
                    modifier = Modifier.testTag(LibraryTestTags.DETAIL_INFO_EMAIL),
                )
            }
        }
    }
}

@Composable
private fun InfoRowSurface(
    icon: ImageVector?,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    text: String? = null,
    annotatedText: AnnotatedString? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val shape = segmentedShape(isFirst = isFirst, isLast = isLast)

    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Spacer(Modifier.size(22.dp))
            }
            if (annotatedText != null) {
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            trailing?.invoke()
        }
    }

    if (onClick != null) {
        Surface(
            onClick = { haptic.tap(); onClick() },
            shape = shape,
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
            modifier = modifier.fillMaxWidth(),
        ) { rowContent() }
    } else {
        Surface(
            shape = shape,
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
            modifier = modifier.fillMaxWidth(),
        ) { rowContent() }
    }
}

@Composable
private fun WeekHoursRow(day: LibraryDayHours) {
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    val weight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
    val color = if (day.isToday) scheme.onSurface else scheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = day.day.format(WeekdayFormat).replaceFirstChar { it.titlecase(locale) },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = weight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (day.ranges.isEmpty()) {
                stringResource(R.string.library_closed)
            } else {
                day.ranges.joinToString("  ") { "${it.open.format(TimeFormat)}–${it.close.format(TimeFormat)}" }
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = weight,
            color = if (day.ranges.isEmpty()) scheme.onSurfaceVariant else color,
            maxLines = 2,
        )
    }
}

@Preview(name = "InfoCard Collapsed · Light", showBackground = true)
@Composable
private fun InfoCardCollapsedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryInfoCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = LibraryPreviewDefaults.LiveStatusOpen,
            weekHours = LibraryPreviewDefaults.WeekHours,
            onOpenAddress = {},
            nowTime = LibraryPreviewDefaults.NowTime,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "InfoCard Collapsed · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun InfoCardCollapsedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryInfoCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = LibraryPreviewDefaults.LiveStatusClosed,
            weekHours = LibraryPreviewDefaults.WeekHours,
            onOpenAddress = {},
            nowTime = LocalTime.of(7, 30),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "InfoCard Hours Expanded · Light", showBackground = true)
@Composable
private fun InfoCardHoursExpandedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryInfoCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = LibraryPreviewDefaults.LiveStatusOpen,
            weekHours = LibraryPreviewDefaults.WeekHours,
            onOpenAddress = {},
            initialHoursExpanded = true,
            nowTime = LibraryPreviewDefaults.NowTime,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "InfoCard Hours Expanded · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun InfoCardHoursExpandedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryInfoCard(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = LibraryPreviewDefaults.LiveStatusClosed,
            weekHours = LibraryPreviewDefaults.WeekHours,
            onOpenAddress = {},
            initialHoursExpanded = true,
            nowTime = LocalTime.of(7, 30),
            modifier = Modifier.padding(16.dp),
        )
    }
}
