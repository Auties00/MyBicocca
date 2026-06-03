package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes

// Page header: the global bar shows a static "Forum", so the forum's own identity
// (name, type, intro the teacher wrote) lives here.
@Composable
fun ForumHeader(
    forum: Forum,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val isNews = forum.type == ForumType.News
    val accent = if (isNews) scheme.tertiary else scheme.primary
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(if (isNews) OrganicShapes.Cookie else OrganicShapes.Sunny)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = headerIcon(forum.type),
                    contentDescription = null,
                    tint = if (isNews) scheme.onTertiary else scheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "✦ ${headerEyebrow(forum.type).uppercase()}",
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = forum.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.3).sp,
                )
            }
        }
        val intro = forum.intro?.let(::stripHeaderIntro)
        if (!intro.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

private fun headerIcon(type: ForumType): ImageVector = when (type) {
    ForumType.News -> Icons.Outlined.Campaign
    ForumType.QandA -> Icons.AutoMirrored.Outlined.HelpOutline
    else -> Icons.Outlined.Forum
}

private fun headerEyebrow(type: ForumType): String = when (type) {
    ForumType.News -> "Bacheca avvisi"
    ForumType.QandA -> "Domande e risposte"
    ForumType.EachUser -> "Una discussione per studente"
    ForumType.SingleSimple -> "Discussione singola"
    ForumType.BlogLike -> "Formato blog"
    else -> "Forum del corso"
}

private fun stripHeaderIntro(html: String): String =
    html.replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ")
