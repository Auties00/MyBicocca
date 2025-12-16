package it.attendance100.mybicocca.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.util.rememberPreferencesManager


@Composable
fun AppTitle(
    modifier: Modifier = Modifier,
) {
    val preferencesManager = rememberPreferencesManager()
    val isDarkMode = preferencesManager.isDarkMode ?: isSystemInDarkTheme()

    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(if (isDarkMode) R.drawable.dark_text else R.drawable.text),
            contentDescription = stringResource(R.string.app_logo),
            modifier = modifier.size(dimensionResource(id = R.dimen.launcher_icon_size))
        )
    }
}

@Preview(
    backgroundColor = 0xFF0D0D0D,
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun AppTitleDarkPreview() {
    MaterialTheme(
        colorScheme = MyBicoccaDarkColorScheme
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            AppTitle()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
@Composable
private fun AppTitleLightPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme()
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            AppTitle()
        }
    }
}