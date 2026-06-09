package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// In-sheet empty/error/result state — the same visual language as the full-screen EmptyState
// (cookie-shaped icon chip, headline title, body), sized for a modal sheet (wrap-content, not
// fillMaxSize). The icon chip is neutral by default; pass [container]/[onContainer] for a
// semantic tint (success/error/…). [action] is centered below; result pages pass a full-width
// button group instead.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetMessage(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    onContainer: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (body != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action?.let {
            Spacer(Modifier.height(28.dp))
            it()
        }
    }
}
