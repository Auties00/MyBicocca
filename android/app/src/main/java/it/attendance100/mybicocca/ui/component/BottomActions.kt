package it.attendance100.mybicocca.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SquircleAnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, //PaddingValues(16.dp)
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        label = "cornerRadius",
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(percent = cornerRadius),
        interactionSource = interactionSource,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun BaseBottomBarSurface(
    modifier: Modifier = Modifier,
    footerText: String? = null,
    isBottomBarVisible: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isBottomBarVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            color = color,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()

                if (footerText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = footerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun SingleActionBottomBar(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    footerText: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isBottomBarVisible: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    overrideContent: (@Composable RowScope.() -> Unit)? = null,
) {
    BaseBottomBarSurface(
        modifier = modifier,
        footerText = footerText,
        color = color,
        isBottomBarVisible = isBottomBarVisible
    ) {
        overrideContent ?: SquircleAnimatedButton(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun DualActionBottomBar(
    modifier: Modifier = Modifier,
    mainActionText: String,
    mainActionIcon: ImageVector? = null,
    onMainActionClick: () -> Unit,
    secondaryActionIcon: ImageVector,
    onSecondaryActionClick: () -> Unit,
    footerText: String? = null,
    mainIsEnabled: Boolean = true,
    secondaryIsEnabled: Boolean = true,
    isBottomBarVisible: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    overrideMainContent: (@Composable RowScope.() -> Unit)? = null,
    overrideSecondaryContent: (@Composable RowScope.() -> Unit)? = null,
) {
    BaseBottomBarSurface(
        modifier = modifier,
        footerText = footerText,
        color = color,
        isBottomBarVisible = isBottomBarVisible
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Main Button
            overrideMainContent ?: SquircleAnimatedButton(
                onClick = onMainActionClick,
                enabled = mainIsEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                if (mainActionIcon != null) {
                    Icon(
                        imageVector = mainActionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = mainActionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Secondary Button
            overrideSecondaryContent ?: Button(
                onClick = onSecondaryActionClick,
                enabled = secondaryIsEnabled,
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    imageVector = secondaryActionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
fun ActionBottomBar(
    modifier: Modifier = Modifier,
    footerText: String? = null,
    isBottomBarVisible: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable () -> Unit,
) {
    BaseBottomBarSurface(
        modifier = modifier,
        footerText = footerText,
        color = color,
        isBottomBarVisible = isBottomBarVisible
    ) {
        content()
    }
}
