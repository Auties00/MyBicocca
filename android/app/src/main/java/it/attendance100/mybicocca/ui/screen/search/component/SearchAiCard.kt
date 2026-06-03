package it.attendance100.mybicocca.ui.screen.search.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.attendance100.mybicocca.domain.model.search.SearchSuggestion
import it.attendance100.mybicocca.ui.component.shimmer.ShimmerBox

// On-device AI interpretation of the submitted query (Gemini Nano). Tertiary container per
// the M3 role for contrasting accents, so the card stands apart from the neutral result
// groups without borrowing the brand primary. Rendered only on devices where the model is
// available; the deterministic results below never depend on it.
@Composable
fun SearchAiCard(
    loading: Boolean,
    suggestion: SearchSuggestion?,
    onOpenTarget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = scheme.tertiaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = scheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Assistente",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            when {
                loading -> {
                    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
                    Column(modifier = Modifier.shimmer(shimmer)) {
                        ShimmerBox(modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp))
                        Spacer(Modifier.height(8.dp))
                        ShimmerBox(modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp))
                    }
                }

                suggestion != null -> {
                    Text(
                        text = suggestion.intent,
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onTertiaryContainer,
                    )
                    suggestion.explanation?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (suggestion.target != null) {
                        TextButton(
                            onClick = onOpenTarget,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = scheme.onTertiaryContainer,
                            ),
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Vai")
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
