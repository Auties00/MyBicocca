package it.attendance100.mybicocca.ui.component.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import it.attendance100.mybicocca.R

@Composable
fun DitheredTexture() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.card_texture),
                contentScale = ContentScale.FillBounds
            ),
        contentAlignment = Alignment.TopStart
    ) {}
}
