package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus

@Composable
fun SearchResultRow(
    name: String,
    code: String,
    breadcrumb: String,
    accent: Color,
    status: EnrolmentStatus,
    onEnrol: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainer)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = scheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp,
                    letterSpacing = (-0.15).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (code.isNotBlank()) {
                    Text(
                        text = code,
                        color = scheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.padding(top = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            CompactStatusButton(status = status, accent = accent, onClick = onEnrol)
        }
        if (breadcrumb.isNotBlank()) {
            Text(
                text = breadcrumb,
                color = scheme.onSurfaceVariant,
                fontSize = 10.5.sp,
                letterSpacing = 0.1.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactStatusButton(
    status: EnrolmentStatus,
    accent: Color,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val enabled = status == EnrolmentStatus.Idle || status == EnrolmentStatus.Failed
    val bg = when (status) {
        EnrolmentStatus.Failed -> scheme.errorContainer
        else -> accent
    }
    val onBg = when (status) {
        EnrolmentStatus.Failed -> scheme.onErrorContainer
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            EnrolmentStatus.Idle, EnrolmentStatus.Failed -> Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Aggiungi",
                tint = onBg,
                modifier = Modifier.size(16.dp),
            )
            EnrolmentStatus.InProgress -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = onBg,
            )
            EnrolmentStatus.Enrolled -> Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Aggiunto",
                tint = onBg,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
