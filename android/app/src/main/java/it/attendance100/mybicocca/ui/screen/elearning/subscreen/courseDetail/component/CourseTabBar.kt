package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab

@Composable
fun CourseTabBar(
    selected: CourseTab,
    onSelect: (CourseTab) -> Unit,
    modifier: Modifier = Modifier,
    pinned: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val topPadding by animateDpAsState(if (pinned) 0.dp else 14.dp, label = "tab-bar-top")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer)
            .padding(horizontal = 16.dp)
            .padding(top = topPadding, bottom = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CourseTab.entries.forEach { tab ->
            val isActive = tab == selected
            val targetCorner by animateDpAsState(if (isActive) 16.dp else 999.dp, label = "tab-corner")
            val bg by animateColorAsState(
                if (isActive) scheme.primary else scheme.surfaceContainerHigh,
                label = "tab-bg",
            )
            val fg by animateColorAsState(
                if (isActive) scheme.onPrimary else scheme.onSurfaceVariant,
                label = "tab-fg",
            )
            Text(
                text = tabLabel(tab),
                color = fg,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(targetCorner))
                    .background(bg)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

private fun tabLabel(tab: CourseTab): String = when (tab) {
    CourseTab.Syllabus -> "Scheda"
    CourseTab.Content -> "Contenuti"
    CourseTab.Assignments -> "Compiti"
    CourseTab.Forum -> "Forum"
}
