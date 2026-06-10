package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Expressive grouped-list shape used across the app: large outer corners on the ends of a
 * group, tight inner corners between adjacent items. A single-item group is fully rounded.
 */
fun groupedShape(isFirst: Boolean, isLast: Boolean): RoundedCornerShape = RoundedCornerShape(
    topStart = if (isFirst) 20.dp else 6.dp,
    topEnd = if (isFirst) 20.dp else 6.dp,
    bottomStart = if (isLast) 20.dp else 6.dp,
    bottomEnd = if (isLast) 20.dp else 6.dp,
)
