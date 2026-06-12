package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.attendance100.mybicocca.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffMember
import it.attendance100.mybicocca.domain.model.elearning.course.CourseStaffRole
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes

/**
 * Horizontally scrolling strip of course staff tiles: each fixed-width tile shows an
 * initials avatar on an organic shape (cycling four accent variants), the member's name and
 * an Italian role label, with a faded blob echoing the avatar color in the corner. Renders
 * nothing when the staff list is empty.
 */
@Composable
fun StaffStrip(
    staff: List<CourseStaffMember>,
    modifier: Modifier = Modifier,
) {
    if (staff.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        staff.forEachIndexed { index, member ->
            val variant = staffVariant(index, scheme)
            StaffTile(member = member, variant = variant)
        }
    }
}

private data class StaffVariant(
    val avatarBg: Color,
    val avatarFg: Color,
    val avatarShape: Shape,
)

private fun staffVariant(index: Int, scheme: androidx.compose.material3.ColorScheme): StaffVariant {
    val variants = listOf(
        StaffVariant(scheme.primary, scheme.onPrimary, OrganicShapes.Burst),
        StaffVariant(scheme.tertiary, scheme.onTertiary, OrganicShapes.Cookie),
        StaffVariant(scheme.secondary, scheme.onSecondary, OrganicShapes.Puffy),
        StaffVariant(scheme.primary, scheme.onPrimary, OrganicShapes.SmoothCookie6),
    )
    return variants[index % variants.size]
}

@Composable
private fun StaffTile(member: CourseStaffMember, variant: StaffVariant) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerLow,
        modifier = Modifier.width(152.dp),
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .size(60.dp)
                    .graphicsLayer { alpha = 0.12f }
                    .background(variant.avatarBg, OrganicShapes.Leaf),
            )
            Column {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(variant.avatarShape)
                        .background(variant.avatarBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = member.initials ?: member.fullName.initials(),
                        color = variant.avatarFg,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = roleLabel(member.role),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
private fun roleLabel(role: CourseStaffRole): String = stringResource(
    when (role) {
        CourseStaffRole.Docente -> R.string.elearning_course_docente_role
        CourseStaffRole.Tutor -> R.string.elearning_course_tutor_role
        CourseStaffRole.Esercitatore -> R.string.elearning_course_esercitatore_role
        CourseStaffRole.Other -> R.string.elearning_course_staff_role
    }
)

private fun String.initials(): String =
    split(" ", "\t").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
