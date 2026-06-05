package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.state.EnrollmentBadge
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.theme.enrollmentBadgeTone

@Composable
fun EnrollmentBadgeChip(
    badge: EnrollmentBadge,
    modifier: Modifier = Modifier,
) {
    val tone = enrollmentBadgeTone(badge.tone)
    Text(
        text = badge.label.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.6.sp,
        color = tone.onContainer,
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(tone.container)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}
