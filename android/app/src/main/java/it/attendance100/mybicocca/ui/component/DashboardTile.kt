package it.attendance100.mybicocca.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.util.*

@Composable
fun RowScope.DashboardTile(
	title: String,
	icon: ImageVector,
	isWide: Boolean,
	onClick: () -> Unit,
	primaryColor: Color,
	textColor: Color,
	height: Dp = 100.dp,
	haptic: HapticManager = rememberHapticManager(),
) {
	Surface(
		modifier = Modifier
		  .weight(if (isWide) 2f else 1f)
		  .height(height),
		shape = RoundedCornerShape(16.dp),
		color = MaterialTheme.colorScheme.surfaceContainerLowest,
		tonalElevation = 2.dp,
		onClick = {
			onClick()
			haptic.tap()
		},
	) {
		if (isWide) {
			// WIDE LAYOUT
			Row(
				modifier = Modifier
				  .fillMaxSize()
				  .padding(16.dp)
				  .offset(x = (-5).dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = primaryColor,
					modifier = Modifier.size(32.dp),
				)
				Spacer(modifier = Modifier.width(9.dp))
				Text(
					text = title,
					fontSize = 16.sp,
					fontWeight = FontWeight.Medium,
					color = textColor,
				)
			}
		} else {
			// SMALL LAYOUT
			Column(
				modifier = Modifier
				  .fillMaxSize()
				  .padding(12.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = primaryColor,
					modifier = Modifier.size(32.dp),
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = title,
					fontSize = 13.sp,
					fontWeight = FontWeight.Medium,
					color = textColor,
				)
			}
		}
	}
}
