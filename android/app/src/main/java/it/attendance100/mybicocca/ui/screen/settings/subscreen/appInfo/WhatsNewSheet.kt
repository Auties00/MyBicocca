package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The "What's New" page, shown as a state of the About modal rather than a separate sheet. A
 * pinned header carries a back affordance (the swipe-back gesture is wired by the host) and the
 * title; below it the published releases scroll newest-first as connected segmented cards —
 * version, date and notes — each opening that release's page in an in-app Custom Tab. Loading
 * shows a spinner, a failed load offers a retry, and no releases yet shows an empty state.
 *
 * Sized to fill the height the host hands it via [modifier] so the page reads full-screen.
 */
@Composable
fun WhatsNewScene(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.settings_whats_new_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        when (val current = state) {
            WhatsNewUiState.Loading -> CenteredBlock(Modifier.weight(1f)) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }

            WhatsNewUiState.Error -> CenteredBlock(Modifier.weight(1f)) {
                EmptyState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.whats_new_error),
                    body = "",
                    action = {
                        TextButton(onClick = viewModel::retry) {
                            Text(stringResource(R.string.whats_new_retry))
                        }
                    },
                )
            }

            is WhatsNewUiState.Loaded ->
                if (current.releases.isEmpty()) {
                    CenteredBlock(Modifier.weight(1f)) {
                        EmptyState(
                            icon = Icons.Outlined.NewReleases,
                            title = stringResource(R.string.settings_no_other_versions),
                            body = "",
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        current.releases.forEachIndexed { index, release ->
                            ReleaseCard(
                                release = release,
                                dateFormatter = dateFormatter,
                                isFirst = index == 0,
                                isLast = index == current.releases.lastIndex,
                            )
                        }
                    }
                }
        }
    }
}

/** Fills the space the header leaves and centers its content (loading / empty / error states). */
@Composable
private fun CenteredBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** One release row: title, version + date, and the notes, opening the release page on tap. */
@Composable
private fun ReleaseCard(
    release: AppRelease,
    dateFormatter: DateTimeFormatter,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = rememberHapticManager()
    val published = release.publishedAt
        ?.atZone(ZoneId.systemDefault())
        ?.let { dateFormatter.format(it) }
    val meta = listOfNotNull("v${release.versionName}", published).joinToString(" · ")

    Surface(
        onClick = {
            haptic.tap()
            CustomTabsIntent.Builder().setShowTitle(true).build()
                .launchUrl(context, release.pageUrl.toUri())
        },
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentedShape(isFirst, isLast),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 14.dp,
                top = 14.dp,
                bottom = 14.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = release.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            if (release.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = release.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
