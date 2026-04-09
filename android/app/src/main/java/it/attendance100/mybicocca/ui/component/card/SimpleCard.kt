package it.attendance100.mybicocca.ui.component.card

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.util.rememberHapticManager
import kotlinx.coroutines.launch
import java.time.LocalTime


@Suppress("unused")
@OptIn(ExperimentalTextApi::class)
@Composable
fun SimpleCard(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    offsetDitherModifier: Modifier = Modifier.offset(x = (-60).dp, y = (80).dp),
    ditherRotation: Float = 13f,
    animateIcon: Boolean = true,
    ditherImage: Int? = R.drawable.dither_95dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    onClick: (() -> Unit)? = null,
	shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    background: (@Composable BoxScope.() -> Unit)? = null,
    leading: (@Composable BoxScope.() -> Unit)? = null,
    trailing: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
	val haptic = rememberHapticManager()
	val scope = rememberCoroutineScope()
	val interactionSource = remember { MutableInteractionSource() }
	var lastTapDown = remember { LocalTime.now() }

	LaunchedEffect(interactionSource) {
		interactionSource.interactions.collect { interaction ->
			if (interaction is PressInteraction.Press) {
				scope.launch {
					haptic.feather()
					lastTapDown = LocalTime.now()
				}
			}
			if (interaction is PressInteraction.Release) {
				scope.launch {
					haptic.tap()
				}
			}
		}
	}

	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
		),
		interactionSource = interactionSource,
		onClick = {
			if (onClick != null) onClick()
		},
        enabled = onClick != null,
		shape = shape,
	) {
		Box {
			Box(modifier = Modifier.align(Alignment.CenterStart)) {
                if (ditherImage != null) Image(
					painter = painterResource(id = ditherImage),
					contentDescription = null,
					contentScale = ContentScale.FillHeight,
					colorFilter = ColorFilter.tint(LocalContentColor.current),
					modifier = Modifier
						.fillMaxHeight()
						.alpha(.1f)
						.zIndex(-100f),
				)
			}

			if (background != null) Box(modifier = Modifier.fillMaxSize()) {
				Box(
					modifier = Modifier
						.fillMaxSize(),
					contentAlignment = Alignment.BottomEnd,
				) {
					background()
				}
			}

			Column(
				modifier = Modifier
					.padding(horizontal = 18.dp)
					.padding(top = 12.dp, bottom = 10.dp)
					.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(4.dp),
			) {
				content()
			}

			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Box(modifier = Modifier.weight(1f)) {
					if (leading != null) leading()
				}
				Box(modifier = Modifier.weight(1f)) {
					if (trailing != null) trailing()
				}

			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true, device = "spec:width=1080px,height=2340px,dpi=240")
@Composable
fun CardDarkPreview() {
	val textColor = MaterialTheme.colorScheme.onBackground
	Box(
		modifier = Modifier
			.size(200.dp, 110.dp)
			.padding(8.dp),
	) {
		SimpleCard(
			textColor = textColor,
			secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
			content = {
				Text(
					text = "Appelli Esami",
					color = textColor,
				)
			},
		)
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true, device = "spec:width=1080px,height=2340px,dpi=240")
@Composable
fun CardLightPreview() {
	val textColor = MaterialTheme.colorScheme.onBackground
	Box(
		modifier = Modifier
			.size(200.dp, 110.dp)
			.padding(8.dp),
	) {
		SimpleCard(
			textColor = textColor,
			secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
			content = {
				Text(
					text = "Appelli Esami",
					color = textColor,
				)
			},
		)
	}
}
