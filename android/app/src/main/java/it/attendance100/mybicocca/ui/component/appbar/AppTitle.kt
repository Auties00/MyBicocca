package it.attendance100.mybicocca.ui.component.appbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R

@Composable
fun AppTitle(modifier: Modifier = Modifier, height: Dp = 20.dp) {
    val isDarkMode = isSystemInDarkTheme()
    Image(
        painter = painterResource(if (isDarkMode) R.drawable.dark_text else R.drawable.text),
        contentDescription = stringResource(R.string.app_logo),
        modifier = Modifier
            .height(height)
            .then(modifier),
    )
}
