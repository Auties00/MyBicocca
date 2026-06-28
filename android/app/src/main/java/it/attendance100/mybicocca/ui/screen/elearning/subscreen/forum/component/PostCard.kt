package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.component

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostAttachment
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.component.time.relativeTimeLabel
import it.attendance100.mybicocca.ui.theme.LocalIsOnline

/**
 * One post rendered as its own card (the connector rails + indentation + swipe live in
 * [ThreadPostItem]): author avatar and name with a relative timestamp, the HTML message body,
 * attachment chips, and an overflow menu with the reply/edit/delete actions the post allows.
 * The opening post reads larger and shows the discussion subject; replies are lighter. Deleted
 * posts render as an italic tombstone, and a post with replies shows a collapse toggle that
 * folds its sub-thread away.
 */
@Composable
fun PostCard(
    post: Post,
    isOpeningPost: Boolean,
    hasChildren: Boolean,
    descendantCount: Int,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenAttachment: (PostAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(if (isOpeningPost) 24.dp else 20.dp),
        color = if (isOpeningPost) scheme.surfaceContainer else scheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(if (isOpeningPost) 18.dp else 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuthorAvatar(name = post.authorName)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    val time = post.modifiedAt ?: post.createdAt
                    if (time != null) {
                        Text(
                            text = relativeTimeLabel(time),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
                if (post.canReply || post.canEdit || post.canDelete) {
                    PostMenu(post = post, onReply = onReply, onEdit = onEdit, onDelete = onDelete)
                }
            }

            Spacer(Modifier.height(if (isOpeningPost) 12.dp else 8.dp))

            if (post.isDeleted) {
                Text(
                    text = stringResource(R.string.elearning_forum_deleted_post),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            } else {
                if (isOpeningPost && post.subject.isNotBlank()) {
                    Text(
                        text = post.subject,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                }
                if (post.message.isNotBlank()) {
                    HtmlBody(html = post.message, color = scheme.onSurface)
                }
                if (post.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        post.attachments.forEach { attachment ->
                            val haptic = rememberHapticManager()
                            AttachmentChip(
                                attachment = attachment,
                                onClick = { haptic.tap(); onOpenAttachment(attachment) })
                        }
                    }
                }
            }

            if (hasChildren) {
                Spacer(Modifier.height(10.dp))
                val haptic = rememberHapticManager()
                RepliesToggle(
                    count = descendantCount,
                    collapsed = collapsed,
                    onClick = { haptic.tap(); onToggleCollapse() })
            }
        }
    }
}

/**
 * Single pill control that folds / unfolds the post's sub-thread; the chevron rotates and the
 * label flips between showing and hiding the reply count.
 */
@Composable
private fun RepliesToggle(count: Int, collapsed: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val rotation by animateFloatAsState(if (collapsed) -90f else 0f, label = "replies_chevron")
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (collapsed) scheme.secondaryContainer else scheme.surfaceContainerHighest,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = if (collapsed) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation),
            )
            Text(
                text = if (collapsed) {
                    pluralStringResource(R.plurals.elearning_forum_reply_count, count, count)
                } else stringResource(R.string.elearning_forum_collapse_replies),
                style = MaterialTheme.typography.labelLarge,
                color = if (collapsed) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PostMenu(post: Post, onReply: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val isOnline = LocalIsOnline.current
    val haptic = rememberHapticManager()
    Box {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { haptic.tap(); expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.elearning_forum_actions),
                tint = scheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (post.canReply) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.elearning_forum_reply)) },
                    onClick = { haptic.tap(); expanded = false; onReply() },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Reply, contentDescription = null) },
                    enabled = isOnline,
                )
            }
            if (post.canEdit) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.elearning_forum_edit)) },
                    onClick = { haptic.tap(); expanded = false; onEdit() },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    enabled = isOnline,
                )
            }
            if (post.canDelete) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.elearning_forum_delete),
                            color = scheme.error
                        )
                    },
                    onClick = { haptic.tap(); expanded = false; onDelete() },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = scheme.error
                        )
                    },
                    enabled = isOnline,
                )
            }
        }
    }
}

@Composable
private fun AttachmentChip(attachment: PostAttachment, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Surface(
        onClick = { haptic.tap(); onClick() },
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = attachmentIcon(attachment.mimeType, attachment.fileName),
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            attachment.sizeBytes?.let { size ->
                Text(
                    text = humanSize(size),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

private fun attachmentIcon(mimeType: String?, fileName: String): ImageVector = when {
    mimeType?.startsWith("image/") == true -> Icons.Outlined.Image
    mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true) -> Icons.Outlined.PictureAsPdf
    else -> Icons.AutoMirrored.Outlined.InsertDriveFile
}

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
}
