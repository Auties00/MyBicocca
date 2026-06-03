package it.attendance100.mybicocca.ui.component.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

// Arch-shaped leading date block shared by the booked exam card, its detail hero
// and the booking flow.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExamDateBadge(
    dayOfMonth: String,
    month: String,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(width = 60.dp, height = 72.dp),
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(size)
            .clip(MaterialShapes.Arch.toShape())
            .background(scheme.primaryContainer),
    ) {
        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = scheme.onPrimaryContainer,
        )
        Text(
            text = month,
            style = MaterialTheme.typography.labelMediumEmphasized,
            color = scheme.onPrimaryContainer,
        )
    }
}
