package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerBox
import it.attendance100.mybicocca.ui.component.time.relativeTimeLabel

/**
 * Hero card for the course's read-only "Avvisi" forum: a tertiary-container card with organic
 * blob decor, the forum name and announcement count, and an inline block for the latest
 * announcement (relative time, attachment marker, subject, message preview, author) that
 * shimmers while loading and opens the discussion directly. Teacher announcements are the
 * highest-value forum content (exam rooms, results, cancellations), so the latest one is
 * surfaced inline rather than hiding behind a generic row.
 */
@Composable
fun AnnouncementsCard(
    forum: Forum,
    latestAnnouncement: Loadable<Discussion?>,
    onOpenForum: () -> Unit,
    onOpenDiscussion: (Discussion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.tertiaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onOpenForum),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-34).dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(scheme.tertiary, OrganicShapes.Burst),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-26).dp, y = 26.dp)
                    .size(64.dp)
                    .graphicsLayer { alpha = 0.14f }
                    .background(scheme.onTertiaryContainer, OrganicShapes.Leaf),
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✦ BACHECA AVVISI",
                            color = scheme.tertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = forum.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 26.sp,
                            letterSpacing = (-0.2).sp,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = announcementCountLabel(forum.discussionCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onTertiaryContainer.copy(alpha = 0.78f),
                            fontStyle = FontStyle.Italic,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(OrganicShapes.Cookie)
                            .background(scheme.tertiary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = scheme.onTertiary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                when (latestAnnouncement) {
                    Loadable.NotYetLoaded -> LatestAnnouncementPlaceholder()
                    is Loadable.Loaded -> {
                        val announcement = latestAnnouncement.value
                        if (announcement != null) {
                            LatestAnnouncementBlock(
                                announcement = announcement,
                                onClick = { onOpenDiscussion(announcement) },
                            )
                        } else {
                            Text(
                                text = "Nessun avviso pubblicato finora",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onTertiaryContainer.copy(alpha = 0.7f),
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestAnnouncementBlock(
    announcement: Discussion,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "ULTIMO AVVISO",
                    color = scheme.tertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                val time = announcement.timeModified ?: announcement.createdAt
                if (time != null) {
                    Text(
                        text = "· ${relativeTimeLabel(time)}",
                        color = scheme.onTertiaryContainer.copy(alpha = 0.65f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (announcement.hasAttachments) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = "Con allegato",
                        tint = scheme.onTertiaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = announcement.subject,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.1).sp,
            )
            if (!announcement.messagePreview.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = announcement.messagePreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onTertiaryContainer.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = announcement.authorName,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onTertiaryContainer.copy(alpha = 0.78f),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = scheme.tertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun LatestAnnouncementPlaceholder() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ShimmerBox(modifier = Modifier.width(90.dp).height(10.dp))
            Spacer(Modifier.height(10.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
        }
    }
}

private fun announcementCountLabel(count: Int): String = when (count) {
    0 -> "Nessun avviso"
    1 -> "1 avviso dei docenti"
    else -> "$count avvisi dei docenti"
}
