package it.attendance100.mybicocca.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*

@Composable
fun <T> AutoScrollingFilterRow(
	items: List<T>,
	selectedItem: T,
	onSelectionChanged: (T) -> Unit,
	labelProvider: @Composable (T) -> String,
	leadingIcon: ((T, Boolean, @Composable (() -> Unit)) -> @Composable (() -> Unit)?)? = null,
	trailingIcon: ((T, Boolean) -> @Composable (() -> Unit)?)? = null,
	modifier: Modifier = Modifier,
	colors: SelectableChipColors? = null,
	itemSpacing: Dp = 8.dp,
	startPadding: Dp = 12.dp,
	contentPadding: PaddingValues = PaddingValues(bottom = 12.dp),
	animationSpec: AnimationSpec<Float> = tween(durationMillis = 600, easing = FastOutSlowInEasing),
) {
	val scrollState = rememberScrollState()
	val coroutineScope = rememberCoroutineScope()
	val itemPositions = remember { mutableStateMapOf<Int, Int>() }
	val density = LocalDensity.current
	var lastIndex by remember { mutableIntStateOf(0) }

	val primaryColor = MaterialTheme.colorScheme.primary
	val textColor = MaterialTheme.colorScheme.onBackground

	val chipColors = colors ?: FilterChipDefaults.filterChipColors(
		selectedLabelColor = textColor,
		selectedContainerColor = primaryColor,
		selectedLeadingIconColor = textColor,
	)

	Row(
		modifier = modifier
			.fillMaxWidth()
			.horizontalScroll(scrollState)
			.padding(contentPadding),
		horizontalArrangement = Arrangement.spacedBy(itemSpacing),
	) {
		Spacer(modifier = Modifier.width(startPadding))

		items.forEachIndexed { index, item ->
			val isSelected = item == selectedItem
			val defaultIcon = @Composable {
				Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
			}

			val finalLeadingIcon = if (leadingIcon != null) {
				leadingIcon(item, isSelected, defaultIcon)
			} else {
				if (isSelected) {
					{ defaultIcon() }
				} else null
			}

			val finalTrailingIcon = trailingIcon?.invoke(item, isSelected)

			FilterChip(
				selected = isSelected,
				colors = chipColors,
				onClick = {
					onSelectionChanged(item)

					val currentScrollPosition = scrollState.value
					val targetX = itemPositions[index] ?: 0

					if (targetX == currentScrollPosition) return@FilterChip

					val direction = if (targetX - currentScrollPosition >= 0) 1 else -1
					val isIndexLower = index < lastIndex
					lastIndex = index

					val paddingOffset = with(density) {
						(if (direction == -1 || isIndexLower) 16 else 32).dp.toPx()
					}.toInt()

					coroutineScope.launch {
						scrollState.animateScrollTo(
							value = targetX - paddingOffset,
							animationSpec = animationSpec,
						)
					}
				},
				label = { Text(labelProvider(item)) },
				leadingIcon = finalLeadingIcon,
				trailingIcon = finalTrailingIcon,
				modifier = Modifier.onGloballyPositioned { coordinates ->
					itemPositions[index] = coordinates.positionInParent().x.toInt()
				},
			)
		}
	}
}
