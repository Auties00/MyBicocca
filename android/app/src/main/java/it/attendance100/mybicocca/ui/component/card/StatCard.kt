package it.attendance100.mybicocca.ui.component.card

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.ui.theme.MyBicoccaLightColorScheme
import it.attendance100.mybicocca.util.ProvideHapticManager
import it.attendance100.mybicocca.util.rememberHapticManager


@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    textColor: Color,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val haptic = rememberHapticManager()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceDim
        ),
        onClick = {
            haptic.tap()
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = secondaryColor,
                fontSize = 12.sp,
                maxLines = 2
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
fun StatCardDarkPreview() {
    ProvideHapticManager {
        MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
            val textColor = MaterialTheme.colorScheme.onBackground
            Box(
                modifier = Modifier
                    .size(200.dp, 110.dp)
                    .padding(8.dp)
            ) {
                StatCard(
                    title = "Media aritmetica",
                    value = "8.5",
                    textColor = textColor,
                    secondaryColor = GrayColor(),
                )
            }
        }
    }
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun StatCardLightPreview() {
    ProvideHapticManager {
        MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
            val textColor = MaterialTheme.colorScheme.onBackground
            Box(
                modifier = Modifier
                    .size(200.dp, 110.dp)
                    .padding(8.dp)
            ) {
                StatCard(
                    title = "Media aritmetica",
                    value = "8.5",
                    textColor = textColor,
                    secondaryColor = GrayColor(),
                )
            }
        }
    }
}