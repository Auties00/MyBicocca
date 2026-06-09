package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.ui.component.button.PrimaryActionButton
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.ext.visual

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresenceMarkingProgress(
    label: String = "Registrazione in corso…",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoadingIndicator(modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresenceResultContent(
    outcome: PresenceMarkOutcome,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = outcome.visual()
    val pop by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "result_pop",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(112.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(visual.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = visual.onContainer,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = visual.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        val detail = outcome.detailText()
        if (detail != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        Spacer(Modifier.height(28.dp))
        PrimaryActionButton(
            text = "Fatto",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun PresenceMarkOutcome.detailText(): String? = when (this) {
    is PresenceMarkOutcome.Recorded -> statusDescription?.let { "Stato: $it" } ?: message
    else -> message
}
