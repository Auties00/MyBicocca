package it.attendance100.mybicocca.util

import androidx.annotation.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.res.*

@Composable
fun vectorResource(
	@DrawableRes id: Int,
): ImageVector {
	return ImageVector.vectorResource(id)
}
