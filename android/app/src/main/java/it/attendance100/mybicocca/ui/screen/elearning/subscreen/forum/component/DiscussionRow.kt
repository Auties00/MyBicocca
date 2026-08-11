package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.component.time.relativeTimeLabel

/**
 * One thread in the discussions list: an initials avatar, the bold subject with an unread-count
 * badge, the message preview, and a muted meta line with attachment/lock markers. Pinned threads
 * read as tertiary-container cards with a pin icon. Real Moodle avatars are almost always the
 * theme placeholder, so identity comes from initials on a per-author accent instead of images.
 */
@Composable
fun DiscussionRow(
    discussion: Discussion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val pinned = discussion.isPinned
    val haptic = rememberHapticManager()
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (pinned) scheme.tertiaryContainer else scheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { haptic.tap(); onClick() }),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuthorAvatar(name = discussion.authorName)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pinned) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.elearning_forum_pinned),
                            tint = scheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = discussion.subject,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (pinned) scheme.onTertiaryContainer else scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.1).sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (discussion.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        UnreadDot(count = discussion.unreadCount)
                    }
                }
                if (!discussion.messagePreview.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = discussion.messagePreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pinned) scheme.onTertiaryContainer.copy(alpha = 0.85f)
                        else scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                MetaLine(discussion = discussion, pinned = pinned)
            }
        }
    }
}

@Composable
private fun MetaLine(discussion: Discussion, pinned: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val muted = if (pinned) scheme.onTertiaryContainer.copy(alpha = 0.7f) else scheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = buildMetaLabel(discussion),
            style = MaterialTheme.typography.labelSmall,
            color = muted,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (discussion.hasAttachments) {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = stringResource(R.string.elearning_forum_has_attachment),
                tint = muted,
                modifier = Modifier.size(13.dp),
            )
        }
        if (discussion.isLocked) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = stringResource(R.string.elearning_forum_locked),
                tint = muted,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

/**
 * Author plus relative time; threads with replies also show who moved them last, since that
 * activity is what students chase.
 */
@Composable
private fun buildMetaLabel(discussion: Discussion): String {
    val parts = mutableListOf(discussion.authorName)
    val time = discussion.timeModified ?: discussion.createdAt
    if (time != null) parts += relativeTimeLabel(time, locale = currentLocale()).asString()
    val lastAuthor = discussion.lastPostAuthorName
    if (discussion.replyCount > 0 && !lastAuthor.isNullOrBlank() && lastAuthor != discussion.authorName) {
        parts += stringResource(R.string.elearning_forum_last_post_author, lastAuthor)
    }
    return parts.joinToString(" · ")
}


@Composable
private fun UnreadDot(count: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(OrganicShapes.Cookie)
            .background(scheme.tertiary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.coerceAtMost(99).toString(),
            color = scheme.onTertiary,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
        )
    }
}

/**
 * Avatar substitute used across the forum: the author's initials in white on an organic shape
 * filled with a per-author accent.
 */
@Composable
fun AuthorAvatar(
    name: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val (color, shape) = rememberAuthorStyle(name, scheme.primary, scheme.secondary, scheme.tertiary)
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(shape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
        )
    }
}

/**
 * Stable per-author accent + organic shape derived from the name's hash, so the same
 * teacher/student reads consistently across rows without real profile pictures.
 */
private fun rememberAuthorStyle(
    name: String,
    primary: Color,
    secondary: Color,
    tertiary: Color,
): Pair<Color, Shape> {
    val hash = name.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) }
    val color = when (hash % 3) {
        0 -> primary
        1 -> secondary
        else -> tertiary
    }
    val shape = when ((hash / 3) % 3) {
        0 -> OrganicShapes.Burst
        1 -> OrganicShapes.Puffy
        else -> OrganicShapes.SmoothCookie6
    }
    return color to shape
}

private fun initialsOf(name: String): String =
    name.split(" ", "\t").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "·" }
