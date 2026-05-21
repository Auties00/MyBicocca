package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
fun CatalogCourseRow(
    name: String,
    code: String,
    accent: Color,
    status: EnrolmentStatus,
    onEnrol: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onAccent = Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = onAccent,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp,
                letterSpacing = (-0.15).sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (code.isNotBlank()) {
                Text(
                    text = code.shortCourseCode(),
                    color = onAccent.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        StatusButton(status = status, accent = accent, onClick = onEnrol)
    }
}

@Composable
private fun StatusButton(
    status: EnrolmentStatus,
    accent: Color,
    onClick: () -> Unit,
) {
    val enabled = status == EnrolmentStatus.Idle || status == EnrolmentStatus.Failed
    val bg = when (status) {
        EnrolmentStatus.Enrolled -> Color.White
        else -> Color.White.copy(alpha = 0.16f)
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            EnrolmentStatus.Idle -> Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Aggiungi corso",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            EnrolmentStatus.InProgress -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
            EnrolmentStatus.Enrolled -> Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Corso aggiunto",
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            EnrolmentStatus.Failed -> Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Riprova",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// Strip Moodle's "yyyy-s-" prefix (e.g. "2526-1-E1805M001" → "E1805M001"); the
// design keeps just the academic code to keep the row scannable.
private fun String.shortCourseCode(): String {
    val regex = Regex("^\\d{4}-\\d-")
    return regex.replaceFirst(this, "")
}
