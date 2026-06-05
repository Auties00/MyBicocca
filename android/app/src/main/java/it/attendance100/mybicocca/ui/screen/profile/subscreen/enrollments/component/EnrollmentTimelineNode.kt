package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.badges
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.courseYearLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.statusLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.theme.EnrollmentBadgeTone
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.theme.enrollmentBadgeTone

// One academic year on the vertical spine: the year on the rail (split AA/AA+1), a ringed
// node coloured by status, and a tappable card with course year, status and highlight chips.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnrollmentTimelineNode(
    enrollment: AnnualEnrollment,
    isLatest: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val nodeTone = enrollmentBadgeTone(
        if (isLatest) EnrollmentBadgeTone.Active else EnrollmentBadgeTone.Neutral,
    )
    val accent = if (isLatest) PassedGreen else scheme.outline
    val badges = enrollment.badges()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        // Year rail.
        Column(
            modifier = Modifier
                .width(52.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = enrollment.academicYear.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = scheme.onSurface,
            )
            Text(
                text = "/${(enrollment.academicYear + 1) % 100}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurfaceVariant,
            )
        }

        // Node rail with continuous connector.
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .then(if (isLast) Modifier.height(34.dp) else Modifier.fillMaxHeight())
                    .background(scheme.outlineVariant),
            )
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scheme.surface),
                    )
                }
            }
        }

        // Card.
        Surface(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, top = 8.dp, bottom = 12.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = enrollment.courseYearLabel(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp,
                            color = scheme.onSurface,
                        )
                        if (isLatest) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "ATTUALE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = nodeTone.onContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100))
                                    .background(nodeTone.container)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = enrollment.statusLabel(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    if (badges.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            badges.forEach { EnrollmentBadgeChip(it) }
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private val PassedGreen = Color(0xFF1FA84B)
