package it.attendance100.mybicocca.ui.screen.settings.component.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val HeadingTextSize = 15.sp
internal const val SUBTEXT_OPACITY = 0.75f
internal val MinRowHeight = 58.dp
internal val PrefXPadding = 15.dp
internal val PrefYPadding = 15.dp

@Composable
fun SettingsSectionTitle(
    titleText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 9.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = titleText,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = PrefXPadding)
        )
    }
}


fun Modifier.dimmedContent(): Modifier = this.alpha(SUBTEXT_OPACITY)

@Composable
internal fun BaseSettingTile(
    modifier: Modifier = Modifier,
    titleText: String? = null,
    middleContent: @Composable (ColumnScope.() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    onRowClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = MinRowHeight)
            .clickable(
                enabled = onRowClick != null,
                onClick = { onRowClick?.invoke() }
            )
    ) {

        if (leadingContent != null) {
            Box(
                modifier = Modifier.padding(start = PrefXPadding, end = 7.dp)
            ) {
                leadingContent()
            }
        }

        Column(
            modifier = Modifier
                .padding(vertical = PrefYPadding)
                .weight(1f)
        ) {
            if (!titleText.isNullOrBlank()) {
                Text(
                    text = titleText,
                    fontSize = HeadingTextSize,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = PrefXPadding)
                )
            }

            middleContent?.invoke(this)
        }

        if (trailingContent != null) {
            Box(
                modifier = Modifier.padding(end = PrefXPadding)
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
fun SwitchSettingTile(
    label: String,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: ImageVector? = null,
    isToggled: Boolean = false
) {

    StandardSettingTile(
        title = label,
        modifier = modifier,
        subtitle = description,
        iconAsset = leadingIcon,
        onItemClick = { onToggle(!isToggled) },
        trailingAction = {
            Switch(
                checked = isToggled,
                onCheckedChange = null,
                modifier = Modifier.padding(start = EndWidgetSpacing)
            )
        }
    )
}

internal val EndWidgetSpacing = 15.dp

@Composable
fun StandardSettingTile(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    iconAsset: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailingAction: @Composable (() -> Unit)? = null,
    onItemClick: (() -> Unit)? = null
) {
    BaseSettingTile(
        titleText = title,
        modifier = modifier,
        leadingContent = if (iconAsset != null) {
            { Icon(imageVector = iconAsset, tint = iconTint, contentDescription = null) }
        } else null,
        middleContent = if (!subtitle.isNullOrBlank()) {
            {
                Text(
                    text = subtitle,
                    maxLines = 10,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = PrefXPadding)
                        .dimmedContent()
                )
            }
        } else null,
        trailingContent = trailingAction,
        onRowClick = onItemClick
    )
}