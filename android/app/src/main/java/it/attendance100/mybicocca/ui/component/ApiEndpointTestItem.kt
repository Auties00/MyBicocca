package it.attendance100.mybicocca.ui.component

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.main.settings.test.ApiEndpointUiModel
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.ui.theme.MyBicoccaLightColorScheme
import it.attendance100.mybicocca.util.ProvideHapticManager
import it.attendance100.mybicocca.util.rememberHapticManager


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun SwaggerEndpointItemDarkPreview() {
    val endpoint = ApiEndpointUiModel("test", "GET", "/test", "Test endpoint")
    ProvideHapticManager {
        MaterialTheme(
            colorScheme = MyBicoccaDarkColorScheme
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
            ) {
                SwaggerEndpointItem(endpoint = endpoint, onToggleExpand = {}, onExecute = {})
            }
        }
    }
}

@Preview
@Composable
fun SwaggerEndpointItemLightPreview() {
    val endpoint = ApiEndpointUiModel("test", "GET", "/test", "Test endpoint")
    ProvideHapticManager {
        MaterialTheme(
            colorScheme = MyBicoccaLightColorScheme
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
            ) {
                SwaggerEndpointItem(endpoint = endpoint, onToggleExpand = {}, onExecute = {})
            }
        }
    }
}

@Composable
fun SwaggerEndpointItem(
    endpoint: ApiEndpointUiModel,
    onToggleExpand: () -> Unit,
    onExecute: () -> Unit,
) {
    val methodColor = when (endpoint.method) {
        "GET" -> Color(0xFF61AFFE)
        "POST" -> Color(0xFF49CC90)
        "PUT" -> Color(0xFFFCA130)
        "DELETE" -> Color(0xFFF93E3E)
        else -> Color.Gray
    }

    val borderColor = MaterialTheme.colorScheme.onSecondary // methodColor.copy(alpha = 0.5f)
    val backgroundColor = MaterialTheme.colorScheme.surfaceDim // methodColor.copy(alpha = 0.1f)
    val haptic = rememberHapticManager()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.feather()
                        onToggleExpand()
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Method Badge
                Surface(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp)
                        .clickable {
                            haptic.tap()
                            onExecute()
                        },
                    shape = RoundedCornerShape(4.dp),
                    color = methodColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (endpoint.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = endpoint.method,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Path & Description
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (3).dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = endpoint.path,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        modifier = Modifier.offset(y = (-6).dp),
                        text = endpoint.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Expand Icon
                val rotation by animateFloatAsState(
                    targetValue = if (endpoint.isExpanded) 180f else 0f,
                    label = "rotation"
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Expanded Content
            if (endpoint.isExpanded) {
                // HorizontalDivider(color = borderColor)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .clip(shape = RoundedCornerShape(size = 4.dp))
                        .heightIn(max = 300.dp) // Approx 10 lines
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = endpoint.response
                                ?: "Click the ${endpoint.method} button to execute.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
