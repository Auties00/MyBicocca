package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.appbar.AppTitle
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.util.rememberPreferencesManager

private val versionText: String by lazy {
    buildString {
        append("Version ${BuildConfig.VERSION_NAME}")
        try {
            val flavorField = BuildConfig::class.java.getDeclaredField("FLAVOR")
            val flavorName = flavorField.get(null) as? String
            if (!flavorName.isNullOrEmpty()) {
                append(" (${flavorName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }})")
            }
        } catch (_: Exception) { /* no flavors */
        }
        if (BuildConfig.DEBUG) append(" [Debug]")
    }
}

@Composable
fun AppInfoScreen(onNavigateBack: () -> Unit) {
    val grayColor = GrayColor()
    val isDarkMode = rememberPreferencesManager().isDarkMode ?: isSystemInDarkTheme()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            AppTitle()

            Text(text = versionText, color = grayColor, fontSize = 16.sp)

            Icon(
                painter = painterResource(
                    if (isDarkMode) R.drawable.logo_text_dark else R.drawable.logo_text,
                ),
                contentDescription = stringResource(R.string.app_logo),
                tint = Color.Unspecified,
                modifier = Modifier.size(190.dp),
            )

            Text(text = stringResource(R.string.copyright), color = grayColor, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val intent = android.content.Intent(
                        context,
                        com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java,
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
            ) {
                Text(text = stringResource(R.string.licenses), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
