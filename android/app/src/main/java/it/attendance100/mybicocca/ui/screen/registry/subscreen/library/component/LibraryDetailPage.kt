package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Accessible
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowdingPage
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDate
import java.time.LocalTime

/**
 * One library's detail page: 2:1 shared-element photo header, facility chips, weekly crowding
 * chart pager, and connected practical-info rows, over a pinned "Prenota un posto" button that
 * enables once live data is loaded.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun LibraryDetailPage(
    library: Library?,
    liveStatus: Loadable<LibraryLiveStatus>,
    weekHours: Loadable<LibraryWeekHours>,
    siteServices: List<LibraryService>,
    crowdingWeek: List<LibraryWeekdayCrowdingPage>,
    detailStatus: SyncStatus,
    onOpenAddress: () -> Unit,
    onPrenota: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(AppClock.clock),
    nowTime: LocalTime = LocalTime.now(AppClock.clock),
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(LibraryTestTags.DETAIL_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (library != null) {
                LibraryPhoto(
                    libraryId = library.id,
                    pictureUrl = library.pictureUrl,
                    clipShape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f),
                )
            }

            when (liveStatus) {
                Loadable.NotYetLoaded -> when (detailStatus) {
                    is SyncStatus.Failed -> SheetMessage(
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        title = stringResource(R.string.common_error_title),
                        body = stringResource(R.string.library_status_loading_failed),
                        action = { RetryButton(onClick = onRetry) },
                    )

                    else -> LibraryLoading(label = stringResource(R.string.common_loading))
                }

                is Loadable.Loaded -> {
                    val visibleServices = siteServices.filter {
                        it.type != LibraryServiceType.Other || it.name.isNotBlank()
                    }
                    if (visibleServices.isNotEmpty()) {
                        FacilityChipsRow(services = visibleServices)
                    }

                    if (crowdingWeek.isNotEmpty()) {
                        LibraryCrowdingChart(
                            pages = crowdingWeek,
                            today = today,
                            nowTime = nowTime,
                        )
                    }

                    LibraryInfoCard(
                        library = library,
                        liveStatus = liveStatus.value,
                        weekHours = weekHours.valueOrNull(),
                        onOpenAddress = onOpenAddress,
                        nowTime = nowTime,
                    )
                }
            }

            Spacer(Modifier.size(4.dp))
        }

        Button(
            onClick = { haptic.tap(); onPrenota() },
            enabled = liveStatus is Loadable.Loaded,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                .testTag(LibraryTestTags.DETAIL_BOOK_BUTTON),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            Icon(Icons.Outlined.EventSeat, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.library_book_seat),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FacilityChipsRow(services: List<LibraryService>) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        services.forEach { service ->
            Surface(
                shape = CircleShape,
                color = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = service.type.icon(),
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = service.localizedLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun LibraryServiceType.icon(): ImageVector = when (this) {
    LibraryServiceType.Accessibility -> Icons.AutoMirrored.Outlined.Accessible
    LibraryServiceType.Power -> Icons.Outlined.ElectricalServices
    LibraryServiceType.Wifi -> Icons.Outlined.Wifi
    LibraryServiceType.AirConditioning -> Icons.Outlined.AcUnit
    LibraryServiceType.StudyRoom -> Icons.AutoMirrored.Outlined.MenuBook
    LibraryServiceType.Other -> Icons.Outlined.CheckCircleOutline
}

@Composable
private fun LibraryService.localizedLabel(): String = when (type) {
    LibraryServiceType.Accessibility -> stringResource(R.string.library_facility_accessibility)
    LibraryServiceType.Power -> stringResource(R.string.library_facility_power)
    LibraryServiceType.Wifi -> stringResource(R.string.library_facility_wifi)
    LibraryServiceType.AirConditioning -> stringResource(R.string.library_facility_ac)
    LibraryServiceType.StudyRoom -> stringResource(R.string.library_facility_study_room)
    LibraryServiceType.Other -> name.trim()
}

@Preview(name = "LibraryDetail Loaded · Light", showBackground = true)
@Composable
private fun LibraryDetailLoadedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryDetailPage(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = Loadable.Loaded(LibraryPreviewDefaults.LiveStatusOpen),
            weekHours = Loadable.Loaded(LibraryPreviewDefaults.WeekHours),
            siteServices = LibraryPreviewDefaults.Services,
            crowdingWeek = LibraryPreviewDefaults.CrowdingWeekOpen,
            detailStatus = SyncStatus.Idle,
            onOpenAddress = {},
            onPrenota = {},
            onRetry = {},
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
        )
    }
}

@Preview(
    name = "LibraryDetail Loaded · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryDetailLoadedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryDetailPage(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = Loadable.Loaded(LibraryPreviewDefaults.LiveStatusOpen),
            weekHours = Loadable.Loaded(LibraryPreviewDefaults.WeekHours),
            siteServices = LibraryPreviewDefaults.Services,
            crowdingWeek = LibraryPreviewDefaults.CrowdingWeekOpen,
            detailStatus = SyncStatus.Idle,
            onOpenAddress = {},
            onPrenota = {},
            onRetry = {},
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
        )
    }
}

@Preview(name = "LibraryDetail Loading · Light", showBackground = true)
@Composable
private fun LibraryDetailLoadingLightPreview() {
    LibraryPreview(dark = false) {
        LibraryDetailPage(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = Loadable.NotYetLoaded,
            weekHours = Loadable.NotYetLoaded,
            siteServices = emptyList(),
            crowdingWeek = emptyList(),
            detailStatus = SyncStatus.Refreshing,
            onOpenAddress = {},
            onPrenota = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "LibraryDetail Error · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryDetailErrorDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryDetailPage(
            library = LibraryPreviewDefaults.Libraries.first(),
            liveStatus = Loadable.NotYetLoaded,
            weekHours = Loadable.NotYetLoaded,
            siteServices = emptyList(),
            crowdingWeek = emptyList(),
            detailStatus = SyncStatus.Failed(RuntimeException("offline")),
            onOpenAddress = {},
            onPrenota = {},
            onRetry = {},
        )
    }
}
